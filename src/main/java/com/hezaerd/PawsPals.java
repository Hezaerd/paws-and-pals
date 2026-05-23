package com.hezaerd;

import com.hezaerd.advancement.ModCriteria;
import com.hezaerd.item.ModItems;
import com.hezaerd.utils.Wisdom;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PawsPals implements ModInitializer {
    public static final String MOD_ID = "pawspals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModCriteria.initialize();
        ModItems.initialize();

        Wisdom.spread();
    }

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}