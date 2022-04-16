package gg.scala.friends.command

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.result.FriendPaginatedResult
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
@AutoRegister
@CommandAlias("friend|friends|fr|f")
class FriendCommand : ScalaCommand()
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
    fun onAdd(player: Player, target: UUID): CompletableFuture<Void>
    {
        return KFriendHandler.attemptDispatchFriendRequestTo(player, target)
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
