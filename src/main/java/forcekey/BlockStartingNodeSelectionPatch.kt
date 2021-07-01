package forcekey

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.map.MapRoomNode
import com.megacrit.cardcrawl.relics.WingBoots


@SpirePatch(clz = MapRoomNode::class, method = "update")
class BlockStartingNodeSelectionPatch {
    companion object {
        @JvmStatic
        @SpireInsertPatch(rloc = 118)
        fun blockSelection(node: MapRoomNode): SpireReturn<Void> {
            // If the player is in act 3 and does not have a working pair of wing boots.
            if (AbstractDungeon.actNum == 3 && !Settings.hasEmeraldKey &&
                !(AbstractDungeon.player.hasRelic(WingBoots.ID) && !AbstractDungeon.player.getRelic(WingBoots.ID).usedUp) &&
                // and the node does not have or does not lead to the green key
                !MapAnalyser(AbstractDungeon.map).nodeReachableFrom(node.coordinates) { it.hasEmeraldKey } && !node.hasEmeraldKey) {
                // Make the method return early so the player cannot select the node.
                return SpireReturn.Return(null)
            }

            // Otherwise allow the node to accept inputs.
            return SpireReturn.Continue()
        }
    }
}