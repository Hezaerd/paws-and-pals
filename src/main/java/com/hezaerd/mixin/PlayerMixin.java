package com.hezaerd.mixin;

import com.hezaerd.accessor.PlayerAccessor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.OptionalInt;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerAccessor {
    @Unique
    private static final EntityDataAccessor<OptionalInt> PAWSPALS_SHOULDER_AXOLOTL_LEFT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

    @Unique
    private static final EntityDataAccessor<OptionalInt> PAWSPALS_SHOULDER_AXOLOTL_RIGHT = SynchedEntityData.defineId(Player.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void pawspals$defineSynchedData(SynchedEntityData.Builder entityData, CallbackInfo ci) {
        entityData.define(PAWSPALS_SHOULDER_AXOLOTL_LEFT, OptionalInt.empty());
        entityData.define(PAWSPALS_SHOULDER_AXOLOTL_RIGHT, OptionalInt.empty());
    }

    @Override
    public void pawspals$setShoulderAxolotlLeft(Axolotl.@Nullable Variant variant) {
        ((Player) (Object) this).getEntityData().set(PAWSPALS_SHOULDER_AXOLOTL_LEFT,
                variant != null ? OptionalInt.of(variant.getId()) : OptionalInt.empty());
    }

    @Override
    public Optional<Axolotl.Variant> pawspals$getShoulderAxolotlLeft() {
        return pawspals$intToVariant(((Player) (Object) this).getEntityData().get(PAWSPALS_SHOULDER_AXOLOTL_LEFT));
    }

    @Override
    public void pawspals$setShoulderAxolotlRight(Axolotl.@Nullable Variant variant) {
        ((Player) (Object) this).getEntityData().set(PAWSPALS_SHOULDER_AXOLOTL_RIGHT,
                variant != null ? OptionalInt.of(variant.getId()) : OptionalInt.empty());
    }

    @Override
    public Optional<Axolotl.Variant> pawspals$getShoulderAxolotlRight() {
        return pawspals$intToVariant(((Player) (Object) this).getEntityData().get(PAWSPALS_SHOULDER_AXOLOTL_RIGHT));
    }

    @Unique
    private static Optional<Axolotl.Variant> pawspals$intToVariant(OptionalInt id) {
        return id.isPresent() ? Optional.of(Axolotl.Variant.byId(id.getAsInt())) : Optional.empty();
    }
}
