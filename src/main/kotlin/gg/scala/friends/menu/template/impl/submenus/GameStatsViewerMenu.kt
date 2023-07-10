package gg.scala.friends.menu.template.impl.submenus

import gg.scala.friends.menu.FriendUIService
import gg.scala.friends.menu.template.impl.AbstractSinglePageSubMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/9/2023
 */
class GameStatsViewerMenu : AbstractSinglePageSubMenu("Game Statistics", 27)
{
    override fun configureMenuItems(player: Player, buttons: MutableMap<Int, Button>)
    {
        for ((index, supplier) in FriendUIService.plugin.suppliers.withIndex())
        {
            val stats = supplier
                .cache[player.uniqueId]

            if (stats == null)
            {
                buttons[12 + index] =
                    ItemBuilder
                        .of(supplier.gameModeIcon())
                        .name("${CC.RED}${supplier.gameModeName()}")
                        .addToLore(
                            "${CC.GRAY}Your statistics for ${supplier.gameModeName()}",
                            "${CC.GRAY}have not yet loaded.",
                            "",
                            "${CC.RED}Please wait a moment${FriendUIService.plugin.loadingString}"
                        )
                        .toButton()
                continue
            }

            if (!stats.loggedIn)
            {
                buttons[12 + index] =
                    ItemBuilder
                        .of(supplier.gameModeIcon())
                        .name("${CC.GREEN}${supplier.gameModeName()}")
                        .addToLore(
                            "${CC.GRAY}You haven't joined our",
                            "${CC.GRAY}${supplier.gameModeName().lowercase()} server yet!"
                        )
                        .toButton()
                continue
            }

            buttons[12 + index] =
                ItemBuilder
                    .of(supplier.gameModeIcon())
                    .name("${CC.GREEN}${supplier.gameModeName()}")
                    .setLore(
                        stats.render ?: listOf(
                            "${CC.RED}Something went wrong. Contact an admin."
                        )
                    )
                    .toButton()
        }
    }
}
