package com.hezaerd.advancement;

import com.hezaerd.PawsPals;
import net.minecraft.advancements.CriteriaTriggers;

public final class ModCriteria {
    public static final CalledHappyGhastCriterion CALLED_HAPPY_GHAST = CriteriaTriggers.register(
            PawsPals.id("called_happy_ghast").toString(), new CalledHappyGhastCriterion()
    );

    public static void initialize() {
        
    }
}
