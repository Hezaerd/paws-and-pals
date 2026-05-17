package com.hezaerd.utils;

import com.hezaerd.PawsPals;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;

public final class Wisdom {
    public static final RandomSource RANDOM = RandomSource.create();
    public static final String[] QUOTES = {
            "I am a cage, in search of a bird.",
            "A book must be the axe for the frozen sea within us.",
            "I am free and that is why I am lost.",
            "The meaning of life is that it stops.",
            "All language is but a poor translation.",
            "You are the knife I turn inside myself; that is love. That, my dear, is love.",
            "Slept, awoke, slept, awoke, miserable life."
    };

    public static void spread() {
        PawsPals.LOGGER.info(Util.getRandom(QUOTES, RANDOM));
    }
}
