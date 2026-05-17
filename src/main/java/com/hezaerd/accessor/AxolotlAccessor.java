package com.hezaerd.accessor;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface AxolotlAccessor {
    boolean pawspals$isTame();
    void pawspals$setTame(boolean isTame, boolean includeSideEffects);
    void pawspals$tame(Player player);
    void pawspals$setOwner(@Nullable LivingEntity owner);
    @Nullable LivingEntity pawspals$getOwner();
    boolean pawspals$isOwnedBy(LivingEntity entity);
    boolean pawspals$isOrderedToSit();
    void pawspals$setOrderedToSit(boolean orderedToSit);
    boolean pawspals$isInSittingPose();
    void pawspals$setInSittingPose(boolean value);
    boolean pawspals$shouldTryTeleportToOwner();
    void pawspals$tryToTeleportToOwner();
    boolean pawspals$unableToMoveToOwner();
    void pawspals$spawnTamingParticles(boolean success);
    boolean pawspals$setEntityOnShoulder(ServerPlayer player);
}
