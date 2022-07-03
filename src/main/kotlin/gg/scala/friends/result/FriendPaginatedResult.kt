package gg.scala.friends.result

import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
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
        val username = result.username()

        val fundamental = QuickAccess
            .server(result).join()

        return if (fundamental != null)
        {
            "${CC.GREEN} ● ${CC.PRI}$username${CC.SEC} is on ${CC.GREEN}${fundamental.id}${CC.SEC}."
        } else
        {
            "${CC.RED} ● ${CC.PRI}$username${CC.SEC} is offline."
        }
    }

    override fun getHeader(page: Int, maxPages: Int) =
        "${CC.PRI}=== ${CC.SEC}Your Friends ${CC.GRAY}($page/$maxPages) ${CC.PRI}==="
}
