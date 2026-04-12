package gg.scala.friends.menu.template.impl

import gg.scala.friends.menu.FriendUIService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
abstract class AbstractSinglePageSubMenu(
    title: String, private val size: Int,
    private val subMenu: Menu? = null
) : Menu(title)
{
    abstract fun configureMenuItems(
        player: Player,
        buttons: MutableMap<Int, Button>
    )

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        configureMenuItems(player, buttons)

        buttons[size - 5] = ItemBuilder
            .of(Material.ARROW)
            .name("${CC.GREEN}Go back")
            .addToLore(
                "${CC.GRAY}Returns to:",
                "${CC.WHITE}${
                    subMenu?.getTitle(player) ?: "Your Profile"
                }"
            )
            .toButton { _, _ ->
                openPreviousMenu(player)
            }

        return buttons
    }

    private fun openPreviousMenu(player: Player)
    {
        if (subMenu != null)
        {
            subMenu.openMenu(player)
            return
        }

        FriendUIService.openProfileMenu(player)
    }

    override fun onClose(
        player: Player, manualClose: Boolean
    )
    {
        if (manualClose)
        {
            Tasks.delayed(1L) {
                openPreviousMenu(player)
            }
        }
    }

    override fun size(buttons: Map<Int, Button>) = size
}
