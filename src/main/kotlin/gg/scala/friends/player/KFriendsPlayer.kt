package gg.scala.friends.player

import gg.scala.common.Savable
import gg.scala.friends.handler.KPlayerHandler
import gg.scala.friends.socials.SocialMediaProvider
import gg.scala.lemon.util.QuickAccess
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.controller.annotations.Timestamp
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
class KFriendsPlayer(
    private val uniqueId: UUID
) : Savable, IDataStoreObject
{
    @JvmField
    @Timestamp
    var timestamp = 0L

    override val identifier: UUID
        get() = uniqueId

    val friends = mutableMapOf<UUID, Long>()
    val bestFriends = mutableSetOf<UUID>()

    val socialMedia =
        mutableMapOf<SocialMediaProvider, String>()

    fun media(
        provider: SocialMediaProvider
    ): String?
    {
        return socialMedia[provider]
    }

    fun orderedFriends(): List<UUID>
    {
        return friends.keys.toList()
            .sortedByDescending { friends[it]!! }
            .sortedByDescending {
                if (QuickAccess.online(it).join()) 1 else 0
            }
    }

    override fun save(): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache
            .findNotNull<KFriendsPlayer>()
            .save(this, DataStoreStorageType.MONGO)
    }
}
