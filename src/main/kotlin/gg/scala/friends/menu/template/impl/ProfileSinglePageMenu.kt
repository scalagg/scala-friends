package gg.scala.friends.menu.template.impl

import gg.scala.friends.handler.KPlayerHandler
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
    }
}
