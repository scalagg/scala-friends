package gg.scala.friends

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.commons.ExtendedScalaPlugin
import gg.scala.commons.annotations.container.ContainerDisable
import gg.scala.commons.annotations.container.ContainerEnable
import gg.scala.commons.core.plugin.Plugin
import gg.scala.commons.core.plugin.PluginAuthor
import gg.scala.commons.core.plugin.PluginAuthorComposite
import gg.scala.commons.core.plugin.PluginDependency
import gg.scala.commons.core.plugin.PluginDependencyComposite
import gg.scala.commons.core.plugin.PluginWebsite
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.statistics.StatisticRetrievalResult
import gg.scala.friends.statistics.StatisticsSupplier
import gg.scala.friends.statistics.defaults.PracticeStatisticsSupplier
import gg.scala.friends.statistics.defaults.UHCHostedStatisticsSupplier
import gg.scala.friends.statistics.defaults.UHCStatisticsSupplier
import gg.scala.lemon.LemonConstants
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
@Plugin(
    name = "Friends",
    version = "%remote%/%branch%/%id%"
)
@PluginAuthor("Scala")
@PluginWebsite("https://scala.gg")
@PluginDependencyComposite(
    PluginDependency("scala-commons"),
    PluginDependency("Lemon"),
    PluginDependency("Cookie", soft = true),
    PluginDependency("cloudsync", soft = true)
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

    val suppliers = mutableListOf<StatisticsSupplier<out IDataStoreObject>>()
    var loadingString = "."

    @ContainerEnable
    fun containerEnable()
    {
        if (LemonConstants.SERVER_NAME != "Minequest")
        {
            this.suppliers.add(PracticeStatisticsSupplier)
            this.suppliers.add(UHCStatisticsSupplier)
            this.suppliers.add(UHCHostedStatisticsSupplier)
        }

        this.aware.listen(KFriendHandler)
        this.aware.connect()
            .toCompletableFuture().join()

        logger.info("Starting pre-load for statistic containers.")

        CompletableFuture.runAsync {
            this.suppliers.forEach {
                DataStoreObjectControllerCache
                    .create(it.getDataModel())
            }
        }.thenRun {
            logger.info("Completed pre-load for statistic containers.")
        }.exceptionally {
            it.printStackTrace()
            return@exceptionally null
        }

        server.scheduler.runTaskTimerAsynchronously(this,
            {
                loadingString =
                    if (loadingString == ".") ".." else if (loadingString == "..") "..." else "."
            }, 10L, 10L
        )

        Events.subscribe(PlayerQuitEvent::class.java)
            .handler {
                for (supplier in this.suppliers)
                {
                    supplier.cache.remove(it.player.uniqueId)
                }
            }

        Events.subscribe(PlayerLoginEvent::class.java)
            .filter { it.result == PlayerLoginEvent.Result.ALLOWED }
            .handler {
                ForkJoinPool.commonPool()
                    .execute {
                        for (supplier in this.suppliers)
                        {
                            // TODO: redis
                            val result = supplier.controller()
                                .load(
                                    it.player.uniqueId,
                                    DataStoreStorageType.MONGO
                                )
                                .join()

                            if (result == null)
                            {
                                supplier.cache[it.player.uniqueId] =
                                    StatisticRetrievalResult(false, null)
                                return@execute
                            }

                            val render = supplier.render(result)

                            supplier.cache[it.player.uniqueId] =
                                StatisticRetrievalResult(true, render)
                        }
                    }
            }
    }
}
