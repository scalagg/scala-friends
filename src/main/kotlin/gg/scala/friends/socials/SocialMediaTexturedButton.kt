package gg.scala.friends.socials

import net.evilblock.cubed.menu.buttons.TexturedHeadButton

/**
 * @author GrowlyX
 * @since 7/14/2022
 */
class SocialMediaTexturedButton(
    provider: SocialMediaProvider
) : TexturedHeadButton(
    provider.skinSignature
)
