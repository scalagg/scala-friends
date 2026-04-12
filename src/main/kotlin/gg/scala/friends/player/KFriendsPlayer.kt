package gg.scala.friends.player

import gg.scala.common.Savable
import gg.scala.friends.handler.KFriendHandler
import gg.scala.friends.socials.SocialMediaProvider
import gg.scala.lemon.util.QuickAccess
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*
import java.util.concurrent.CompletableFuture

class KFriendsPlayer(
    private val uniqueId: UUID
) : Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    val bestFriends = mutableSetOf<UUID>()

    val socialMedia =
        mutableMapOf<SocialMediaProvider, String>()

    fun media(
        provider: SocialMediaProvider
    ): String?
    {
        return socialMedia[provider]
    }

    fun orderedFriends() = KFriendHandler.getAllFriendshipsOf(uniqueId)
        .thenApply {
            it
                .sortedByDescending { friendship -> friendship.startedOn }
                .sortedByDescending { friendship ->
                    if (QuickAccess.online(friendship.otherEndOf(uniqueId)).join()) 1 else 0
                }
        }

    override fun save(): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache
            .findNotNull<KFriendsPlayer>()
            .save(this, DataStoreStorageType.MONGO)
    }
}
