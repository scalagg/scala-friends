package gg.scala.friends.statistics.defaults

import com.cryptomorin.xseries.XMaterial
import gg.scala.friends.statistics.StatisticsSupplier
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject

object UHCHostedStatisticsSupplier : StatisticsSupplier<UHCHostedPlayerModel>()
{
    override fun render(model: IDataStoreObject) = listOf<String>()

    override fun controller() = DataStoreObjectControllerCache.findNotNull<UHCHostedPlayerModel>()
    override fun getDataModel() = UHCHostedPlayerModel::class

    override fun gameModeName() = "Hosted UHC"
    override fun gameModeIcon() = XMaterial.ENCHANTED_BOOK
}
