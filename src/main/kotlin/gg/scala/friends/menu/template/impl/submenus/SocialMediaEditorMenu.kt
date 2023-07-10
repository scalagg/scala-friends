package gg.scala.friends.menu.template.impl.submenus

import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.menu.template.impl.AbstractSinglePageSubMenu
import gg.scala.friends.socials.SocialMediaProvider
import gg.scala.friends.socials.SocialMediaTexturedButton
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import org.bukkit.Sound
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/9/2023
 */
class SocialMediaEditorMenu : AbstractSinglePageSubMenu("Social Media", 27)
{
    override fun configureMenuItems(player: Player, buttons: MutableMap<Int, Button>)
    {
        val friendsPlayer = KPlayerHandler
            .find(player)
            ?: return

        for ((index, media) in SocialMediaProvider.values().withIndex())
        {
            val textured = SocialMediaTexturedButton(media)

            buttons[11 + index] = ItemBuilder
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
                    "${CC.RED}Shift-Click to reset!",
                )
                .toButton { _, type ->
                    if (type!!.isShiftClick)
                    {
                        friendsPlayer.socialMedia.remove(media)
                        friendsPlayer.save()
                        openMenu(player)

                        player.playSound(
                            player.location,
                            Sound.NOTE_BASS,
                            1.0f, 0.7f
                        )
                        return@toButton
                    }

                    player.closeInventory()

                    EzPrompt(
                        promptText = "${CC.GREEN}Enter your handle for: ${CC.WHITE}${media.name}${CC.GREEN}!"
                    ) {
                        if (!media.regex.matches(it))
                        {
                            player.sendMessage("${CC.RED}That is an invalid handle for ${media.name}!")
                            Tasks.sync {
                                openMenu(player)
                            }
                            return@EzPrompt
                        }

                        friendsPlayer.socialMedia[media] = it

                        friendsPlayer
                            .save()
                            .thenRun {
                                player.sendMessage("${CC.GREEN}Your handle has been updated to: ${CC.WHITE}${it}!")

                                player.playSound(
                                    player.location,
                                    Sound.NOTE_BASS,
                                    1.0f, 2.0f
                                )

                                Tasks.sync {
                                    openMenu(player)
                                }
                            }
                    }.start(player)
                }
        }
    }
}
