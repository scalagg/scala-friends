package gg.scala.friends.player

import gg.scala.common.Savable
import gg.scala.friends.registrar.KPlayerHandler
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
class KFriendsPlayer(
    val uniqueId: UUID
) : Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    val friends = mutableMapOf<UUID, Long>()

    fun orderedFriends(): List<UUID>
    {
        return friends.keys.toList()
            .sortedByDescending { friends[it]!! }
    }

    override fun save(): CompletableFuture<Void>
    {
        return KPlayerHandler.controller
            .save(this, DataStoreStorageType.MONGO)
    }
}
