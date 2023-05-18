package gg.scala.friends.menu.template

import gg.scala.friends.menu.FriendUIService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
abstract class AbstractSinglePageFriendMenu(title: String) : Menu(title)
{
    abstract fun configureMenuItems(
        player: Player,
        buttons: MutableMap<Int, Button>
    )

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        FriendUIService.populate(player, buttons)

        configureMenuItems(player, buttons)
        return buttons
    }

    override fun size(buttons: Map<Int, Button>) = 36
}
