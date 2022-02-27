package gg.scala.friends.settings

import com.cryptomorin.xseries.XMaterial
import gg.scala.cookie.settings.PlayerSettings
import gg.scala.cookie.settings.builder.BooleanPlayerSettingsBuilder
import net.evilblock.cubed.menu.Button
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
object FriendsSettingsImpl : PlayerSettings()
{
    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf(
            0 to BooleanPlayerSettingsBuilder()
                .titleOf("Friend Requests")
                .descriptionOf(
                    "Would you like to",
                    "receive friend requests?"
                )
                .materialOf(XMaterial.BAKED_POTATO)
                .settingOf("friend-requests-disabled")
                .asButton()
        )
    }

    override fun hasPermission(player: Player): Boolean = true
}
