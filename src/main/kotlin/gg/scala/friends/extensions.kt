package gg.scala.friends

import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/14/2022
 */
fun Player.textured(item: String) = object : TexturedHeadButton(item)
{}.getButtonItem(this)
