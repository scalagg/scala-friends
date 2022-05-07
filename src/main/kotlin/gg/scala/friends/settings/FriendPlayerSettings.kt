package gg.scala.friends.settings

import com.cryptomorin.xseries.XMaterial
import gg.scala.cookie.settings.PlayerSettings
import gg.scala.cookie.settings.annotation.RegisterPlayerSettings
import gg.scala.cookie.settings.builder.BooleanPlayerSettingsBuilder
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
@RegisterPlayerSettings
object FriendPlayerSettings : PlayerSettings()
{
    override fun getButtons(player: Player) =
        mutableMapOf(
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

    override fun identifier() = "Friends"
    override fun description() = listOf(
        "${CC.GRAY}Friend privacy, spam, and other options."
    )

    override fun hasPermission(player: Player) = true
}
