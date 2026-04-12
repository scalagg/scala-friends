package gg.scala.friends.statistics

import com.cryptomorin.xseries.XMaterial
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.UUID
import kotlin.reflect.KClass

abstract class StatisticsSupplier<T : IDataStoreObject>
{
    var loaded = false
    val cache = mutableMapOf<UUID, StatisticRetrievalResult>()

    abstract fun render(model: IDataStoreObject): List<String>
    abstract fun controller(): DataStoreObjectController<T>

    abstract fun getDataModel(): KClass<T>

    abstract fun gameModeName(): String
    abstract fun gameModeIcon(): XMaterial
}

data class StatisticRetrievalResult(
    val loggedIn: Boolean,
    val render: List<String>?
)
