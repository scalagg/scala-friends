package gg.scala.friends.menu.template.impl

import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.menu.FriendUIService
import gg.scala.friends.menu.template.AbstractSinglePageFriendMenu
import gg.scala.friends.socials.SocialMediaProvider
import gg.scala.friends.socials.SocialMediaTexturedButton
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
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
        val friendsPlayer = KPlayerHandler
            .find(player)
            ?: return

        for ((index, media) in SocialMediaProvider.values().withIndex())
        {
            val textured = SocialMediaTexturedButton(media)

            buttons[12 + index] = ItemBuilder
                .copyOf(
                    textured.getButtonItem(player)
                )
                .name("${CC.GREEN}${media.name}")
                .addToLore(
                    "${CC.GRAY}Handle:",
                    "${CC.WHITE}${
                        friendsPlayer.media(media) ?: "N/A"
                    }",
                    "",
                    "${CC.GRAY}Example:",
                    "${media.color}${media.example}",
                    "",
                    "${CC.YELLOW}Left-Click to update!",
                    "${CC.D_GRAY}Right-Click to reset!",
                )
                .toButton { _, type ->
                    if (type!!.isRightClick)
                    {
                        friendsPlayer.socialMedia.remove(media)
                        player.sendMessage("${CC.GREEN}Your handle has been reset!")
                        return@toButton
                    }

                    player.closeInventory()

                    EzPrompt(
                        promptText = "${CC.GREEN}Please enter your ${CC.YELLOW}${media.name}${CC.GREEN} handle."
                    ) {
                        if (!media.regex.matches(it))
                        {
                            player.sendMessage("${CC.RED}That is an invalid handle for ${media.name}.")
                            return@EzPrompt
                        }

                        friendsPlayer.socialMedia[media] = it

                        friendsPlayer
                            .save()
                            .thenRun {
                                player.sendMessage("${CC.GREEN}Your handle has been updated to: ${CC.WHITE}${it}!")

                                Tasks.sync {
                                    openMenu(player)
                                }
                            }
                    }.start(player)
                }
        }

        for ((index, supplier) in FriendUIService.plugin.suppliers.withIndex())
        {
            val stats = supplier
                .cache[player.uniqueId]

            if (stats == null)
            {
                buttons[22 + index] =
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
                buttons[22 + index] =
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

            buttons[22 + index] =
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
