package gg.scala.friends.menu

import com.cryptomorin.xseries.XMaterial
import gg.scala.cookie.Cookie
import gg.scala.friends.FriendsSpigotPlugin
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.socials.SocialMediaProvider
import gg.scala.friends.socials.SocialMediaTexturedButton
import gg.scala.friends.textured
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.EzPrompt
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
        val SLOTS = 18..44

        @JvmStatic
        val PLACEHOLDERS = 9..17

        @JvmStatic
        private val plugin = JavaPlugin
            .getPlugin(FriendsSpigotPlugin::class.java)
    }

    enum class RenderState(
        val color: Int
    )
    {
        REQUESTS(color = 4),
        FRIENDS(color = 5),
        PROFILE(color = 1),
        SETTINGS(color = 3)
    }

    private var render = defaultState

    init
    {
        autoUpdate = true
        applyHeader = false
        updateAfterClick = true
    }

    override fun getAllPagesButtonSlots() = SLOTS.toList()
    override fun size(buttons: Map<Int, Button>) = 45

    override fun getPageButtonSlots() = 9 to 17
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

        if (render == RenderState.PROFILE)
        {
            configureProfileExtensive(buttons, player)
        }

        buttons[2] = ItemBuilder
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
                /**
                "${CC.GRAY}First joined:",
                "${CC.WHITE}${
                    TimeUtil.formatIntoFullCalendarString(
                        Date(
                            lemonPlayer.getMetadata("first-connection")
                                ?.asLong() ?: System.currentTimeMillis()
                        )
                    )
                }",
                "",
                "${CC.GRAY}Last seen:",
                "${CC.WHITE}${
                    TimeUtil.formatIntoFullCalendarString(
                        Date(
                            lemonPlayer.getMetadata("last-connection")
                                ?.asLong() ?: System.currentTimeMillis()
                        )
                    )
                }",
                **/
                "${CC.GRAY}Last seen on: ${CC.WHITE}${
                    lemonPlayer.getMetadata("last-connected-server-id")?.asString() ?: "N/A"
                }"
            )
            .toButton { _, _ ->
                this.render(player, RenderState.PROFILE)
            }

        buttons[3] = ItemBuilder
            .copyOf(
                player.textured(Constants.BELL_ICON)
            )
            .name("${CC.YELLOW}Your Preferences")
            .addToLore(
                "${CC.GRAY}View your preferences."
            )
            .toButton { _, _ ->
                this.render(player, RenderState.SETTINGS)
            }

        buttons[4] = ItemBuilder
            .of(XMaterial.OAK_SIGN)
            .name("${CC.YELLOW}Your Friends")
            .addToLore(
                "${CC.GRAY}View your friends list."
            )
            .toButton { _, _ ->
                this.render(player, RenderState.FRIENDS)
            }

        buttons[5] = ItemBuilder
            .of(XMaterial.HOPPER)
            .name("${CC.YELLOW}Your Friend Requests")
            .addToLore(
                "${CC.GRAY}View your friends requests."
            )
            .toButton { _, _ ->
                this.render(player, RenderState.REQUESTS)
            }

        val greenPlusIcon = player.textured(Constants.GREEN_PLUS_TEXTURE)

        buttons[6] = ItemBuilder
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

        val placeholderButton = ItemBuilder
            .copyOf(
                PLACEHOLDER.getButtonItem(player)
            )
            .data(render.color.toShort())
            .toButton()

        for (placeholder in PLACEHOLDERS)
        {
            buttons[placeholder] = placeholderButton
        }

        return buttons
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        when (render)
        {
            RenderState.FRIENDS ->
                configureFriendsList(buttons, player)

            RenderState.REQUESTS ->
                configureFriendRequests(buttons, player)

            RenderState.PROFILE ->
                configureProfile(buttons, player)

            RenderState.SETTINGS ->
                configureSettings(buttons, player)
        }

        return buttons
    }

    private fun render(
        player: Player,
        state: RenderState
    )
    {
        if (this.page != 1)
        {
            this.modPage(player, -(this.page - 1))
        }

        this.render = state
    }

    private fun configureSettings(
        buttons: MutableMap<Int, Button>,
        player: Player
    )
    {
        Cookie.instance.playerSettings
            .filter {
                it.hasPermission(player)
            }
            .flatMap {
                it.getButtons(player).values
            }
            .forEach {
                buttons[buttons.size] = it
            }
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
                .name("${CC.B_YELLOW}${media.name}")
                .addToLore(
                    "${CC.GRAY}Handle:",
                    "${CC.WHITE}${
                        friendsPlayer.media(media) ?: "N/A"
                    }",
                    "",
                    "${CC.GRAY}Example:",
                    "${media.color}${media.example}",
                    "",
                    "${CC.GREEN}Left-Click to update!",
                    "${CC.RED}Right-Click to reset!",
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

                        friendsPlayer.save().thenRun {
                            player.sendMessage("${CC.GREEN}Your handle has been updated to: ${CC.WHITE}${it}!")

                            Tasks.sync {
                                openMenu(player)
                            }
                        }
                    }.start(player)
                }
        }

        buttons[buttons.size] = ItemBuilder.of(Material.AIR).toButton()

        buttons[buttons.size] = ItemBuilder
            .copyOf(
                player.textured(Constants.TROPHY_ICON)
            )
            .name("${CC.B_YELLOW}Achievements")
            .addToLore("${CC.GRAY}Coming soon!")
            .toButton()

        buttons[buttons.size] = ItemBuilder
            .copyOf(
                player.textured(Constants.SUNNY_SKY_ICON)
            )
            .name("${CC.B_YELLOW}Played Games")
            .addToLore(
                "${CC.GRAY}Most recent previously",
                "${CC.GRAY}played games.",
                "",
                "${CC.GREEN}Click to view!"
            )
            .toButton { _, _ ->
                player.chat("/games")
            }

        buttons[buttons.size] = ItemBuilder
            .copyOf(
                player.textured(Constants.GMAIL_ICON)
            )
            .name("${CC.B_YELLOW}Notifications")
            .addToLore(
                "${CC.GRAY}All unread notifications.",
                "",
                "${CC.GREEN}Click to view!"
            )
            .toButton { _, _ ->
                player.chat("/notifications")
            }

        buttons[buttons.size] = ItemBuilder.of(Material.AIR).toButton()
        buttons[buttons.size] = ItemBuilder.of(Material.AIR).toButton()
        buttons[buttons.size] = ItemBuilder.of(Material.AIR).toButton()

        buttons[buttons.size] = ItemBuilder
            .copyOf(
                player.textured(Constants.GLOBE_ICON)
            )
            .name("${CC.B_YELLOW}Languages")
            .addToLore(
                "${CC.GRAY}Select your language:",
                " ${CC.WHITE}- English",
                " ${CC.WHITE}- Español",
                "",
                "${CC.GREEN}Click to edit!"
            )
            .toButton { _, _ ->
                player.sendMessage("${CC.RED}This feature is temporarily disabled!")
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

    private fun configureProfileExtensive(
        buttons: MutableMap<Int, Button>,
        player: Player
    )
    {
        for (supplier in plugin.suppliers)
        {
            val stats = supplier
                .cache[player.uniqueId]

            if (stats == null)
            {
                buttons[30 + buttons.size] =
                    ItemBuilder
                        .of(supplier.gameModeIcon())
                        .name("${CC.B_YELLOW}${supplier.gameModeName()}")
                        .addToLore(
                            "${CC.RED}Your statistics for ${supplier.gameModeName()}",
                            "${CC.RED}have not yet loaded.",
                            "",
                            "${CC.RED}Please wait a moment${plugin.loadingString}"
                        )
                        .toButton()
                continue
            }

            if (!stats.loggedIn)
            {
                buttons[30 + buttons.size] =
                    ItemBuilder
                        .of(supplier.gameModeIcon())
                        .name("${CC.B_YELLOW}${supplier.gameModeName()}")
                        .addToLore(
                            "${CC.RED}You have not yet joined",
                            "${CC.RED}our ${supplier.gameModeName()} server."
                        )
                        .toButton()
                continue
            }

            buttons[30 + buttons.size] =
                ItemBuilder
                    .of(supplier.gameModeIcon())
                    .name("${CC.B_YELLOW}${supplier.gameModeName()}")
                    .setLore(
                        stats.render ?: listOf(
                            "${CC.RED}Something went wrong. Contact an admin."
                        )
                    )
                    .toButton()
        }
    }

    private fun configureFriendsList(
        buttons: MutableMap<Int, Button>,
        player: Player
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
