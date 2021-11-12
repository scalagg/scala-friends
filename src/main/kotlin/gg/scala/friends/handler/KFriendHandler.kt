package gg.scala.friends.handler

import gg.scala.friends.registrar.impl.KPlayerHandler
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.DataStoreHandler
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.md_5.bungee.api.chat.ClickEvent
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
    fun attemptDispatchFriendRequestTo(player: Player, uniqueId: UUID)
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        val username = CubedCacheUtil.fetchName(uniqueId)!!

        if (friendPlayer.friends[uniqueId] != null)
        {
            throw ConditionFailedException("You already have ${CC.YELLOW}$username${CC.RED} added as a friend.")
        }

        hasOutgoingFriendRequestFrom(player.uniqueId, uniqueId).thenAccept {
            if (it)
            {
                player.sendMessage("${CC.RED}You've already sent a friend request to ${CC.YELLOW}$username${CC.RED}, please wait.")
                return@thenAccept
            }

            retrieveFriendRequestStatus(uniqueId).whenComplete { disabled, _ ->
                if (disabled)
                {
                    player.sendMessage("${CC.YELLOW}$username${CC.RED} has their friend requests disabled.")
                    return@whenComplete
                }

                internalHandleFriendRequestDispatch(player, uniqueId, username)
            }
        }
    }

    private fun internalHandleFriendRequestDispatch(player: Player, uniqueId: UUID, username: String)
    {
        val message = FancyMessage()
        message.withMessage(
            "",
            " ${CC.PRI}$username${CC.SEC} sent you a friend request!",
            " ${CC.GREEN}[CLICK TO ACCEPT]",
            ""
        )
        message.andCommandOf(
            ClickEvent.Action.RUN_COMMAND,
            "/friend accept ${player.name}"
        )

        Lemon.instance.banana.useResource {
            val requestKey = "friends:requests:$uniqueId:${player.uniqueId}"

            it.hset(
                requestKey, player.uniqueId.toString(),
                System.currentTimeMillis().toString()
            )
            it.expire(requestKey, TimeUnit.MINUTES.toSeconds(5L))
        }

        QuickAccess.sendGlobalPlayerFancyMessage(
            fancyMessage = message, uuid = uniqueId
        )
    }

    fun attemptFriendRequestAcceptFrom(player: Player, uniqueId: UUID)
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        val username = CubedCacheUtil.fetchName(uniqueId)!!

        if (friendPlayer.friends[uniqueId] != null)
        {
            throw ConditionFailedException("You already have ${CC.YELLOW}$username${CC.RED} added as a friend.")
        }

        hasOutgoingFriendRequestFrom(uniqueId, player.uniqueId).thenAccept {
            if (!it)
            {
                player.sendMessage("${CC.RED}You don't have a friend request from ${CC.YELLOW}$username${CC.RED}.")
                return@thenAccept
            }
        }
    }

    private fun retrieveFriendRequestStatus(uniqueId: UUID): CompletableFuture<Boolean>
    {
        return DataStoreHandler.lemonPlayerLayer.fetchEntryByKey(uniqueId.toString()).thenApply {
            return@thenApply it.getSetting("friend-requests-disabled")
        }
    }

    private fun hasOutgoingFriendRequestFrom(requester: UUID, target: UUID): CompletableFuture<Boolean>
    {
        return CompletableFuture.supplyAsync {
            var exists = false

            Lemon.instance.banana.useResource {
                exists = it.hget("friends:requests:$target:$requester", requester.toString()) != null
            }

            return@supplyAsync exists
        }
    }
}
