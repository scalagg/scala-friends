package gg.scala.friends.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.result.FriendPaginatedResult
import org.bukkit.entity.Player
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
@AutoRegister
class FriendListCommand : ScalaCommand()
{
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
