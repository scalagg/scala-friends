package gg.scala.friends.friendship

import gg.scala.common.Savable
import gg.scala.commons.annotations.Model
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.controller.annotations.Indexed
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.UUID

@Model
data class Friendship(
    @Indexed val player1: UUID,
    @Indexed val player2: UUID,
    override val identifier: UUID = UUID.randomUUID(),
    val startedOn: Long = System.currentTimeMillis()
) : IDataStoreObject, Savable
{
    override fun save() = DataStoreObjectControllerCache
        .findNotNull<Friendship>()
        .save(this, DataStoreStorageType.MONGO)

    fun otherEndOf(player: UUID) = if (player1 == player) player2 else player1
}
