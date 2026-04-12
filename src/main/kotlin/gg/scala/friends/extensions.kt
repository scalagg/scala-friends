package gg.scala.friends

import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import org.bukkit.entity.Player

fun Player.textured(item: String) = object : TexturedHeadButton(item)
{}.getButtonItem(this)
