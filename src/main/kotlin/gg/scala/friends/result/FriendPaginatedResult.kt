package gg.scala.friends.result

import gg.scala.friends.friendship.Friendship
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.PaginatedResult
import net.evilblock.cubed.util.time.DateUtil
import org.bukkit.entity.Player

class FriendPaginatedResult(private val holder: Player) : PaginatedResult<Friendship>()
{
    override fun format(result: Friendship, resultIndex: Int): String
    {
        val otherEnd = result.otherEndOf(holder.uniqueId)
        val username = otherEnd.username()

        val fundamental = QuickAccess
            .server(otherEnd).join()

        return if (fundamental != null)
        {
            val cannotInteract = !QuickAccess.canInteractWith(holder, otherEnd)

            if (cannotInteract)
            {
                "${CC.RED} ● ${CC.PRI}$username${CC.SEC} was on a while ago."
            } else
            {
                "${CC.GREEN} ● ${CC.PRI}$username${CC.SEC} is on ${CC.GREEN}${fundamental.id}${CC.SEC}."
            }
        } else
        {
            val lastLogin = QuickAccess
                .getLastOnline(otherEnd)
                .join()

            "${CC.RED} ● ${CC.PRI}$username${CC.SEC} ${
                if (lastLogin == null) "was on a while ago" else "was on ${
                    DateUtil.formatSimplifiedDateDiff(lastLogin)
                } ago"
            }."
        }
    }

    override fun getHeader(page: Int, maxPages: Int) =
        "${CC.PRI}=== ${CC.SEC}Your Friends ${CC.GRAY}($page/$maxPages) ${CC.PRI}==="
}
