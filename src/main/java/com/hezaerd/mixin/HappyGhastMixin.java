package com.hezaerd.mixin;

import com.hezaerd.accessor.HappyGhastAccessor;
import com.hezaerd.entity.ia.goal.HappyGhastRecallGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(HappyGhast.class)
public abstract class HappyGhastMixin extends Animal implements HappyGhastAccessor {
    @Unique
    private int pawspals$recallTicks;

    @Unique
    @Nullable
    private UUID pawspals$recallTarget;

    protected HappyGhastMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void pawspals$registerRecallGoal(CallbackInfo ci) {
        HappyGhast self = (HappyGhast) (Object) this;
        this.goalSelector.addGoal(2, new HappyGhastRecallGoal(self));
    }

    @Override
    public int pawspals$getRecallTicks() {
        return this.pawspals$recallTicks;
    }

    @Override
    public void pawspals$setRecallTicks(int ticks) {
        this.pawspals$recallTicks = Math.max(0, ticks);
    }

    @Override
    @Nullable
    public UUID pawspals$getRecallTarget() {
        return this.pawspals$recallTarget;
    }

    @Override
    public void pawspals$setRecallTarget(@Nullable UUID playerUuid) {
        this.pawspals$recallTarget = playerUuid;
    }
}
