package com.stevekung.stratagems.datagen;

import com.stevekung.stratagems.api.ModConstants;
import com.stevekung.stratagems.api.Stratagem;
import com.stevekung.stratagems.api.StratagemDisplay;
import com.stevekung.stratagems.api.StratagemProperties;
import com.stevekung.stratagems.api.action.StratagemAction;
import com.stevekung.stratagems.api.rule.StratagemRule;
import net.minecraft.network.chat.Component;

public abstract class AbstractStratagem {

    /**
     * You can rename this MOD_ID as you wish, it's only used to create the translation key
     */
    public static final String MOD_ID = ModConstants.MOD_ID;

    /**
     * String of you stratagem register in the registry
     * @return
     */
    public abstract String KEY_STRING();

    /**
     * Stratagem code, only "wsda"
     * @return
     */
    public abstract String STRATAGEM_CODE();

    /**
     * The stratagem display
     * @return
     */
    public abstract StratagemDisplay DISPLAY();

    /**
     * The stratagem action
     * @return
     */
    public abstract StratagemAction.Builder ACTION();

    /**
     * The stratagem rule
     * @return
     */
    public abstract StratagemRule.Builder RULE();

    /**
     * The stratagem property
     * @return
     */
    public abstract StratagemProperties PROPERTIES();

    /**
     * The translation key you can use in you Language Provider
     * @return
     */
    public Component TRANSLATE_KEY() {
        return Component.translatable(MOD_ID + ".stratagem." + KEY_STRING());
    }

    /**
     * The Actual Stratagem
     * Used for the StratagemDataProvider when generatingStratagems
     * @return
     */
    public Stratagem STRATAGEM() {
        return new Stratagem(STRATAGEM_CODE(), TRANSLATE_KEY(), DISPLAY(), ACTION().build(), RULE().build(), PROPERTIES());
    }
}

