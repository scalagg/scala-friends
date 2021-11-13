package gg.scala.friends.command

import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.registrar.impl.KPlayerHandler
import gg.scala.friends.result.FriendPaginatedResult
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
@CommandAlias("friend|friends|fr|f")
class FriendCommand : BaseCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("list")
    @Description("View all your friends.")
    fun onList(player: Player, @Optional page: Int?)
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        ForkJoinPool.commonPool().execute {
            FriendPaginatedResult.display(
                sender = player,
                results = friendPlayer.orderedFriends(),
                page = page ?: 1,
                command = "/friend list %s"
            )
        }
    }

    @Default
    @Subcommand("add")
    @Syntax("<player>")
    @CommandCompletion("@all-players")
    @Description("Send a friend request to a player.")
    fun onAdd(player: Player, target: UUID)
    {
        KFriendHandler.attemptDispatchFriendRequestTo(player, target)
    }

    @Syntax("<player>")
    @Subcommand("remove|delete")
    @CommandCompletion("@all-players")
    @Description("Remove a player from your friends list.")
    fun onRemove(player: Player, target: UUID)
    {
        KFriendHandler.attemptFriendRemoval(player, target)
    }

    @Syntax("<player>")
    @Subcommand("accept")
    @CommandCompletion("@all-players")
    @Description("Accept a player's friend request.")
    fun onAccept(player: Player, target: UUID)
    {
        KFriendHandler.attemptFriendRequestAcceptFrom(player, target)
    }
}
