package gg.scala.friends.statistics.defaults

import com.cryptomorin.xseries.XMaterial
import gg.scala.friends.statistics.StatisticsSupplier
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.util.CC
import java.util.*

/**
 * @author GrowlyX
 * @since 7/17/2022
 */
object PracticeStatisticsSupplier : StatisticsSupplier<PracticePlayer>()
{
    override fun render(model: IDataStoreObject): List<String>
    {
        val practicePlayer = model as PracticePlayer
        val description = mutableListOf<String>()

        description += "${CC.GRAY}Total played: ${CC.WHITE}${practicePlayer.totalPlayed}"
        description += "${CC.GRAY}Total wins: ${CC.WHITE}${practicePlayer.totalWins}"
        description += "${CC.GRAY}Total losses: ${CC.WHITE}${practicePlayer.totalLosses}"
        description += ""
        description += "${CC.GRAY}Unranked played: ${CC.WHITE}${practicePlayer.unrankedPlayed}"
        description += "${CC.GRAY}Unranked wins: ${CC.WHITE}${practicePlayer.unrankedWinCount}"
        description += "${CC.GRAY}Unranked losses: ${CC.WHITE}${practicePlayer.unrankedLossCount}"
        description += ""
        description += "${CC.GRAY}Global elo: ${CC.WHITE}${practicePlayer.globalElo}"
        description += ""
        description += "${CC.GRAY}Ranked played: ${CC.WHITE}${practicePlayer.rankedPlayed}"
        description += "${CC.GRAY}Ranked wins: ${CC.WHITE}${practicePlayer.rankedWinCount}"
        description += "${CC.GRAY}Ranked losses: ${CC.WHITE}${practicePlayer.rankedLossCount}"
        description += ""
        description += "${CC.GRAY}W/L ratio: ${CC.WHITE}${"%.2f".format(practicePlayer.winLossRatio)}"

        return description
    }

    override fun controller() = DataStoreObjectControllerCache.findNotNull<PracticePlayer>()
    override fun getDataModel() = PracticePlayer::class

    override fun gameModeName() = "Duels"
    override fun gameModeIcon() = XMaterial.DIAMOND_SWORD
}

object UHCStatisticsSupplier : StatisticsSupplier<UHCPlayerModel>()
{
    override fun render(model: IDataStoreObject): List<String>
    {
        val practicePlayer = model as UHCPlayerModel
        val description = mutableListOf<String>()

        description += "${CC.GRAY}Total played: ${CC.WHITE}${practicePlayer.played.value}"
        description += "${CC.GRAY}Total wins: ${CC.WHITE}${practicePlayer.wins.value}"
        description += "${CC.GRAY}Total losses: ${CC.WHITE}${practicePlayer.losses.value}"
        description += ""
        description += "${CC.GRAY}Total kills: ${CC.WHITE}${practicePlayer.kills.value}"
        description += "${CC.GRAY}Total deaths: ${CC.WHITE}${practicePlayer.deaths.value}"
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

object UHCHostedStatisticsSupplier : StatisticsSupplier<UHCHostedPlayerModel>()
{
    override fun render(model: IDataStoreObject) = listOf<String>()

    override fun controller() = DataStoreObjectControllerCache.findNotNull<UHCHostedPlayerModel>()
    override fun getDataModel() = UHCHostedPlayerModel::class

    override fun gameModeName() = "Hosted UHC"
    override fun gameModeIcon() = XMaterial.ENCHANTED_BOOK
}

data class ImmutableValue(val value: Int)

data class UHCPlayerModel(
    val _id: UUID,
    val wins: ImmutableValue,
    val losses: ImmutableValue,
    val played: ImmutableValue,
    val deaths: ImmutableValue,
    val kills: ImmutableValue,
) : IDataStoreObject
{
    override val identifier: UUID
        get() = _id

    val winLossRatio: Float
        get() = kotlin
            .runCatching {
                wins.value / losses.value.toFloat()
            }
            .getOrElse { 0.0F }

    val killDeathRatio: Float
        get() = kotlin
            .runCatching {
                kills.value / deaths.value.toFloat()
            }
            .getOrElse { 0.0F }
}

data class UHCHostedPlayerModel(
    val _id: UUID,
    val wins: ImmutableValue,
    val losses: ImmutableValue,
    val played: ImmutableValue,
    val deaths: ImmutableValue,
    val kills: ImmutableValue,
) : IDataStoreObject
{
    override val identifier: UUID
        get() = _id

    val winLossRatio: Float
        get() = kotlin
            .runCatching {
                wins.value / losses.value.toFloat()
            }
            .getOrElse { 0.0F }

    val killDeathRatio: Float
        get() = kotlin
            .runCatching {
                kills.value / deaths.value.toFloat()
            }
            .getOrElse { 0.0F }
}

data class PracticePlayer(
    val uniqueId: UUID,
    val unrankedWinCount: Int,
    val unrankedLossCount: Int,
    val rankedWinCount: Int,
    val rankedLossCount: Int,
    val globalElo: Int
) : IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    val unrankedPlayed: Int
        get() = unrankedWinCount + unrankedLossCount

    val rankedPlayed: Int
        get() =  rankedWinCount + rankedLossCount

    val totalWins: Int
        get() = unrankedWinCount + rankedWinCount

    val totalLosses: Int
        get() = unrankedLossCount + rankedLossCount

    val totalPlayed: Int
        get() =  rankedPlayed + unrankedPlayed

    val winLossRatio: Float
        get() = kotlin
            .runCatching {
                totalWins / totalLosses.toFloat()
            }
            .getOrElse { 0.0F }
}
