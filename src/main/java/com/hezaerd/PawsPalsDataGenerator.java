package com.hezaerd;

import com.hezaerd.datagen.PawsPalsAdvancementProvider;
import com.hezaerd.datagen.PawsPalsModelProvider;
import com.hezaerd.datagen.PawsPalsRecipeProvider;
import com.hezaerd.datagen.lang.PawsPalsEnglishLangProvider;
import com.hezaerd.datagen.lang.PawsPalsFrenchLangProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class PawsPalsDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        // Models
        pack.addProvider(PawsPalsModelProvider::new);

        // Recipes
        pack.addProvider(PawsPalsRecipeProvider::new);

        // Advancements
        pack.addProvider(PawsPalsAdvancementProvider::new);

        // Translations
        pack.addProvider(PawsPalsEnglishLangProvider::new);
        pack.addProvider(PawsPalsFrenchLangProvider::new);
    }
}
