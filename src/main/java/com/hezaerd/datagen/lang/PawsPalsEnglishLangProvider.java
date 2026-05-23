package com.hezaerd.datagen.lang;

import com.hezaerd.utils.TranslationKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class PawsPalsEnglishLangProvider extends FabricLanguageProvider {
    public PawsPalsEnglishLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider holderLookup, @NonNull TranslationBuilder translationBuilder) {
        generateItems(translationBuilder);
        generateMessages(translationBuilder);
        generateAdvancements(translationBuilder);
    }

    private void generateItems(TranslationBuilder translationBuilder) {
        translationBuilder.add(TranslationKeys.Item.HappyGhastWhistle, "Happy Ghast Whistle");
    }

    private void generateMessages(TranslationBuilder translationBuilder) {
        translationBuilder.add(TranslationKeys.Message.HappyGhastWhistle.NoTarget, "No Happy Ghast nearby to call.");
        translationBuilder.add(TranslationKeys.Message.HappyGhastWhistle.Success, "A Happy Ghast heard your whistle!");
    }

    private void generateAdvancements(TranslationBuilder translationBuilder) {
        translationBuilder.add(TranslationKeys.Advancement.CalledHappyGhast.Title, "Heard and Answered");
        translationBuilder.add(TranslationKeys.Advancement.CalledHappyGhast.Description, "Blow the Happy Ghast Whistle and call a nearby Happy Ghast to you");
    }
}
