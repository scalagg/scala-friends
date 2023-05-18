package gg.scala.friends.menu.template

import gg.scala.friends.menu.FriendUIService
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
abstract class AbstractMultiPageFriendMenu(
    private val title: String
) : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val multiPageSlots = mutableListOf(
            *addItemRow(0),
            *addItemRow(1),
            *addItemRow(2),
            *addItemRow(3)
        )

        private fun addItemRow(multiplier: Int) =
            mutableListOf<Int>()
                .apply {
                    val multiplierOfNine = multiplier * 9
                    add(2 + multiplierOfNine)
                    add(3 + multiplierOfNine)
                    add(4 + multiplierOfNine)
                    add(5 + multiplierOfNine)
                    add(6 + multiplierOfNine)
                    add(7 + multiplierOfNine)
                    add(8 + multiplierOfNine)
                }
                .toTypedArray()
    }

    init
    {
        applyHeader = false
    }

    abstract fun configureMenuItems(
        player: Player,
        buttons: MutableMap<Int, Button>
    )

    override fun getPrePaginatedTitle(player: Player) = title

    override fun getPageButtonSlots() = 1 to 28

    override fun getAllPagesButtonSlots() = multiPageSlots
    override fun getMaxItemsPerPage(player: Player) = multiPageSlots.size

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        FriendUIService.populate(player, buttons)

        return buttons
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()
        configureMenuItems(player, buttons)

        return buttons
    }

    override fun size(buttons: Map<Int, Button>) = 36
}
