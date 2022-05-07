package gg.scala.friends.command

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.result.FriendPaginatedResult
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
@AutoRegister
class ToggleFriendInvitationCommand : ScalaCommand()
{
    @CommandAlias("togglefriendinvitations|togglefriendrequests|togglefriendinvites|tfi|tfr")
    fun onToggleFriendRequests(player: Player)
    {
        val lemonPlayer = PlayerHandler
            .find(player.uniqueId)!!

        val friendsRequests = lemonPlayer
            .getSetting("friend-requests-disabled")

        if (friendsRequests) {
            lemonPlayer remove "friend-requests-disabled"

            player.sendMessage("${CC.GREEN}You've enabled friend invitations.")
        } else {
            lemonPlayer.updateOrAddMetadata(
                "friend-requests-disabled", Metadata(true)
            )

            player.sendMessage("${CC.RED}You've disabled friend invitations.")
        }
    }

    @CommandAlias("fl|flist")
    fun onList(player: Player, @Optional page: Int?)
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        ForkJoinPool.commonPool().execute {
            FriendPaginatedResult.display(
                sender = player,
                results = friendPlayer.orderedFriends(),
                page = page ?: 1,
                command = "friend list %s"
            )
        }
    }
}
