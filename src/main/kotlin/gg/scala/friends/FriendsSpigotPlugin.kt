package gg.scala.friends

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.friends.handler.KFriendHandler
import me.lucko.helper.plugin.ap.Plugin
import me.lucko.helper.plugin.ap.PluginDependency

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
@Plugin(
    name = "Friends",
    apiVersion = "1.18",
    depends = [
        PluginDependency("scala-commons"),
        PluginDependency("Lemon"),
        PluginDependency("Cookie"),
        PluginDependency("store-spigot"),
        PluginDependency("cloudsync", soft = true)
    ]
)
class FriendsSpigotPlugin : ExtendedScalaPlugin()
{
    private val aware by lazy {
        AwareBuilder
            .of<AwareMessage>("friends")
            .codec(AwareMessageCodec)
            .logger(logger)
            .build()
    }

    @ContainerEnable
    fun containerEnable()
    {
        this.aware.listen(KFriendHandler)
        this.aware.connect()
            .toCompletableFuture().join()
    }

    @ContainerDisable
    fun containerDisable()
    {
        this.aware.shutdown()
    }
}
