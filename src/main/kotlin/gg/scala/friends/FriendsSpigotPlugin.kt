package gg.scala.friends

import gg.scala.banana.Banana
import gg.scala.banana.options.BananaOptions
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.cookie.Cookie
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.settings.FriendsSettingsImpl
import gg.scala.lemon.Lemon
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency
import net.evilblock.cubed.command.manager.CubedCommandManager
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
@Plugin(
    name = "Friends",
    depends = [
        PluginDependency("Cubed"),
        PluginDependency("Lemon"),
        PluginDependency("Cookie"),
        PluginDependency("helper"),
        PluginDependency("store-spigot"),
    ]
)
class FriendsSpigotPlugin : ExtendedScalaPlugin()
{
    override fun enable()
    {
        KPlayerHandler.configure()

        registerBananaHandlers()
        registerAcfCommands()

        Cookie.instance.playerSettings
            .add(FriendsSettingsImpl)
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
        val manager = CubedCommandManager(
            plugin = this,
            primary = ChatColor.valueOf(Lemon.instance.lemonWebData.primary),
            secondary = ChatColor.valueOf(Lemon.instance.lemonWebData.secondary)
        )

        Lemon.instance.registerCompletionsAndContexts(manager)
    }
}
