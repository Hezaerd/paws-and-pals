package com.hezaerd.datagen.lang;

import com.hezaerd.utils.TranslationKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class PawsPalsFrenchLangProvider extends FabricLanguageProvider {
    public PawsPalsFrenchLangProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(packOutput, "fr_fr", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider registryLookup, @NonNull TranslationBuilder translationBuilder) {
        generateItems(translationBuilder);
        generateMessages(translationBuilder);
        generateAdvancements(translationBuilder);
    }

    private void generateItems(TranslationBuilder translationBuilder) {
        translationBuilder.add(TranslationKeys.Item.HappyGhastWhistle, "Sifflet à ghast joyeux");
    }

    private void generateMessages(TranslationBuilder translationBuilder) {
        translationBuilder.add(TranslationKeys.Message.HappyGhastWhistle.NoTarget, "Aucun ghast joyeux n'a entendu votre appel.");
        translationBuilder.add(TranslationKeys.Message.HappyGhastWhistle.Success, "Un ghast joyeux a entendu votre sifflet!");
    }

    private void generateAdvancements(TranslationBuilder translationBuilder) {
        translationBuilder.add(TranslationKeys.Advancement.CalledHappyGhast.Title, "Répondez s'il ghast plait");
        translationBuilder.add(TranslationKeys.Advancement.CalledHappyGhast.Description, "Soufflez dans un sifflet à ghast joyeux pour qu'un ghast joyeux des environs vienne à vous");
    }
}
