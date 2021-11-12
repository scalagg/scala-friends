package gg.scala.friends

import gg.scala.banana.Banana
import gg.scala.banana.options.BananaOptions
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.friends.handler.KFriendHandler
import gg.scala.lemon.Lemon
import net.evilblock.cubed.command.manager.CubedCommandManager

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
class FriendsSpigotPlugin : ExtendedScalaPlugin()
{
    override fun enable()
    {
        registerBananaHandlers()
        registerAcfCommands()
    }

    private fun registerBananaHandlers()
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

    private fun registerAcfCommands()
    {
        val manager = CubedCommandManager(this)

        Lemon.instance.registerCompletionsAndContexts(manager)
        Lemon.instance.registerCommandsInPackage(manager, "gg.scala.friends.command")
    }
}
