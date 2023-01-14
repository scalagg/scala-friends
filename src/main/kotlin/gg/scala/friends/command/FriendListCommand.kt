package gg.scala.friends.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.result.FriendPaginatedResult
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
@AutoRegister
object FriendListCommand : ScalaCommand()
{
    @CommandAlias("fl|flist")
    fun onList(
        player: Player, @Optional page: Int?
    ): CompletableFuture<Void>
    {
        val friendPlayer = KPlayerHandler.find(player)
            ?: throw RuntimeException()

        return CompletableFuture
            .runAsync {
                FriendPaginatedResult.display(
                    sender = player,
                    results = friendPlayer.orderedFriends(),
                    page = page ?: 1,
                    command = "friend list %s"
                )
            }
    }
}
