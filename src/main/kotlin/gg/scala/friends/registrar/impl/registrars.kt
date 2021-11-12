package gg.scala.friends.registrar.impl

import com.solexgames.datastore.commons.connection.impl.mongo.UriMongoConnection
import com.solexgames.datastore.commons.layer.impl.MongoStorageLayer
import gg.scala.friends.player.KFriendsPlayer
import gg.scala.friends.registrar.ObjectRegistrar
import gg.scala.lemon.Lemon
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
    lateinit var playerHandle: MongoStorageLayer<KFriendsPlayer>

    override fun initialLoad()
    {
        MongoStorageLayer(
            UriMongoConnection(
                Lemon.instance.mongoConfig.uri
            ),
            "SolexGames", "friend_players",
            KFriendsPlayer::class.java
        ).also {
            playerHandle = it
            it.supplyWithCustomGson(Serializers.gson)
        }

        Events.subscribe(AsyncPlayerPreLoginEvent::class.java).handler {
            playerHandle.fetchEntryByKey(
                it.uniqueId.toString()
            ).thenAccept { player ->
                if (player != null)
                {
                    insert(
                        it.uniqueId, player.also { _ ->
                            player.username = it.name
                        }
                    )
                } else
                {
                    insert(it.uniqueId, KFriendsPlayer(it.uniqueId, it.name))
                }
            }
        }

        Events.subscribe(PlayerQuitEvent::class.java).handler {
            delete(it.player.uniqueId)?.save()
        }
    }

    fun find(player: Player): KFriendsPlayer? = handle[player.uniqueId]
}
