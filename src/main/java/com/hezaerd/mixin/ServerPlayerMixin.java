package com.hezaerd.mixin;

import com.hezaerd.accessor.PlayerAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Inject(method = "setShoulderEntityLeft", at = @At("TAIL"))
    private void pawspals$onSetShoulderEntityLeft(CompoundTag tag, CallbackInfo ci) {
        ((PlayerAccessor) this).pawspals$setShoulderAxolotlLeft(pawspals$extractAxolotlVariant(tag));
    }

    @Inject(method = "setShoulderEntityRight", at = @At("TAIL"))
    private void pawspals$onSetShoulderEntityRight(CompoundTag tag, CallbackInfo ci) {
        ((PlayerAccessor) this).pawspals$setShoulderAxolotlRight(pawspals$extractAxolotlVariant(tag));
    }

    @Unique
    private static Axolotl.@Nullable Variant pawspals$extractAxolotlVariant(CompoundTag tag) {
        if (!tag.isEmpty()) {
            EntityType<?> entityType = tag.read("id", EntityType.CODEC).orElse(null);
            if (entityType == EntityType.AXOLOTL) {
                return tag.read("Variant", Axolotl.Variant.LEGACY_CODEC).orElse(null);
            }
        }
        return null;
    }
}
