package gg.scala.friends.settings

import gg.scala.basics.plugin.profile.BasicsProfileService
import gg.scala.basics.plugin.settings.SettingCategory
import gg.scala.basics.plugin.settings.SettingContainer.buildEntry
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.friends.FriendsSpigotPlugin
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 2/27/2022
 */
@Service
@IgnoreAutoScan
@SoftDependency("ScBasics")
object FriendSettingsCategory : SettingCategory
{
    @Inject
    lateinit var plugin: FriendsSpigotPlugin

    @Configure
    fun configure()
    {
        plugin.commandManager
            .registerCommand(object : ScalaCommand()
            {
                @CommandAlias(
                    "togglefriendinvitations|togglefriendrequests|togglefriendinvites|tfi|tfr"
                )
                fun onToggleFriendRequests(player: ScalaPlayer)
                {
                    val profile = BasicsProfileService.find(player.bukkit())
                        ?: throw ConditionFailedException(
                            "Sorry, your profile did not load properly."
                        )

                    val messagesRef = profile.settings["friend_requests"]!!
                    val mapped = messagesRef.map<StateSettingValue>()

                    if (mapped == StateSettingValue.ENABLED)
                    {
                        messagesRef.value = "DISABLED"

                        player.sendMessage(
                            "${CC.RED}You're no longer receiving friend requests."
                        )
                    } else
                    {
                        messagesRef.value = "ENABLED"

                        player.sendMessage(
                            "${CC.GREEN}You're now receiving friend requests."
                        )
                    }

                    profile.save()
                }
            })
    }

    override val description = listOf(
        "Friend privacy, spam, and",
        "other options."
    )
    override val displayName = "Friends"

    override val items = listOf(
        buildEntry {
            id = "friend_requests"
            displayName = "Friend requests"

            clazz = StateSettingValue::class.java
            default = StateSettingValue.ENABLED

            description += "Allows you to disable"
            description += "friend requests through"
            description += "/friend add."

            item = ItemBuilder.of(Material.SPECKLED_MELON)
        }
    )

    override fun display(player: Player) = true
}
