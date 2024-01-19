package gg.scala.friends.handler

import com.mongodb.client.model.Filters
import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.message.AwareMessage
import gg.scala.basics.plugin.profile.BasicsProfile
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.friends.friendship.Friendship
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.utils.Players
import net.evilblock.cubed.ScalaCommonsSpigot
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
object KFriendHandler
{
    @Subscribe("friend-network-update")
    fun subscribeOnFriendNetworkUpdate(message: AwareMessage)
    {
        val uniqueId = message.retrieve<UUID>("uniqueId")
        val type = message.retrieve<String>("type")

        CompletableFuture.allOf(
            *Players.all()
                .map {
                    getFriendshipBetween(it.uniqueId, uniqueId)
                        .thenAccept { friendship ->
                            if (friendship == null)
                                return@thenAccept

                            it.sendMessage(
                                "${CC.BD_AQUA}Friends ${CC.B_GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${
                                    if (type == "left") CC.RED else CC.GREEN
                                }${uniqueId.username()} ${CC.YELLOW}$type."
                            )
                        }
                }
                .toTypedArray()
        )
    }

    fun attemptDispatchFriendRequestTo(player: Player, uniqueId: UUID): CompletableFuture<Void>
    {
        if (player.uniqueId == uniqueId)
        {
            throw ConditionFailedException("You cannot add yourself as a friend.")
        }

        val username = CubedCacheUtil.fetchName(uniqueId)!!

        return getFriendshipBetween(player.uniqueId, uniqueId)
            .thenAccept {
                if (it != null)
                {
                    throw ConditionFailedException("You already have ${CC.YELLOW}$username${CC.RED} added as a friend.")
                }
            }
            .thenComposeAsync {
                QuickAccess.online(uniqueId)
            }
            .thenCompose {
                if (it == null)
                {
                    throw ConditionFailedException("${CC.YELLOW}$username${CC.RED} is not online.")
                }

                handlePostPlayerOnlineCheck(player, uniqueId, username)
            }
    }

    private fun handlePostPlayerOnlineCheck(
        player: Player, uniqueId: UUID, username: String
    ): CompletableFuture<Void>
    {
        return CompletableFuture
            .supplyAsync {
                hasOutgoingFriendRequestFrom(player.uniqueId, uniqueId)
            }
            .thenCompose {
                if (it)
                {
                    throw ConditionFailedException("${CC.RED}You've already sent a friend request to ${CC.YELLOW}$username${CC.RED}, please wait.")
                }

                retrieveFriendRequestStatus(uniqueId).thenCompose { disabled ->
                    if (disabled)
                    {
                        throw ConditionFailedException("${CC.YELLOW}$username${CC.RED} has their friend requests disabled.")
                    }

                    internalHandleFriendRequestDispatch(player, uniqueId, username)
                }
            }
    }

    val cacheConn by lazy {
        ScalaCommonsSpigot.instance.kvConnection
    }

    private fun internalHandleFriendRequestDispatch(
        player: Player, uniqueId: UUID, username: String
    ): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            val message = FancyMessage()
            message.withMessage(
                "",
                " ${CC.PRI}${player.name}${CC.SEC} sent you a friend request!",
                " ${CC.GREEN}(Click to accept)",
                ""
            )
            message.andHoverOf(
                "${CC.GREEN}Click to accept ${CC.YELLOW}${player.name}'s${CC.GREEN} friend request!"
            )
            message.andCommandOf(
                ClickEvent.Action.RUN_COMMAND,
                "/friend accept ${player.name}"
            )

            val requestKey = "friends:requests:outgoing:${player.uniqueId}"
            cacheConn.sync().hset(
                requestKey, uniqueId.toString(),
                "${System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5L)}"
            )

            cacheConn.sync().hset(
                "friends:requests:incoming:${uniqueId}", player.uniqueId.toString(),
                "${System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5L)}"
            )

            QuickAccess.sendGlobalPlayerFancyMessage(
                fancyMessage = message, uuid = uniqueId
            )

            player.sendMessage("${CC.GREEN}You've sent a friend request to ${CC.YELLOW}$username${CC.GREEN}!")
        }
    }

    fun attemptFriendRequestAcceptFrom(player: Player, uniqueId: UUID): CompletableFuture<Void>
    {
        if (player.uniqueId == uniqueId)
        {
            throw ConditionFailedException("You cannot add yourself as a friend.")
        }

        val username = CubedCacheUtil.fetchName(uniqueId)!!

        return getFriendshipBetween(player.uniqueId, uniqueId)
            .thenAccept {
                if (it != null)
                {
                    throw ConditionFailedException("You already have ${CC.YELLOW}$username${CC.RED} added as a friend.")
                }
            }
            .thenApplyAsync {
                hasOutgoingFriendRequestFrom(uniqueId, player.uniqueId)
            }
            .thenCompose {
                if (!it)
                {
                    throw ConditionFailedException("${CC.RED}You don't have a friend request from ${CC.YELLOW}$username${CC.RED}.")
                }

                internalHandleFriendRequestAccept(player, uniqueId, username)
            }
    }

    private fun internalHandleFriendRequestAccept(
        player: Player, uniqueId: UUID, username: String
    ): CompletableFuture<Void>
    {
        return createFriendshipBetween(player.uniqueId, uniqueId)
            .thenRun {
                cacheConn.apply {
                    sync().hdel("friends:requests:incoming:${player.uniqueId}", uniqueId.toString())
                    sync().hdel("friends:requests:outgoing:${uniqueId}", player.uniqueId.toString())
                }

                QuickAccess.sendGlobalPlayerMessage(
                    message = "${CC.GREEN}Your friend request to ${CC.PRI}${player.name}${CC.GREEN} has been accepted!",
                    uuid = uniqueId
                )

                player.sendMessage("${CC.GREEN}You've accepted ${CC.BOLD}$username's${CC.GREEN} friend request.")
            }
    }

    fun attemptFriendRemoval(player: Player, uniqueId: UUID): CompletableFuture<Void>
    {
        val username = CubedCacheUtil.fetchName(uniqueId)!!

        return getFriendshipBetween(player.uniqueId, uniqueId)
            .thenCompose {
                if (it == null)
                {
                    player.sendMessage("${CC.YELLOW}$username${CC.RED} is not on your friends list.")
                    return@thenCompose CompletableFuture.completedFuture(null)
                }

                return@thenCompose deleteFrienshipBetween(player.uniqueId, uniqueId)
                    .thenRun {
                        internalHandleFriendRemoval(player, uniqueId, username)
                    }
            }
    }

    private fun internalHandleFriendRemoval(player: Player, uniqueId: UUID, username: String)
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        friendPlayer.bestFriends.remove(uniqueId)
        friendPlayer.save()

        player.sendMessage(
            "${CC.RED}You have removed ${CC.BOLD}$username${CC.RED} from your friends list."
        )
    }

    fun deleteFrienshipBetween(user1: UUID, user2: UUID) = getFriendshipBetween(user1, user2)
        .thenComposeAsync {
            if (it == null)
                return@thenComposeAsync CompletableFuture
                    .completedFuture(null)

            DataStoreObjectControllerCache
                .findNotNull<Friendship>()
                .delete(
                    it.identifier,
                    DataStoreStorageType.MONGO
                )
        }

    fun createFriendshipBetween(user1: UUID, user2: UUID) =
        Friendship(player1 = user1, player2 = user2)
            .save()

    fun getFriendshipBetween(user1: UUID, user2: UUID) = DataStoreObjectControllerCache
        .findNotNull<Friendship>()
        .mongo()
        .loadWithFilter(
            Filters.or(
                Filters.and(
                    Filters.eq("player1", user1.toString()),
                    Filters.eq("player2", user2.toString())
                ),
                Filters.and(
                    Filters.eq("player2", user1.toString()),
                    Filters.eq("player1", user2.toString())
                )
            )
        )

    fun getAllFriendshipsOf(user: UUID) = DataStoreObjectControllerCache
        .findNotNull<Friendship>()
        .mongo()
        .loadAllWithFilter(
            Filters.or(
                Filters.eq("player1", user.toString()),
                Filters.eq("player2", user.toString())
            )
        )
        .thenApply { it.values.toList() }

    private fun retrieveFriendRequestStatus(uniqueId: UUID): CompletableFuture<Boolean>
    {
        if (Bukkit.getPluginManager().isPluginEnabled("ScBasics"))
        {
            return DataStoreObjectControllerCache
                .findNotNull<BasicsProfile>()
                .load(uniqueId, DataStoreStorageType.MONGO)
                .thenApply {
                    if (it == null)
                    {
                        throw ConditionFailedException("${CC.YELLOW}${uniqueId.username()}${CC.RED} has never logged on the server.")
                    }

                    val stateSettingValue = it
                        .setting(
                            id = "friend_requests",
                            default = StateSettingValue.ENABLED
                        )

                    stateSettingValue == StateSettingValue.DISABLED
                }
        }

        return AsyncLemonPlayer
            .of(uniqueId)
            .computeNow()
            .thenApply {
                if (it.isEmpty())
                {
                    throw ConditionFailedException("${CC.YELLOW}${uniqueId.username()}${CC.RED} has never logged on the server.")
                }

                it[0].getSetting("friend-requests-disabled")
            }
    }

    private fun hasOutgoingFriendRequestFrom(requester: UUID, target: UUID): Boolean
    {
        val outgoing = cacheConn.sync()
            .hget(
                "friends:requests:outgoing:$requester",
                target.toString()
            )

        return !(outgoing == null || outgoing.toLong() < System.currentTimeMillis())
    }

    fun getOutgoingFriendRequests(requester: UUID) = cacheConn.sync()
        .hgetall("friends:requests:outgoing:$requester")
        .mapValues { it.value.toLong() }
        .filterValues {
            it > System.currentTimeMillis()
        }
}
