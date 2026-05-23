package com.hezaerd.datagen;

import com.hezaerd.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class PawsPalsRecipeProvider extends FabricRecipeProvider {
    public PawsPalsRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registryLookup, @NonNull RecipeOutput exporter) {
        return new RecipeProvider(registryLookup, exporter) {
            @Override
            public void buildRecipes() {
                HolderLookup.RegistryLookup<Item> itemLookup = registries.lookupOrThrow(Registries.ITEM);

                shaped(RecipeCategory.TOOLS, ModItems.HAPPY_GHAST_WHISTLE, 1)
                        .pattern(" E ")
                        .pattern("GTG")
                        .pattern(" G ")
                        .define('E', Items.ECHO_SHARD)
                        .define('G', Items.GHAST_TEAR)
                        .define('T', Items.COPPER_INGOT)
                        .group("multi_bench")
                        .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                        .save(output);

            }
        };
    }

    @Override
    public @NonNull String getName() {
        return "PawsPalsRecipeProvider";
    }
}
