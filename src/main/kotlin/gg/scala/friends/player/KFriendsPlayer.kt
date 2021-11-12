package gg.scala.friends.player

import gg.scala.common.Savable
import gg.scala.friends.registrar.impl.KPlayerHandler
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
class KFriendsPlayer(
    val uniqueId: UUID,
    var username: String
) : Savable
{
    val friends = mutableMapOf<UUID, Long>()

    fun orderedFriends(): List<UUID>
    {
        return friends.keys.toList()
            .sortedByDescending { friends[it]!! }
    }

    override fun save(): CompletableFuture<Void>
    {
        return KPlayerHandler.playerHandle
            .saveEntry(uniqueId.toString(), this)
    }
}
