package gg.scala.friends.menu

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import gg.scala.friends.FriendsSpigotPlugin
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.menu.template.impl.FriendRequestListMultiPageMenu
import gg.scala.friends.menu.template.impl.FriendsListMultiPageMenu
import gg.scala.friends.menu.template.impl.ProfileSinglePageMenu
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.time.DateUtil
import net.evilblock.cubed.util.time.TimeUtil
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
@Service
object FriendUIService
{
    @Inject
    lateinit var plugin: FriendsSpigotPlugin

    var initialLorePipe = { player: Player ->
        listOf(
            "${CC.GRAY}Level: ${CC.GOLD}0",
            "${CC.GRAY}Experience: ${CC.GREEN}0"
        )
    }

    private val buttonMappings = mutableListOf(
        { player: Player, profile: LemonPlayer ->
            ItemBuilder
                .of(Material.EMERALD)
                .name("${CC.GREEN}Your Profile")
                .setLore(initialLorePipe(player))
                .addToLore(
                    "",
                    "${CC.GRAY}Playtime: ${CC.WHITE}${
                        TimeUtil.formatIntoAbbreviatedString(
                            (profile.pastLogins.values.sum() / 1000).toInt()
                        )
                    }",
                    "${CC.GRAY}Rank: ${CC.GREEN}${
                        profile.activeGrant?.getRank()?.getColoredName()
                    }",
                    "",
                    "${CC.GRAY}First login:",
                    "${CC.WHITE}${
                        DateUtil.formatSimplifiedDateDiff(
                            profile.getMetadata("first-connection")
                                ?.asString()?.toLong() ?: System.currentTimeMillis()
                        )
                    } ago"
                )
                .toButton { player, _ ->
                    ProfileSinglePageMenu()
                        .openMenu(player!!)
                }
        },
        { _: Player, _: LemonPlayer ->
            ItemBuilder
                .of(Material.FIREBALL)
                .name("${CC.GOLD}Your Friends")
                .addToLore(
                    "${CC.GRAY}View all of your added",
                    "${CC.GRAY}friends!"
                )
                .toButton { player, _ ->
                    KFriendHandler
                        .getAllFriendshipsOf(player!!.uniqueId)
                        .thenApply(::FriendsListMultiPageMenu)
                        .thenAccept { menu ->
                            menu.openMenu(player)
                        }
                }
        },
        { _: Player, _: LemonPlayer ->
            ItemBuilder
                .of(Material.NETHER_STAR)
                .name("${CC.WHITE}Your Friend Requests")
                .addToLore(
                    "${CC.GRAY}View all of your outgoing",
                    "${CC.GRAY}and incoming friend requests!"
                )
                .toButton { player, _ ->
                    FriendRequestListMultiPageMenu().openMenu(player!!)
                }
        },
        { _: Player, _: LemonPlayer ->
            ItemBuilder
                .of(Material.ANVIL)
                .name("${CC.D_GRAY}Send Friend Request")
                .addToLore(
                    "${CC.GRAY}Send out a friend request",
                    "${CC.GRAY}to a user on the network!"
                )
                .toButton { player, _ ->
                    AnvilGUI.Builder()
                        .onClose {
                            Tasks.delayed(1L) {
                                ProfileSinglePageMenu().openMenu(player!!)
                            }
                        }
                        .onComplete { _, u ->
                            val uniqueId = CubedCacheUtil
                                .fetchUuid(u)
                                ?: return@onComplete kotlin.run {
                                    player!!.sendMessage("${CC.RED}Invalid player!")
                                    AnvilGUI.Response.close()
                                }

                            KFriendHandler
                                .attemptDispatchFriendRequestTo(
                                    player!!, uniqueId
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
        }
    )

    fun updateButtonMapping(
        index: Int,
        lambda: (Player, LemonPlayer) -> Button
    ) = buttonMappings
        .apply {
            this[index] = lambda
        }

    fun populate(
        player: Player,
        buttons: MutableMap<Int, Button>
    )
    {
        val profile = PlayerHandler
            .find(player.uniqueId)
            ?: return

        for (i in 0..3)
        {
            buttons[1 + (i * 9)] =
                PaginatedMenu.PLACEHOLDER

            buttons[i * 9] =
                buttonMappings[i](player, profile)
        }
    }
}
