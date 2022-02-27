package gg.scala.friends.registrar

import gg.scala.friends.player.KFriendsPlayer
import gg.scala.lemon.Lemon
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
import net.evilblock.cubed.serializers.Serializers
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * @author GrowlyX
 * @since 11/10/2021
 */
object KPlayerHandler : ObjectRegistrar<UUID, KFriendsPlayer>()
{
    lateinit var controller: DataStoreObjectController<KFriendsPlayer>

    override val handle: MutableMap<UUID, KFriendsPlayer>
        get() = controller.localCache

    override fun initialLoad()
    {
        controller = DataStoreObjectControllerCache.create()
        controller.provideCustomSerializer(Serializers.gson)

        Events.subscribe(AsyncPlayerPreLoginEvent::class.java).handler {
            controller.loadAndCache(
                it.uniqueId,
                { KFriendsPlayer(it.uniqueId) },
                DataStoreStorageType.MONGO
            )
        }

        Events.subscribe(PlayerQuitEvent::class.java).handler {
            delete(it.player.uniqueId)?.save()
        }
    }

    fun find(player: Player): KFriendsPlayer? = handle[player.uniqueId]
}
