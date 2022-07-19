package gg.scala.friends.socials

import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 7/14/2022
 */
enum class SocialMediaProvider(
    val color: ChatColor,
    val regex: Regex,
    val skinSignature: String,
    val example: String
)
{
    GitHub(
        color = ChatColor.WHITE,
        regex = "(?!.*(-){2,}.*)[a-z0-9][a-z0-9-]{0,38}[a-z0-9]".toRegex(),
        skinSignature = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjZlMjdkYTEyODE5YThiMDUzZGEwY2MyYjYyZGVjNGNkYTkxZGU2ZWVlYzIxY2NmM2JmZTZkZDhkNDQzNmE3In19fQ==",
        example = "Example"
    ),
    Discord(
        color = ChatColor.BLUE,
        regex = "^.{3,32}#[0-9]{4}\$".toRegex(),
        skinSignature = Constants.DISCORD_ICON,
        example = "Example#0001"
    ),
    Twitter(
        color = ChatColor.AQUA,
        regex = "^[A-Za-z0-9_]{1,15}\$".toRegex(),
        skinSignature = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmFkNDZhNDIyYWU1OTYwM2ZkODg5YzI1MzQ0ZmY2N2JjODQzYWY4ZWU1MTg5MzJjMmUyYWQwN2NkYmY5MzliMyJ9fX0=",
        example = "Example"
    ),
    YouTube(
        color = ChatColor.RED,
        regex = "((http|https):\\/\\/|)(www\\.|)youtube\\.com\\/(channel\\/|user\\/)[a-zA-Z0-9_-]{1,}".toRegex(),
        skinSignature = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDJmNmMwN2EzMjZkZWY5ODRlNzJmNzcyZWQ2NDU0NDlmNWVjOTZjNmNhMjU2NDk5YjVkMmI4NGE4ZGNlIn19fQ==",
        example = "https://youtube.com/user/YouTube"
    )
}
