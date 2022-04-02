package gg.scala.friends.handler

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.friends.player.KFriendsPlayer
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.CachedDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author GrowlyX
 * @since 11/10/2021
 */
@Service
object KPlayerHandler
{
    lateinit var controller: DataStoreObjectController<KFriendsPlayer>

    @Configure
    fun configure()
    {
        controller = DataStoreObjectControllerCache.create()

        Events.subscribe(AsyncPlayerPreLoginEvent::class.java).handler {
            controller.loadAndCache(
                it.uniqueId,
                { KFriendsPlayer(it.uniqueId) },
                DataStoreStorageType.MONGO
            )
        }

        Events.subscribe(PlayerQuitEvent::class.java).handler {
            controller.localCache().remove(it.player.uniqueId)?.save()
        }
    }

    fun find(player: Player): KFriendsPlayer? =
        controller.localCache()[player.uniqueId]

    fun find(uniqueId: UUID): KFriendsPlayer? =
        controller.localCache()[uniqueId]
}
