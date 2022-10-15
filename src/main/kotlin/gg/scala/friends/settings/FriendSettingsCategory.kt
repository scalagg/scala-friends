package gg.scala.friends.settings

import gg.scala.basics.plugin.settings.SettingCategory
import gg.scala.basics.plugin.settings.SettingContainer.buildEntry
import gg.scala.basics.plugin.settings.defaults.values.StateSettingValue
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
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
