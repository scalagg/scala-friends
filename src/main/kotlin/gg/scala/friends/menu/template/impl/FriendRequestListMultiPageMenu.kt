package gg.scala.friends.menu.template.impl

import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.menu.template.AbstractMultiPageFriendMenu
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
class FriendRequestListMultiPageMenu : AbstractMultiPageFriendMenu("Your Friend Requests")
{
    override fun configureMenuItems(
        player: Player, buttons: MutableMap<Int, Button>
    )
    {
        val outgoing = KFriendHandler
            .getOutgoingFriendRequests(
                player.uniqueId
            )

        for (request in outgoing)
        {
            val uniqueId = UUID.fromString(request.key)

            val username = CubedCacheUtil
                .fetchName(uniqueId)
                ?: continue

            val requestTime = request.value

            buttons[buttons.size] = ItemBuilder
                .of(Material.SKULL_ITEM)
                .owner(username)
                .name("${CC.YELLOW}$username")
                .addToLore(
                    "${CC.GRAY}Time left:",
                    "${CC.WHITE}${
                        DurationFormatUtils
                            .formatDurationWords(
                                requestTime - System.currentTimeMillis(),
                                true, true
                            )
                    }",
                    "",
                    "${CC.RED}Right-Click to cancel!"
                )
                .data(3)
                .toButton { _, type ->
                    if (type!!.name.contains("RIGHT"))
                    {
                        KFriendHandler.cacheConn.apply {
                            sync().hdel("friends:requests:outgoing:${player.uniqueId}", uniqueId.toString())
                            sync().hdel("friends:requests:incoming:${uniqueId}", player.uniqueId.toString())
                        }

                        player.sendMessage(
                            "${CC.GREEN}Cancelled friend request to $username."
                        )
                    }
                }
        }
    }
}
