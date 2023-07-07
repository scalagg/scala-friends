package gg.scala.friends.statistics.defaults

import com.cryptomorin.xseries.XMaterial
import gg.scala.friends.statistics.StatisticsSupplier
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
object SoupStatisticsSupplier : StatisticsSupplier<SoupProfile>()
{
    override fun render(model: IDataStoreObject): List<String>
    {
        val practicePlayer = model as SoupProfile
        val description = mutableListOf<String>()

        description += "${CC.GRAY}Kills: ${CC.WHITE}${practicePlayer.kills.fmt()}"
        description += "${CC.GRAY}Deaths: ${CC.WHITE}${practicePlayer.deaths.fmt()}"
        description += "${CC.GRAY}Consumed soup: ${CC.WHITE}${practicePlayer.soupsConsumed.fmt()}"
        description += ""
        description += "${CC.GRAY}Kill streak: ${CC.WHITE}${practicePlayer.killStreak.fmt()}"
        description += "${CC.GRAY}Highest kill streak: ${CC.WHITE}${practicePlayer.maxKillStreak.fmt()}"
        description += ""
        description += "${CC.GRAY}K/D ratio: ${CC.WHITE}${"%.2f".format(practicePlayer.killDeathRatio)}"

        return description
    }

    override fun controller() = DataStoreObjectControllerCache.findNotNull<SoupProfile>()
    override fun getDataModel() = SoupProfile::class

    override fun gameModeName() = "SoupPvP"
    override fun gameModeIcon() = XMaterial.MUSHROOM_STEW
}
