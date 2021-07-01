package forcekey

import basemod.ReflectionHacks
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.rooms.CampfireUI
import com.megacrit.cardcrawl.rooms.RestRoom
import com.megacrit.cardcrawl.ui.campfire.AbstractCampfireOption
import com.megacrit.cardcrawl.ui.campfire.RecallOption

@SpirePatch(clz = CampfireUI::class, method = "initializeButtons")
class CampfireUIPatch {
    companion object {
        @JvmStatic
        @SpirePostfixPatch
        fun disableButtons(ui: CampfireUI) {
            // If act 4 is available, and the player is in act 3 and can recall
            if (Settings.isFinalActAvailable && !Settings.hasRubyKey && AbstractDungeon.actNum == 3 &&
                // and if there are no campfires available in the future.
                !MapAnalyser.nodeReachableFromCurrentNode { node -> node.room is RestRoom }) {
                ReflectionHacks.getPrivate<ArrayList<AbstractCampfireOption>>(ui, CampfireUI::class.java, "buttons")
                    .forEach { campfireOption ->
                        // Turn off all but the recall option.
                        if (campfireOption !is RecallOption) {
                            campfireOption.usable = false
                        }
                    }
            }
        }
    }
}