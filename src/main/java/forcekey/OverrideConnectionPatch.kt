package forcekey

import com.evacipated.cardcrawl.modthespire.lib.ByRef
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.map.MapRoomNode
import com.megacrit.cardcrawl.relics.WingBoots

@SpirePatch(clz = MapRoomNode::class, method = "update")
class OverrideConnectionPatch {
    companion object {
        // In the update function, two variables, normalConnection and wingedConnection are calculated, and they will
        // be used to determine whether the node will be available for travel.
        @JvmStatic
        @SpireInsertPatch(rloc = 58, localvars = ["normalConnection", "wingedConnection"])
        fun overrideConnection(node: MapRoomNode,
                               @ByRef(type = "boolean")
                               normalConnection: Array<Boolean>,
                               @ByRef(type = "boolean")
                               wingedConnection: Array<Boolean>) {
            // Only consider blocking paths if the player is in act 3.
            if (AbstractDungeon.actNum == 3 && Settings.isFinalActAvailable && !Settings.hasEmeraldKey) {
                val nodeContainsKey = node.hasEmeraldKey
                val nodeLeadsToKey = MapAnalyser(AbstractDungeon.map).nodeReachableFrom(node.coordinates) { it.hasEmeraldKey }

                // If the player can access the node normally
                if (normalConnection[0]) {
                    // and with wing boots
                    if (wingedConnection[0]) {
                        // Locate the green key on the map.
                        val greenKeyLocation = AbstractDungeon.map
                            .fold(listOf()) { acc: List<MapRoomNode>, row: ArrayList<MapRoomNode> -> acc + row }
                            .first { it.hasEmeraldKey }.coordinates
                        // If it is only one floor away from the player, then block the player form going to a node that
                        // does not contain the green key.
                        if (greenKeyLocation.y - AbstractDungeon.currMapNode.y == 1) {
                            wingedConnection[0] = nodeContainsKey
                            normalConnection[0] = nodeContainsKey
                        }
                        // Otherwise allow the player to travel freely.
                    } // and without wing boots (the player does not have wing boots)
                    else {
                        // Block the player from going to a node that does not contain or lead to the green key.
                        normalConnection[0] = nodeContainsKey || nodeLeadsToKey
                    }
                } // If the player cannot access the node normally.
                else {
                    // But can with wing boots
                    if (AbstractDungeon.player.hasRelic(WingBoots.ID) && wingedConnection[0]) {
                        val availableCharges = AbstractDungeon.player.getRelic(WingBoots.ID).counter
                        // If the player only has one charge left.
                        if (availableCharges == 1) {
                            // then block the player from going to a node that does not contain or lead to the green key.
                            wingedConnection[0] = nodeContainsKey || nodeLeadsToKey
                        } // If the player has more than one charges left.
                        else if (availableCharges > 1) {
                            // Locate the green key on the map.
                            val greenKeyLocation = AbstractDungeon.map
                                .fold(listOf()) { acc: List<MapRoomNode>, row: ArrayList<MapRoomNode> -> acc + row }
                                .first { it.hasEmeraldKey }.coordinates
                            // If it is only one floor away from the player, then block the player form going to a node that
                            // does not contain the green key.
                            if (greenKeyLocation.y - AbstractDungeon.currMapNode.y == 1) {
                                wingedConnection[0] = nodeContainsKey
                            }
                            // Otherwise allow the player to travel freely.
                        }
                    }
                }
            }
        }
    }
}