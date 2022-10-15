package gg.scala.friends.handler

import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.message.AwareMessage
import gg.scala.basics.plugin.profile.BasicsProfile
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
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

        val username = CubedCacheUtil
            .fetchName(uniqueId)
            ?: return

        val type = message.retrieve<String>("type")

        for (player in Bukkit.getOnlinePlayers())
        {
            val friendPlayer = KPlayerHandler.find(player.uniqueId) ?: continue

            if (friendPlayer.friends[uniqueId] != null)
            {
                player.sendMessage("${CC.B_PRI}Friends ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.GREEN}$username ${CC.SEC}$type.")
            }
        }
    }

    @Subscribe("friend-request-accept")
    fun subscribeOnFriendRequestAccept(message: AwareMessage)
    {
        val uniqueId = message.retrieve<UUID>("uniqueId")
        val friendPlayer = KPlayerHandler.find(uniqueId) ?: return

        friendPlayer.friends[message.retrieve("friend")] = System.currentTimeMillis()
        friendPlayer.save()
    }

    @Subscribe("friend-request-removal")
    fun subscribeOnFriendRequestRemoval(message: AwareMessage)
    {
        val uniqueId = message.retrieve<UUID>("uniqueId")
        val friendPlayer = KPlayerHandler.find(uniqueId) ?: return

        friendPlayer.friends.remove(
            message.retrieve("friend")
        )
        friendPlayer.save()
    }

    fun attemptDispatchFriendRequestTo(player: Player, uniqueId: UUID): CompletableFuture<Void>
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        if (player.uniqueId == uniqueId)
        {
            throw ConditionFailedException("You cannot add yourself as a friend.")
        }

        val username = CubedCacheUtil.fetchName(uniqueId)!!

        if (friendPlayer.friends[uniqueId] != null)
        {
            throw ConditionFailedException("You already have ${CC.YELLOW}$username${CC.RED} added as a friend.")
        }

        return QuickAccess.online(uniqueId).thenCompose {
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
        return hasOutgoingFriendRequestFrom(player.uniqueId, uniqueId).thenCompose {
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
        Lemon.instance.aware.internal().connect()!!
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

            Lemon.instance.aware.internal().apply {
                val requestKey =
                    "friends:requests:$uniqueId:${player.uniqueId}"

                cacheConn.sync().hset(
                    requestKey, player.uniqueId.toString(),
                    System.currentTimeMillis().toString()
                )

                cacheConn.sync().expire(
                    requestKey,
                    TimeUnit.MINUTES.toSeconds(5L)
                )
            }

            QuickAccess.sendGlobalPlayerFancyMessage(
                fancyMessage = message, uuid = uniqueId
            )

            player.sendMessage("${CC.GREEN}You've sent a friend request to ${CC.YELLOW}$username${CC.GREEN}!")
        }
    }

    fun attemptFriendRequestAcceptFrom(player: Player, uniqueId: UUID): CompletableFuture<Void>
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        if (player.uniqueId == uniqueId)
        {
            throw ConditionFailedException("You cannot add yourself as a friend.")
        }

        val username = CubedCacheUtil.fetchName(uniqueId)!!

        if (friendPlayer.friends[uniqueId] != null)
        {
            throw ConditionFailedException("You already have ${CC.YELLOW}$username${CC.RED} added as a friend.")
        }

        return hasOutgoingFriendRequestFrom(uniqueId, player.uniqueId).thenCompose {
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
        return CompletableFuture.runAsync {
            val friendPlayer = KPlayerHandler.find(player)
                ?: throw RuntimeException()

            friendPlayer.friends[uniqueId] = System.currentTimeMillis()
            friendPlayer.save()

            cacheConn.apply {
                sync().hdel("friends:requests:${player.uniqueId}:$uniqueId", uniqueId.toString())
            }

            RedisHandler.buildMessage(
                packet = "friend-request-accept",
                "uniqueId" to uniqueId.toString(),
                "friend" to player.uniqueId.toString(),
            ).publish(
                channel = "friends"
            )

            QuickAccess.sendGlobalPlayerMessage(
                message = "${CC.GREEN}Your friend request to ${CC.PRI}${player.name}${CC.GREEN} has been accepted!",
                uuid = uniqueId
            )

            player.sendMessage("${CC.SEC}You've accepted ${CC.PRI}$username's${CC.SEC} friend request.")
        }
    }

    fun attemptFriendRemoval(player: Player, uniqueId: UUID)
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        val username = CubedCacheUtil.fetchName(uniqueId)!!

        if (friendPlayer.friends[uniqueId] == null)
        {
            throw ConditionFailedException("${CC.YELLOW}$username${CC.RED} is not on your friends list.")
        }

        internalHandleFriendRemoval(player, uniqueId, username)
    }

    private fun internalHandleFriendRemoval(player: Player, uniqueId: UUID, username: String)
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        friendPlayer.friends.remove(uniqueId)
        friendPlayer.bestFriends.remove(uniqueId)
        friendPlayer.save()

        RedisHandler.buildMessage(
            packet = "friend-request-removal",
            "uniqueId" to uniqueId.toString(),
            "friend" to player.uniqueId.toString(),
        ).publish(
            channel = "friends"
        )

        player.sendMessage("${CC.SEC}You've removed ${CC.PRI}$username${CC.SEC} from your friends list.")
    }

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


        return AsyncLemonPlayer.of(uniqueId, false).future
            .thenApply {
                if (it.isEmpty())
                {
                    throw ConditionFailedException("${CC.YELLOW}${uniqueId.username()}${CC.RED} has never logged on the server.")
                }

                it[0].getSetting("friend-requests-disabled")
            }
    }

    private fun hasOutgoingFriendRequestFrom(requester: UUID, target: UUID): CompletableFuture<Boolean>
    {
        return CompletableFuture.supplyAsync {
            var exists: Boolean

            cacheConn.apply {
                exists = sync()
                    .hget("friends:requests:$target:$requester", requester.toString()) != null
            }

            exists
        }
    }

    fun getOutgoingFriendRequests(requester: UUID): List<Pair<String, UUID>>
    {
        val requests = mutableListOf<Pair<String, UUID>>()

        val keys = this.cacheConn.sync()
            .keys("friends:requests:*")

        for (key in keys)
        {
            val split = key
                .split(":")
                .toMutableList()

            if (key.contains(requester.toString()))
            {
                requests += key to UUID
                    .fromString(
                        split[2]
                    )
            }
        }

        return requests
    }
}
