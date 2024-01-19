package gg.scala.friends.menu.template.impl

import gg.scala.friends.friendship.Friendship
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.menu.template.AbstractMultiPageFriendMenu
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
class FriendsListMultiPageMenu(val friendships: List<Friendship>) : AbstractMultiPageFriendMenu("Your Friends")
{
    override fun configureMenuItems(
        player: Player, buttons: MutableMap<Int, Button>
    )
    {
        val friendsPlayer = KPlayerHandler.find(player)
            ?: return

        for (friendship in friendships)
        {
            val otherEnd = friendship.otherEndOf(player.uniqueId)
            val username = otherEnd.username()

            val bestFriend = friendsPlayer.bestFriends.contains(otherEnd)

            buttons[buttons.size] = ItemBuilder
                .of(Material.SKULL_ITEM)
                .owner(username)
                .name(
                    "${CC.GREEN}$username${
                        if (bestFriend) "${CC.GRAY} (Best Friend)" else ""
                    }"
                )
                .addToLore(
                    "${CC.GRAY}Added on: ${CC.WHITE}${
                        TimeUtil.formatIntoDateString(
                            Date(friendship.startedOn)
                        )
                    }",
                    "",
                )
                .apply {
                    if (
                        !QuickAccess
                            .online(otherEnd)
                            .join()
                        )
                    {
                        addToLore(
                            "${CC.RED}Player offline",
                            "${CC.RED}Last seen: ${CC.WHITE}${
                                TimeUtil.formatIntoAbbreviatedString(
                                    ((System.currentTimeMillis() - (QuickAccess.getLastOnline(otherEnd).join()
                                        ?: System.currentTimeMillis())) / 1000).toInt()
                                )
                            } ago"
                        )
                    } else
                    {
                        addToLore(
                            "${CC.D_GREEN}Player online",
                            "${CC.D_GREEN}Server: ${CC.WHITE}${
                                QuickAccess.server(otherEnd).join()?.id ?: "Private"
                            }"
                        )
                    }
                }
                .apply {
                    addToLore("")

                    if (bestFriend)
                    {
                        addToLore(
                            "${CC.D_GREEN}Left-Click to remove from",
                            "${CC.D_GREEN}best-friends list!"
                        )
                    } else
                    {
                        addToLore(
                            "${CC.GREEN}Left-Click to add to",
                            "${CC.GREEN}best-friends list!"
                        )
                    }

                    addToLore(
                        "${CC.RED}Right-Click to remove!"
                    )
                }
                .data(3)
                .apply {
                    if (bestFriend)
                    {
                        glow()
                    }
                }
                .toButton { _, type ->
                    if (type!!.isRightClick)
                    {
                        player.closeInventory()

                        KFriendHandler
                            .attemptFriendRemoval(
                                player, otherEnd
                            )
                    } else if (type.isLeftClick)
                    {
                        if (bestFriend)
                        {
                            friendsPlayer.bestFriends.remove(otherEnd)
                        } else
                        {
                            friendsPlayer.bestFriends.add(otherEnd)
                        }

                        openMenu(player)
                    }
                }
        }
    }
}
