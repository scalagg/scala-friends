package gg.scala.friends

import gg.scala.banana.Banana
import gg.scala.banana.options.BananaOptions
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.friends.handler.KFriendHandler
import gg.scala.lemon.Lemon

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
class FriendsSpigotPlugin : ExtendedScalaPlugin()
{
    override fun enable()
    {
        val banana = Banana(
            Lemon.instance.credentials,
            BananaOptions(
                "friends"
            )
        )
        banana.registerClass(KFriendHandler)
        banana.subscribe()
    }
}
