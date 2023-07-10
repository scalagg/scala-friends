package gg.scala.friends.menu.template.impl

import com.cryptomorin.xseries.XMaterial
import gg.scala.friends.menu.template.AbstractSinglePageFriendMenu
import gg.scala.friends.menu.template.impl.submenus.GameStatsViewerMenu
import gg.scala.friends.menu.template.impl.submenus.SocialMediaEditorMenu
import gg.scala.lemon.LemonConstants
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
class ProfileSinglePageMenu : AbstractSinglePageFriendMenu("Your Profile")
{
    override fun configureMenuItems(
        player: Player, buttons: MutableMap<Int, Button>
    )
    {
        buttons[12] = ItemBuilder
            .of(
                Material.REDSTONE_COMPARATOR
            )
            .name("${CC.GREEN}Settings")
            .addToLore(
                "${CC.GRAY}Edit network-wide settings",
                "${CC.GRAY}and options on your profile:",
                "",
                "${CC.YELLOW}Click to open!"
            )
            .toButton { _, _ ->
                player.performCommand("settings")
            }

        buttons[13] = ItemBuilder
            .of(
                Material.EYE_OF_ENDER
            )
            .name("${CC.GREEN}Stats Viewer")
            .addToLore(
                "${CC.GRAY}Showcases statistics for all",
                "${CC.GRAY}of our gamemodes.",
                "",
                "${CC.YELLOW}Click to open!"
            )
            .toButton { _, _ ->
                GameStatsViewerMenu().openMenu(player)
            }

        buttons[14] = ItemBuilder
            .of(
                Material.BOOK_AND_QUILL
            )
            .name("${CC.GREEN}Social Media")
            .addToLore(
                "${CC.GRAY}Edit your social media",
                "${CC.GRAY}handles for the platforms:",
                "${CC.WHITE}- Instagram",
                "${CC.WHITE}- Discord",
                "${CC.WHITE}- Telegram",
                "${CC.WHITE}- and more...",
                "",
                "${CC.YELLOW}Click to open!"
            )
            .toButton { _, _ ->
                SocialMediaEditorMenu().openMenu(player)
            }

        buttons[15] = ItemBuilder
            .of(
                Material.DIAMOND
            )
            .name("${CC.GREEN}Achievements")
            .addToLore(
                "${CC.GRAY}View status for network-wide",
                "${CC.GRAY}and game-specific achievements!",
                "",
                "${CC.YELLOW}Click to open!"
            )
            .toButton { _, _ ->
                player.performCommand("achievements")
            }

        buttons[16] = ItemBuilder
            .of(
                XMaterial.RED_DYE
            )
            .name("${CC.GREEN}Customizer")
            .addToLore(
                "${CC.GRAY}Customize the appearance",
                "${CC.GRAY}of your network profile:",
                "${CC.WHITE}- Name color",
                "${CC.WHITE}- Chat tags",
                "${CC.WHITE}- Death messages",
                "${CC.WHITE}- Kill effects",
                "${CC.WHITE}- Float particles",
                "${CC.WHITE}- and more...",
                "",
                "${CC.RED}Only available to players",
                "${CC.RED}with rank ${CC.GOLD}Pro${CC.RED} and up!",
                "",
                "${CC.YELLOW}Click to open!"
            )
            .toButton { _, _ ->
                if (!player.hasPermission("profile.customize"))
                {
                    player.sendMessage(
                        "${CC.RED}Buy a rank at ${CC.GOLD}${
                            LemonConstants.STORE_LINK
                        }${CC.RED} to get access to this menu!"
                    )
                    return@toButton
                }

                player.closeInventory()
                player.performCommand("customizer")
            }

    }
}
