package com.hezaerd.datagen;

import com.hezaerd.PawsPals;
import com.hezaerd.advancement.CalledHappyGhastCriterion;
import com.hezaerd.advancement.ModCriteria;
import com.hezaerd.item.ModItems;
import com.hezaerd.utils.TranslationKeys;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.minecraft.data.advancements.AdvancementSubProvider.createPlaceholder;

public class PawsPalsAdvancementProvider extends FabricAdvancementProvider {
    public PawsPalsAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.@NonNull Provider registryLookup, @NonNull Consumer<AdvancementHolder> consumer) {
        AdvancementHolder ghastRoot = Advancement.Builder.advancement()
                .parent(createPlaceholder(Identifier.withDefaultNamespace("husbandry/place_dried_ghast_in_water").toString()))
                .display(
                        ModItems.HAPPY_GHAST_WHISTLE,
                        Component.translatable(TranslationKeys.Advancement.CalledHappyGhast.Title),
                        Component.translatable(TranslationKeys.Advancement.CalledHappyGhast.Description),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion("called_happy_ghast",
                        ModCriteria.CALLED_HAPPY_GHAST.createCriterion(
                                new CalledHappyGhastCriterion.Conditions(Optional.empty())))
                .requirements(AdvancementRequirements.Strategy.AND)
                .save(consumer, PawsPals.id("husbandry/called_happy_ghast").toString());
    }
}
