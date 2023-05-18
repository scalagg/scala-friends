package gg.scala.friends.menu.template.impl

import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.menu.template.AbstractMultiPageFriendMenu
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
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
class FriendsListMultiPageMenu : AbstractMultiPageFriendMenu("Your Friends")
{
    override fun configureMenuItems(
        player: Player, buttons: MutableMap<Int, Button>
    )
    {
        val friendsPlayer = KPlayerHandler
            .find(player)!!

        val descending = friendsPlayer.friends.entries
            .sortedByDescending {
                if (friendsPlayer.bestFriends.contains(it.key))
                    1 else 0
            }

        for (friend in descending)
        {
            val username = CubedCacheUtil
                .fetchName(friend.key)!!

            val bestFriend = friendsPlayer
                .bestFriends.contains(friend.key)

            buttons[buttons.size] = ItemBuilder
                .of(Material.SKULL_ITEM)
                .owner(username)
                .name(
                    "${CC.YELLOW}$username${
                        if (bestFriend) "${CC.GRAY} (Best Friend)" else ""
                    }"
                )
                .addToLore(
                    "${CC.GRAY}Added on: ${CC.WHITE}${
                        TimeUtil.formatIntoDateString(
                            Date(friend.value)
                        )
                    }",
                    "",
                    "${CC.GRAY}Online: ${
                        if (QuickAccess.online(friend.key).join())
                            "${CC.GREEN}Yes" else "${CC.RED}No"
                    }",
                    "${CC.GRAY}Last seen: ${
                        TimeUtil.formatIntoDateString(
                            Date(
                                QuickAccess.getLastOnline(friend.key).join() ?: System.currentTimeMillis()
                            )
                        )
                    }",
                    ""
                )
                .apply {
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
                                player, friend.key
                            )
                    } else if (type.isLeftClick)
                    {
                        if (bestFriend)
                        {
                            friendsPlayer.bestFriends.remove(friend.key)
                        } else
                        {
                            friendsPlayer.bestFriends.add(friend.key)
                        }
                    }
                }
        }
    }
}
