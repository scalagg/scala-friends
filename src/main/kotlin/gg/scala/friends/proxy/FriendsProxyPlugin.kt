package gg.scala.friends.proxy

import com.imaginarycode.minecraft.redisbungee.events.PlayerJoinedNetworkEvent
import com.imaginarycode.minecraft.redisbungee.events.PlayerLeftNetworkEvent
import gg.scala.banana.message.Message
import gg.scala.cocoa.CocoaProxyPlugin
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.event.EventHandler

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
class FriendsProxyPlugin : Plugin(), Listener
{
    override fun onEnable()
    {
        proxy.pluginManager.registerListener(this, this)
    }

    @EventHandler
    fun onNetworkJoin(event: PlayerJoinedNetworkEvent)
    {
        val message = Message(
            packet = "friend-network-update"
        )

        message["uniqueId"] = event.uuid.toString()
        message["type"] = "joined"

        message.dispatch("friends", CocoaProxyPlugin.instance.proxyBanana)
    }

    @EventHandler
    fun onNetworkJoin(event: PlayerLeftNetworkEvent)
    {
        val message = Message(
            packet = "friend-network-update"
        )

        message["uniqueId"] = event.uuid.toString()
        message["type"] = "left"

        message.dispatch("friends", CocoaProxyPlugin.instance.proxyBanana)
    }
}
