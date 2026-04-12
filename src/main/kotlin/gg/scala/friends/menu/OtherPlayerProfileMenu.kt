package gg.scala.friends.menu

import gg.scala.friends.player.KFriendsPlayer
import gg.scala.friends.socials.SocialMediaProvider
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import java.util.UUID

class OtherPlayerProfileMenu(
    private val target: UUID
) : Menu("Viewing ${target.username()}'s Profile")
{
    private var friendPlayer: KFriendsPlayer? = null
    private var rank: Rank? = null

    init
    {
        placeholder = true
        async = true
    }

    override fun asyncLoadResources(
        player: Player, callback: (Boolean) -> Unit
    )
    {
        DataStoreObjectControllerCache
            .findNotNull<KFriendsPlayer>()
            .load(target, DataStoreStorageType.MONGO)
            .thenAccept {
                this.friendPlayer = it
            }
            .thenCompose {
                QuickAccess.computeRank(this.target)
            }
            .thenAccept {
                this.rank = it
            }
            .exceptionally {
                callback.invoke(false)
                it.printStackTrace()
                return@exceptionally null
            }
            .thenRun {
                callback.invoke(true)
            }
            .join()
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        buttons[4] = ItemBuilder
            .of(Material.matchMaterial("PLAYER_HEAD") ?: Material.COMPASS)
            .name("${CC.WHITE}${
                this.target.username()
            }")
            .addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_POTION_EFFECTS)
            .addToLore(
                "${CC.GRAY}Rank: ${
                    this.rank?.getColoredName(false) ?: "${CC.RED}Loading..."
                }",
                "",
                "${CC.BLUE}Discord: ${CC.WHITE}${
                    if (this.friendPlayer == null) "Loading..." else this.friendPlayer?.media(SocialMediaProvider.Discord) ?: "Not set"
                }",
                "${CC.GOLD}Instagram: ${CC.WHITE}${
                    if (this.friendPlayer == null) "Loading..." else this.friendPlayer?.media(SocialMediaProvider.Instagram) ?: "Not set"
                }",
                "${CC.D_AQUA}Telegram: ${CC.WHITE}${
                    if (this.friendPlayer == null) "Loading..." else this.friendPlayer?.media(SocialMediaProvider.Telegram) ?: "Not set"
                }",
                "${CC.D_GRAY}GitHub: ${CC.WHITE}${
                    if (this.friendPlayer == null) "Loading..." else this.friendPlayer?.media(SocialMediaProvider.GitHub) ?: "Not set"
                }",
                "${CC.AQUA}Twitter: ${CC.WHITE}${
                    if (this.friendPlayer == null) "Loading..." else this.friendPlayer?.media(SocialMediaProvider.Twitter) ?: "Not set"
                }",
                "${CC.RED}YouTube: ${CC.WHITE}${
                    if (this.friendPlayer == null) "Loading..." else this.friendPlayer?.media(SocialMediaProvider.YouTube) ?: "Not set"
                }"
            )
            .owner(this.target.username())
            .toButton()

        return buttons
    }
}
