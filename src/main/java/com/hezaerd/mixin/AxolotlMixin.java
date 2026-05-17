package com.hezaerd.mixin;

import com.hezaerd.accessor.AxolotlAccessor;
import com.hezaerd.entity.ia.goal.AxolotlFollowOwnerGoal;
import com.hezaerd.entity.ia.goal.AxolotlSitWhenOrderedToGoal;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(Axolotl.class)
public abstract class AxolotlMixin extends Animal implements AxolotlAccessor, OwnableEntity {
    @Unique
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID =
            SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.BYTE);
    @Unique
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNERUUID_ID =
            SynchedEntityData.defineId(Axolotl.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    @Unique
    private static final int TELEPORT_DISTANCE_SQ = 144;

    @Unique
    private boolean orderedToSit = false;

    protected AxolotlMixin(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void pawspals$defineSynchedData(SynchedEntityData.Builder entityData, CallbackInfo ci) {
        entityData.define(DATA_FLAGS_ID, (byte) 0);
        entityData.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void pawspals$addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        EntityReference.store(this.getOwnerReference(), output, "pawspals:Owner");
        output.putBoolean("pawspals:Tame", this.pawspals$isTame());
        output.putBoolean("pawspals:Sitting", this.orderedToSit);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void pawspals$readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        if (input.getBooleanOr("pawspals:Tame", false)) {
            EntityReference<LivingEntity> owner = EntityReference.read(input, "pawspals:Owner");
            if (owner != null) {
                this.entityData.set(DATA_OWNERUUID_ID, Optional.of(owner));
            }
            this.pawspals$setTame(true, true);
        }
        this.orderedToSit = input.getBooleanOr("pawspals:Sitting", false);
        this.pawspals$setInSittingPose(this.orderedToSit);
    }

    @Inject(method = "saveToBucketTag", at = @At("TAIL"))
    private void pawspals$saveToBucketTag(ItemStack bucket, CallbackInfo ci) {
        if (!this.pawspals$isTame()) return;
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, bucket, tag -> {
            tag.putBoolean("pawspals:Tame", true);
            tag.putBoolean("pawspals:Sitting", this.orderedToSit);
            EntityReference<LivingEntity> ref = this.getOwnerReference();
            if (ref != null) {
                tag.putString("pawspals:OwnerUUID", ref.getUUID().toString());
            }
        });
    }

    @Inject(method = "loadFromBucketTag", at = @At("TAIL"))
    private void pawspals$loadFromBucketTag(CompoundTag tag, CallbackInfo ci) {
        if (!tag.getBooleanOr("pawspals:Tame", false)) return;
        tag.getString("pawspals:OwnerUUID").ifPresent(uuidStr -> {
            try {
                this.entityData.set(DATA_OWNERUUID_ID,
                        Optional.of(EntityReference.of(UUID.fromString(uuidStr))));
            } catch (IllegalArgumentException ignored) {}
        });
        this.pawspals$setTame(true, true);
        this.orderedToSit = tag.getBooleanOr("pawspals:Sitting", false);
        this.pawspals$setInSittingPose(this.orderedToSit);
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void pawspals$mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = player.getItemInHand(hand);
        Axolotl self = (Axolotl) (Object) this;
        if (this.pawspals$isTame()) {
            if (itemStack.is(Items.TROPICAL_FISH) && self.getHealth() < self.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    itemStack.consume(1, player);
                    self.heal(3.0F);
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
                return;
            }
            if (self.isFood(itemStack) || itemStack.is(Items.WATER_BUCKET)) return;
            if (this.pawspals$isOwnedBy(player)) {
                if (!this.level().isClientSide()) {
                    this.pawspals$setOrderedToSit(!this.orderedToSit);
                    this.jumping = false;
                    this.getNavigation().stop();

                    player.sendSystemMessage(Component.literal("sit"));
                }
                cir.setReturnValue(InteractionResult.SUCCESS.withoutItem());
            }
            return;
        }
        if (itemStack.is(Items.TROPICAL_FISH)) {
            if (!this.level().isClientSide()) {
                itemStack.consume(1, player);
                if (this.random.nextInt(3) == 0) {
                    this.pawspals$tame(player);
                    this.getNavigation().stop();
                    this.pawspals$setOrderedToSit(true);
                    this.level().broadcastEntityEvent(self, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(self, (byte) 6);
                }
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }



    @Inject(method = "customServerAiStep", at = @At("HEAD"), cancellable = true)
    private void pawspals$onCustomServerAiStep(ServerLevel level, CallbackInfo ci) {
        if (!this.pawspals$isTame()) return;
        ci.cancel();
        Axolotl self = (Axolotl) (Object) this;
        if (self.isNoAi()) return;
        Brain<Axolotl> brain = self.getBrain();
        Optional<Integer> playDeadTicks = brain.getMemory(MemoryModuleType.PLAY_DEAD_TICKS);
        self.setPlayingDead(playDeadTicks.isPresent() && playDeadTicks.get() > 0);
        playDeadTicks.ifPresent(ticks -> {
            if (ticks <= 1) {
                brain.eraseMemory(MemoryModuleType.PLAY_DEAD_TICKS);
                brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
            } else {
                brain.setMemory(MemoryModuleType.PLAY_DEAD_TICKS, ticks - 1);
            }
        });
    }

    @Inject(method = "handleAirSupply", at = @At("HEAD"), cancellable = true)
    private void pawspals$onHandleAirSupply(ServerLevel level, int preTickAirSupply, CallbackInfo ci) {
        if (this.pawspals$isTame()) {
            ci.cancel();
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    @Inject(method = "removeWhenFarAway", at = @At("HEAD"), cancellable = true)
    private void pawspals$onRemoveWhenFarAway(double distSqr, CallbackInfoReturnable<Boolean> cir) {
        if (this.pawspals$isTame()) cir.setReturnValue(false);
    }

    @Inject(method = "requiresCustomPersistence", at = @At("HEAD"), cancellable = true)
    private void pawspals$onRequiresCustomPersistence(CallbackInfoReturnable<Boolean> cir) {
        if (this.pawspals$isTame()) cir.setReturnValue(true);
    }

    @Override
    public void die(@NonNull final DamageSource source) {
        if (this.level() instanceof ServerLevel serverLevel
                && serverLevel.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES)
                && this.getOwner() instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(this.getCombatTracker().getDeathMessage());
        }

        super.die(source);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void pawspals$onInit(EntityType<? extends Axolotl> type, Level level,
                                 CallbackInfo ci) {
        this.goalSelector.addGoal(1, new AxolotlSitWhenOrderedToGoal((Axolotl) (Object) this));
        this.goalSelector.addGoal(4, new AxolotlFollowOwnerGoal((Axolotl) (Object) this, 1.0, 10.0F, 2.0F));
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getOwnerReference() {
        return this.entityData.get(DATA_OWNERUUID_ID).orElse(null);
    }

    @Override
    public boolean pawspals$isTame() {
        return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
    }

    @Override
    public void pawspals$setTame(boolean isTame, boolean includeSideEffects) {
        byte current = this.entityData.get(DATA_FLAGS_ID);
        this.entityData.set(DATA_FLAGS_ID, isTame
                ? (byte) (current | 4)
                : (byte) (current & ~4));
        if (includeSideEffects) pawspals$applyTamingSideEffects();
    }

    @Unique
    public void pawspals$tame(Player player) {
        this.pawspals$setTame(true, true);
        this.pawspals$setOwner(player);
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger(serverPlayer, this);
        }
    }

    @Unique
    public void pawspals$setOwner(@Nullable final LivingEntity owner) {
        this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(owner).map(EntityReference::of));
    }

    @Nullable
    @Override
    public LivingEntity pawspals$getOwner() {
        return EntityReference.getLivingEntity(this.getOwnerReference(), this.level());
    }

    @Override
    public boolean pawspals$isOwnedBy(LivingEntity entity) {
        return entity == this.pawspals$getOwner();
    }

    @Override
    public boolean pawspals$isOrderedToSit() {
        return this.orderedToSit;
    }

    @Override
    public void pawspals$setOrderedToSit(boolean orderedToSit) {
        this.orderedToSit = orderedToSit;
        this.pawspals$setInSittingPose(orderedToSit);
    }

    @Override
    public boolean pawspals$isInSittingPose() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    @Override
    public void pawspals$setInSittingPose(boolean value) {
        byte current = this.entityData.get(DATA_FLAGS_ID);
        this.entityData.set(DATA_FLAGS_ID, value
                ? (byte) (current | 1)
                : (byte) (current & ~1));
    }

    @Override
    public boolean pawspals$shouldTryTeleportToOwner() {
        LivingEntity owner = this.pawspals$getOwner();
        return owner != null && this.distanceToSqr(owner) >= TELEPORT_DISTANCE_SQ;
    }

    @Override
    public void pawspals$tryToTeleportToOwner() {
        LivingEntity owner = this.pawspals$getOwner();
        if (owner != null) pawspals$teleportToAroundBlockPos(owner.blockPosition());
    }

    @Override
    public boolean pawspals$unableToMoveToOwner() {
        LivingEntity owner = this.pawspals$getOwner();
        return this.orderedToSit
                || this.isPassenger()
                || this.mayBeLeashed()
                || (owner != null && owner.isSpectator());
    }

    @Unique
    public void pawspals$applyTamingSideEffects() {
        if (this.pawspals$isTame()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0F);
            this.setHealth(40.0F);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(14.0F);
        }
    }

    @Unique
    public void pawspals$spawnTamingParticles(boolean success) {
        ParticleOptions particle = success ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        for (int i = 0; i < 7; i++) {
            double xa = this.random.nextGaussian() * 0.02;
            double ya = this.random.nextGaussian() * 0.02;
            double za = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particle,
                    this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0),
                    xa, ya, za);
        }
    }

    @Unique
    private void pawspals$teleportToAroundBlockPos(BlockPos targetPos) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int xd = this.random.nextIntBetweenInclusive(-3, 3);
            int zd = this.random.nextIntBetweenInclusive(-3, 3);
            if (Math.abs(xd) >= 2 || Math.abs(zd) >= 2) {
                int yd = this.random.nextIntBetweenInclusive(-1, 1);
                if (pawspals$maybeTeleportTo(
                        targetPos.getX() + xd,
                        targetPos.getY() + yd,
                        targetPos.getZ() + zd)) {
                    return;
                }
            }
        }
    }

    @Unique
    private boolean pawspals$maybeTeleportTo(int x, int y, int z) {
        if (!pawspals$canTeleportTo(new BlockPos(x, y, z))) return false;
        this.snapTo(x + 0.5, y, z + 0.5, this.getYRot(), this.getXRot());
        this.getNavigation().stop();
        return true;
    }

    @Unique
    private boolean pawspals$canTeleportTo(BlockPos pos) {
        PathType pathType = WalkNodeEvaluator.getPathTypeStatic(this, pos);
        if (pathType != PathType.WALKABLE && pathType != PathType.WATER) return false;
        BlockPos delta = pos.subtract(this.blockPosition());
        return this.level().noCollision(this, this.getBoundingBox().move(delta));
    }
}
