package gg.scala.friends.statistics.defaults

import com.cryptomorin.xseries.XMaterial
import gg.scala.friends.statistics.StatisticsSupplier
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.util.CC

object UHCStatisticsSupplier : StatisticsSupplier<UHCPlayerModel>()
{
    override fun render(model: IDataStoreObject): List<String>
    {
        val practicePlayer = model as UHCPlayerModel
        val description = mutableListOf<String>()

        description += "${CC.GRAY}Total wins: ${CC.WHITE}${practicePlayer.wins.value.fmt()}"
        description += "${CC.GRAY}Total losses: ${CC.WHITE}${practicePlayer.losses.value.fmt()}"
        description += ""
        description += "${CC.GRAY}Total kills: ${CC.WHITE}${practicePlayer.kills.value.fmt()}"
        description += "${CC.GRAY}Total deaths: ${CC.WHITE}${practicePlayer.deaths.value.fmt()}"
        description += ""
        description += "${CC.GRAY}W/L ratio: ${CC.WHITE}${"%.2f".format(practicePlayer.winLossRatio)}"
        description += "${CC.GRAY}K/D ratio: ${CC.WHITE}${"%.2f".format(practicePlayer.killDeathRatio)}"

        return description
    }

    override fun controller() = DataStoreObjectControllerCache.findNotNull<UHCPlayerModel>()
    override fun getDataModel() = UHCPlayerModel::class

    override fun gameModeName() = "UHC"
    override fun gameModeIcon() = XMaterial.GOLDEN_APPLE
}

