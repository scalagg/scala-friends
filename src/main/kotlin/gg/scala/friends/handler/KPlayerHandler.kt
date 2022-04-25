package gg.scala.friends.handler

import gg.scala.commons.persist.ProfileOrchestrator
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.friends.player.KFriendsPlayer
import java.util.*

/**
 * @author GrowlyX
 * @since 11/10/2021
 */
@Service
object KPlayerHandler : ProfileOrchestrator<KFriendsPlayer>()
{
    override fun new(uniqueId: UUID) =
        KFriendsPlayer(uniqueId)

    override fun type() =
        KFriendsPlayer::class
}
