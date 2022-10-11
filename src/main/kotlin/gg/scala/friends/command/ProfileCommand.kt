package gg.scala.friends.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.Optional
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.friends.menu.FriendsMenu
import gg.scala.friends.menu.OtherPlayerProfileMenu
import java.util.*

/**
 * @author GrowlyX
 * @since 7/14/2022
 */
@AutoRegister
class ProfileCommand : ScalaCommand()
{
    @CommandCompletion("@players")
    @CommandAlias("profile")
    fun onProfile(
        player: ScalaPlayer, @Optional target: UUID?
    )
    {
        if (target != null)
        {
            OtherPlayerProfileMenu(target)
                .openMenu(player.bukkit())
            return
        }

        FriendsMenu(FriendsMenu.RenderState.PROFILE)
            .openMenu(player.bukkit())
    }
}
