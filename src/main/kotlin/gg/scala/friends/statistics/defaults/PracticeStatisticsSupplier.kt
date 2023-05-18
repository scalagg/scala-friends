package gg.scala.friends.statistics.defaults

import com.cryptomorin.xseries.XMaterial
import gg.scala.friends.statistics.StatisticsSupplier
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.math.Numbers
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

        description += "${CC.GRAY}Total played: ${CC.WHITE}${practicePlayer.totalPlayed.fmt()}"
        description += "${CC.GRAY}Total wins: ${CC.WHITE}${practicePlayer.totalWins.fmt()}"
        description += "${CC.GRAY}Total losses: ${CC.WHITE}${practicePlayer.totalLosses.fmt()}"
        description += ""
        description += "${CC.GRAY}Unranked played: ${CC.WHITE}${practicePlayer.unrankedPlayed.fmt()}"
        description += "${CC.GRAY}Unranked wins: ${CC.WHITE}${practicePlayer.unrankedWinCount.fmt()}"
        description += "${CC.GRAY}Unranked losses: ${CC.WHITE}${practicePlayer.unrankedLossCount.fmt()}"
        description += ""
        description += "${CC.GRAY}Global elo: ${CC.WHITE}${practicePlayer.globalElo.fmt()}"
        description += ""
        description += "${CC.GRAY}Ranked played: ${CC.WHITE}${practicePlayer.rankedPlayed.fmt()}"
        description += "${CC.GRAY}Ranked wins: ${CC.WHITE}${practicePlayer.rankedWinCount.fmt()}"
        description += "${CC.GRAY}Ranked losses: ${CC.WHITE}${practicePlayer.rankedLossCount.fmt()}"
        description += ""
        description += "${CC.GRAY}W/L ratio: ${CC.WHITE}${"%.2f".format(practicePlayer.winLossRatio)}"

        return description
    }

    override fun controller() = DataStoreObjectControllerCache.findNotNull<PracticePlayer>()
    override fun getDataModel() = PracticePlayer::class

    override fun gameModeName() = "Duels"
    override fun gameModeIcon() = XMaterial.DIAMOND_SWORD
}

fun Int.fmt() = Numbers.format(this)


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
