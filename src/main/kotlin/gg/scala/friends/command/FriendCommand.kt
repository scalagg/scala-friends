package gg.scala.friends.command

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.result.FriendPaginatedResult
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.acf.annotation.Optional
import gg.scala.friends.menu.FriendsMenu
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
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
    fun onFriends(player: Player)
    {
        FriendsMenu().openMenu(player)
    }

    @HelpCommand
    @Subcommand("help")
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("list")
    @Description("View all your friends.")
    fun onList(player: Player, @Optional page: Int?) =
        FriendListCommand.onList(player, page)

    @Default
    @Subcommand("add")
    @Syntax("<player>")
    @CommandCompletion("@all-players")
    @Description("Send a friend request to a player.")
    fun onAdd(player: Player, target: AsyncLemonPlayer) =
        target.validatePlayers(player, false) {
            KFriendHandler
                .attemptDispatchFriendRequestTo(player, it.uniqueId)
                .join()
        }

    @Syntax("<player>")
    @Subcommand("remove|delete")
    @CommandCompletion("@all-players")
    @Description("Remove a player from your friends list.")
    fun onRemove(player: Player, target: AsyncLemonPlayer) =
        target.validatePlayers(player, false) {
            KFriendHandler.attemptFriendRemoval(player, it.uniqueId)
        }

    @Syntax("<player>")
    @Subcommand("accept")
    @CommandCompletion("@all-players")
    @Description("Accept a player's friend request.")
    fun onAccept(player: Player, target: AsyncLemonPlayer) =
        target.validatePlayers(player, false) {
            KFriendHandler.attemptFriendRequestAcceptFrom(player, it.uniqueId)
        }
}
