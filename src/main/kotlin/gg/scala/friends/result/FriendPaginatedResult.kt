package gg.scala.friends.result

import gg.scala.lemon.player.extension.PlayerCachingExtension
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.PaginatedResult
import java.util.*

/**
 * @author GrowlyX
 * @since 11/11/2021
 */
object FriendPaginatedResult : PaginatedResult<UUID>()
{
    override fun format(result: UUID, resultIndex: Int): String
    {
        val username = CubedCacheUtil.fetchName(result)!!

        if (PlayerCachingExtension.loaded)
        {
            val fundamental = PlayerCachingExtension.handle
                .fetchEntryByKeySync(result.toString())
            val online = fundamental != null

            return if (online)
            {
                "${CC.GREEN} ● ${CC.PRI}${fundamental.username}${CC.SEC} is on ${fundamental.currentServer}."
            } else
            {
                "${CC.RED} ● ${CC.PRI}$username"
            }
        } else
        {
            return "${CC.GRAY} ● ${CC.PRI}$username"
        }
    }

    override fun getHeader(page: Int, maxPages: Int): String = "${CC.PRI}=== ${CC.SEC}Your Friends ${CC.GRAY}($page/$maxPages) ${CC.PRI}==="
}
