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
import org.bukkit.inventory.ItemFlag

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
class ProfileSinglePageMenu : AbstractSinglePageFriendMenu("Your Profile")
{
    private fun modernButton(material: Material, title: String) = ItemBuilder
        .of(material)
        .name(title)
        .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)

    override fun configureMenuItems(
        player: Player, buttons: MutableMap<Int, Button>
    )
    {
        buttons[11] = modernButton(Material.matchMaterial("COMPARATOR") ?: Material.COMPASS, "${CC.WHITE}Settings")
            .addToLore(
                "${CC.GRAY}Edit network-wide settings",
                "${CC.GRAY}and profile preferences.",
                "",
                "${CC.YELLOW}Click to open!"
            )
            .toButton { _, _ ->
                player.performCommand("settings")
            }

        buttons[13] = modernButton(Material.matchMaterial("SPYGLASS") ?: Material.EYE_OF_ENDER, "${CC.WHITE}Statistics")
            .addToLore(
                "${CC.GRAY}Open a modern breakdown",
                "${CC.GRAY}of your network statistics.",
                "",
                "${CC.YELLOW}Click to open!"
            )
            .toButton { _, _ ->
                GameStatsViewerMenu().openMenu(player)
            }

        buttons[15] = modernButton(Material.matchMaterial("WRITABLE_BOOK") ?: Material.BOOK, "${CC.WHITE}Social Media")
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

        buttons[21] = modernButton(Material.matchMaterial("NETHER_STAR") ?: Material.DIAMOND, "${CC.WHITE}Achievements")
            .addToLore(
                "${CC.GRAY}View status for network-wide",
                "${CC.GRAY}and game-specific achievements!",
                "",
                "${CC.YELLOW}Click to open!"
            )
            .toButton { _, _ ->
                player.performCommand("achievements")
            }

        buttons[23] = modernButton(XMaterial.RED_DYE.parseMaterial() ?: Material.REDSTONE, "${CC.WHITE}Customizer")
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
