package gg.scala.friends.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.friends.FriendsSpigotPlugin
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.player.KFriendsPlayer
import gg.scala.friends.socials.SocialMediaProvider
import gg.scala.friends.socials.SocialMediaTexturedButton
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.time.TimeUtil
import net.wesjd.anvilgui.AnvilGUI
import org.apache.commons.lang3.time.DurationFormatUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * @author GrowlyX
 * @since 7/3/2022
 */
class FriendsMenu(
    defaultState: RenderState = RenderState.FRIENDS
) : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val SLOTS by lazy {
            val slots = mutableListOf<Int>()
            slots += listOf(2, 3, 4, 5, 6, 7)
            slots += listOf(2, 3, 4, 5, 6, 7).map { it + 9 }
            slots += listOf(2, 3, 4, 5, 6, 7).map { it + 18 }
            slots += listOf(2, 3, 4, 5, 6, 7).map { it + 27 }

            slots
        }

        @JvmStatic
        val PLACEHOLDERS = listOf(
            1, 10, 19, 19 + 9, 17, 17 + 9
        )

        @JvmStatic
        private val plugin = JavaPlugin
            .getPlugin(FriendsSpigotPlugin::class.java)
    }

    enum class RenderState
    {
        REQUESTS, FRIENDS, PROFILE
    }

    private var render = defaultState

    init
    {
        autoUpdate = true
        applyHeader = false
        updateAfterClick = true
    }

    override fun getAllPagesButtonSlots() = SLOTS
    override fun size(buttons: Map<Int, Button>) = 36

    override fun getPageButtonSlots() = 8 to 26 + 9
    override fun getPrePaginatedTitle(player: Player) =
        if (render == RenderState.PROFILE)
            "Your Profile" else "Profile ${Constants.DOUBLE_ARROW_RIGHT} Viewing ${
            this.render.name.lowercase()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }"

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        val lemonPlayer = PlayerHandler
            .find(player.uniqueId)!!

        buttons[0] = ItemBuilder
            .of(Material.SKULL_ITEM)
            .owner(player.name)
            .data(3)
            .name("${CC.YELLOW}Your Profile")
            .addToLore(
                "${CC.GRAY}Rank: ${
                    lemonPlayer.activeGrant!!.getRank().getColoredName()
                }",
                "${CC.GRAY}Clan: ${CC.WHITE}N/A",
                "",
                "${CC.GRAY}First joined:",
                "${CC.WHITE}${
                    TimeUtil.formatIntoFullCalendarString(
                        Date(
                            lemonPlayer.getMetadata("first-connection")
                                ?.asString()?.toLong() ?: System.currentTimeMillis()
                        )
                    )
                }",
                "",
                "${CC.GRAY}Last seen:",
                "${CC.WHITE}${
                    TimeUtil.formatIntoFullCalendarString(
                        Date(
                            lemonPlayer.getMetadata("last-connection")
                                ?.asString()?.toLong() ?: System.currentTimeMillis()
                        )
                    )
                }",
                "${CC.GRAY}Last seen on: ${CC.WHITE}${
                    lemonPlayer.getMetadata("last-connected-server-id")?.asString() ?: "N/A"
                }"
            )
            .toButton { _, _ ->
                this.render = RenderState.PROFILE
            }

        buttons[9] = ItemBuilder
            .of(XMaterial.OAK_SIGN)
            .name("${CC.YELLOW}Your Friends")
            .addToLore(
                "${CC.GRAY}View your friends list."
            )
            .toButton { _, _ ->
                this.render = RenderState.FRIENDS
            }

        buttons[18] = ItemBuilder
            .of(XMaterial.HOPPER)
            .name("${CC.YELLOW}Your Friend Requests")
            .addToLore(
                "${CC.GRAY}View your friends requests."
            )
            .toButton { _, _ ->
                this.render = RenderState.REQUESTS
            }

        val greenPlusIcon = object : TexturedHeadButton(Constants.GREEN_PLUS_TEXTURE)
        {}
            .getButtonItem(player)

        buttons[27] = ItemBuilder
            .copyOf(greenPlusIcon)
            .name("${CC.YELLOW}Add a Friend")
            .addToLore(
                "${CC.GRAY}Send a friend request to a user."
            )
            .toButton { _, _ ->
                AnvilGUI.Builder()
                    .onClose {
                        Tasks.delayed(1L) {
                            FriendsMenu(render)
                                .openMenu(player)
                        }
                    }
                    .onComplete { _, u ->
                        val uniqueId = CubedCacheUtil
                            .fetchUuid(u)
                            ?: return@onComplete kotlin.run {
                                player.sendMessage("${CC.RED}Invalid player!")
                                AnvilGUI.Response.close()
                            }

                        KFriendHandler
                            .attemptDispatchFriendRequestTo(
                                player, uniqueId
                            )
                            .exceptionally {
                                if (it.message!!.startsWith(
                                        "gg.scala.commons.acf.ConditionFailedException: "
                                    )
                                )
                                {
                                    player.sendMessage(
                                        it.message!!.removePrefix(
                                            "gg.scala.commons.acf.ConditionFailedException: "
                                        )
                                    )
                                }

                                return@exceptionally null
                            }

                        return@onComplete AnvilGUI.Response.close()
                    }
                    .text("Enter a username...")
                    .plugin(plugin)
                    .open(player)
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

        when (render)
        {
            RenderState.FRIENDS ->
                configureFriendsList(buttons, player, friendsPlayer)

            RenderState.REQUESTS ->
                configureFriendRequests(buttons, player)

            RenderState.PROFILE ->
                configureProfile(buttons, player)
        }

        return buttons
    }

    private fun configureProfile(
        buttons: MutableMap<Int, Button>,
        player: Player
    )
    {
        val friendsPlayer = KPlayerHandler
            .find(player)
            ?: return


        for (media in SocialMediaProvider.values())
        {
            val textured = SocialMediaTexturedButton(media)

            buttons[buttons.size] = ItemBuilder
                .copyOf(
                    textured.getButtonItem(player)
                )
                .name("${media.color}${media.name}")
                .addToLore(
                    "${CC.GRAY}Handle:",
                    "${CC.WHITE}${
                        friendsPlayer.media(media) ?: "N/A"
                    }",
                    "",
                    "${CC.GRAY}Example:",
                    "${media.color}${media.example}",
                    "",
                    "${CC.GREEN}Click to update!"
                )
                .toButton()
        }
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
