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
        description += "${CC.GRAY}W/L ratio: ${CC.WHITE}${"%.2f".format(practicePlayer.winLossRatio)}%"

        return description
    }

    override fun controller() = DataStoreObjectControllerCache.findNotNull<PracticePlayer>()
    override fun getDataModel() = PracticePlayer::class

    override fun gameModeName() = "Practice"
    override fun gameModeIcon() = XMaterial.POTION
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
