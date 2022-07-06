package gg.scala.friends.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.player.KFriendsPlayer
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
import net.evilblock.cubed.util.bukkit.prompt.PlayerPrompt
import net.evilblock.cubed.util.bukkit.prompt.PromptUtil
import net.evilblock.cubed.util.time.TimeUtil
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 7/3/2022
 */
class FriendsMenu : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val SLOTS by lazy {
            val slots = mutableListOf<Int>()
            slots += listOf(2, 3, 4, 5, 6, 7)
            slots += listOf(2, 3, 4, 5, 6, 7).map { it + 9 }
            slots += listOf(2, 3, 4, 5, 6, 7).map { it + 18 }

            slots
        }

        @JvmStatic
        val PLACEHOLDERS = listOf(
            1, 10, 19, 8 + 9
        )
    }

    enum class MenuState
    {
        REQUESTS, FRIENDS
    }

    private var state = MenuState.FRIENDS

    init
    {
        autoUpdate = true
        applyHeader = false
        updateAfterClick = true
    }

    override fun getAllPagesButtonSlots() = SLOTS

    override fun getPageButtonSlots() = 8 to 26
    override fun getPrePaginatedTitle(player: Player) =
        "Friends ${Constants.DOUBLE_ARROW_RIGHT} Viewing ${
            this.state.name.lowercase().capitalize()
        }"

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        buttons[0] = ItemBuilder
            .of(XMaterial.OAK_SIGN)
            .name("${CC.YELLOW}Your Friends")
            .addToLore(
                "${CC.GRAY}View your friends list."
            )
            .toButton { _, _ ->
                this.state = MenuState.FRIENDS
            }

        buttons[9] = ItemBuilder
            .of(XMaterial.HOPPER)
            .name("${CC.YELLOW}Your Friend Requests")
            .addToLore(
                "${CC.GRAY}View your friends requests."
            )
            .toButton { _, _ ->
                this.state = MenuState.REQUESTS
            }

        buttons[18] = ItemBuilder
            .of(XMaterial.COMPASS)
            .name("${CC.YELLOW}Add a Friend")
            .addToLore(
                "${CC.GRAY}Send a friend request to a user."
            )
            .toButton { _, _ ->
                player.closeInventory()

                EzPrompt(
                    promptText = "${CC.GREEN}Please enter a username.",
                    complete = {
                        val uniqueId = CubedCacheUtil
                            .fetchUuid(it)
                            ?: return@EzPrompt player
                                .sendMessage("Not a valid player.")

                        KFriendHandler
                            .attemptDispatchFriendRequestTo(
                                player, uniqueId
                            )
                    }
                ).start(player)
            }


        for (placeholder in PLACEHOLDERS)
        {
            buttons[placeholder] = PLACEHOLDER
        }

        return buttons
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        val friendsPlayer = KPlayerHandler
            .find(player) ?: return buttons

        when (state)
        {
            MenuState.FRIENDS ->
                configureFriendsList(buttons, player, friendsPlayer)
            MenuState.REQUESTS ->
                configureFriendRequests(buttons, player)
        }

        return buttons
    }

    private fun configureFriendRequests(
        buttons: MutableMap<Int, Button>,
        player: Player
    )
    {
        val outgoing = KFriendHandler
            .getOutgoingFriendRequests(
                player.uniqueId
            )

        for (request in outgoing)
        {
            val username = CubedCacheUtil
                .fetchName(request.second)!!

            val key = request.first

            val requestTime = KFriendHandler.cacheConn
                .sync().hget(
                    key, player.uniqueId.toString()
                )
                .toLong()

            buttons[buttons.size] = ItemBuilder
                .of(Material.SKULL_ITEM)
                .owner(username)
                .name("${CC.YELLOW}$username")
                .addToLore(
                    "${CC.GRAY}Requested:",
                    "${CC.WHITE}${
                        DurationFormatUtils
                            .formatDurationWords(
                                System.currentTimeMillis() - requestTime,
                                true, true
                            )
                    } ago",
                    "",
                    "${CC.RED}Right-Click to cancel!"
                )
                .data(3)
                .toButton { _, type ->
                    if (type!!.name.contains("RIGHT"))
                    {
                        KFriendHandler.cacheConn
                            .sync().hdel(
                                key, player.uniqueId.toString()
                            )

                        player.sendMessage(
                            "${CC.GREEN}Cancelled friend request to $username."
                        )
                    }
                }
        }
    }

    private fun configureFriendsList(
        buttons: MutableMap<Int, Button>,
        player: Player,
        friendsPlayer: KFriendsPlayer
    )
    {
        for (friend in friendsPlayer.friends)
        {
            val username = CubedCacheUtil
                .fetchName(friend.key)!!

            buttons[buttons.size] = ItemBuilder
                .of(Material.SKULL_ITEM)
                .owner(username)
                .name("${CC.YELLOW}$username")
                .addToLore(
                    "${CC.GRAY}Added on: ${CC.WHITE}${
                        TimeUtil.formatIntoDateString(
                            Date(friend.value)
                        )
                    }",
                    "",
                    "${CC.RED}Right-Click to remove!"
                )
                .data(3)
                .toButton { _, type ->
                    if (type!!.name.contains("RIGHT"))
                    {
                        player.closeInventory()

                        KFriendHandler
                            .attemptFriendRemoval(
                                player, friend.key
                            )
                    }
                }
        }
    }
}
