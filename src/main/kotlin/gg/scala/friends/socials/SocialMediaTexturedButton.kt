package gg.scala.friends.socials

import net.evilblock.cubed.menu.buttons.TexturedHeadButton

class SocialMediaTexturedButton(
    provider: SocialMediaProvider
) : TexturedHeadButton(
    provider.skinSignature
)
