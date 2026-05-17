package com.hezaerd.entity.ia.goal;

import com.hezaerd.accessor.AxolotlAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public class AxolotlFollowOwnerGoal extends Goal {
    private final Axolotl axolotl;
    private final AxolotlAccessor accessor;
    private final double speedModifier;
    private final PathNavigation navigation;
    private final float startDistance;
    private final float stopDistance;
    @Nullable
    private LivingEntity owner;
    private int timeToRecalcPath;
    private float oldWaterCost;

    public AxolotlFollowOwnerGoal(Axolotl axolotl, double speedModifier,
                                  float startDistance, float stopDistance) {
        this.axolotl = axolotl;
        this.accessor = (AxolotlAccessor) axolotl;
        this.speedModifier = speedModifier;
        this.navigation = axolotl.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity owner = this.accessor.pawspals$getOwner();
        if (owner == null) return false;
        if (this.accessor.pawspals$unableToMoveToOwner()) return false;
        if (this.axolotl.distanceToSqr(owner) < this.startDistance * this.startDistance)
            return false;
        this.owner = owner;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.navigation.isDone()) return false;
        if (this.accessor.pawspals$unableToMoveToOwner()) return false;
        return this.axolotl.distanceToSqr(this.owner)
                > this.stopDistance * this.stopDistance;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.axolotl.getPathfindingMalus(PathType.WATER);
        this.axolotl.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.axolotl.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
    }

    @Override
    public void tick() {
        boolean isFar = this.accessor.pawspals$shouldTryTeleportToOwner();
        if (!isFar) {
            this.axolotl.getLookControl().setLookAt(
                    this.owner, 10.0F, this.axolotl.getMaxHeadXRot());
        }
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (isFar) {
                this.accessor.pawspals$tryToTeleportToOwner();
            } else {
                this.navigation.moveTo(this.owner, this.speedModifier);
            }
        }
    }
}
