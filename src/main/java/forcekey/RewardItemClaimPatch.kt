package forcekey

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.relics.TinyChest
import com.megacrit.cardcrawl.rewards.RewardItem
import com.megacrit.cardcrawl.rooms.TreasureRoom
import org.apache.logging.log4j.LogManager

@SpirePatch(clz = RewardItem::class, method = "claimReward")
class RewardItemClaimPatch {
    companion object {
        @JvmStatic
        val logger = LogManager.getLogger(RewardItemClaimPatch::class.java.name)

        @JvmStatic
        @SpirePrefixPatch
        fun blockClaim(rewardItem: RewardItem): SpireReturn<Boolean> {
            // If the player does not have tiny chest and is in act 3
            if (!AbstractDungeon.player.hasRelic(TinyChest.ID) && AbstractDungeon.actNum == 3 &&
                // and the reward is a relic and the relic has a blue key attached to it
                rewardItem.type == RewardItem.RewardType.RELIC && rewardItem.relicLink != null &&
                // and if there are no treasure rooms available in the future.
                !MapAnalyser.nodeReachableFromCurrentNode { node -> node.room is TreasureRoom }) {
                // Hint at the player to click the blue key by flashing it.
                rewardItem.relicLink.flash()
                // And reject the claim.
                return SpireReturn.Return(false)
            }

            // Otherwise allow the item to be claimed.
            return SpireReturn.Continue()
        }

        @JvmStatic
        @SpirePostfixPatch
        fun removeRelic(rewardItem: RewardItem) {
            // If the player does not have tiny chest and is in act 3
            if (!AbstractDungeon.player.hasRelic(TinyChest.ID) && AbstractDungeon.actNum == 3 &&
                // and claimed a sapphire key with a relic attached to it
                rewardItem.type == RewardItem.RewardType.SAPPHIRE_KEY && rewardItem.relicLink != null &&
                // and if there are no treasure rooms available in the future.
                !MapAnalyser.nodeReachableFromCurrentNode { node -> node.room is TreasureRoom }) {
                // Disconnect key from the relic.
                rewardItem.relicLink.relicLink = null
            }
        }
    }
}