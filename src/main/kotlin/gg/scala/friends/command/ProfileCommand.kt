package gg.scala.friends.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.friends.menu.FriendsMenu
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/14/2022
 */
@AutoRegister
@CommandAlias("profile|pr|user|myprofile")
class ProfileCommand : ScalaCommand()
{
    @Default
    fun onProfile(player: Player)
    {
        FriendsMenu(FriendsMenu.RenderState.PROFILE)
            .openMenu(player)
    }
}
