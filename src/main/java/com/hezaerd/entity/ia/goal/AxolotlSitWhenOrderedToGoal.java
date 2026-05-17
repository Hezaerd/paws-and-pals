package com.hezaerd.entity.ia.goal;

import com.hezaerd.accessor.AxolotlAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

import java.util.EnumSet;

public class AxolotlSitWhenOrderedToGoal extends Goal {
    private final Axolotl axolotl;
    private final AxolotlAccessor accessor;

    public AxolotlSitWhenOrderedToGoal(Axolotl axolotl) {
        this.axolotl = axolotl;
        this.accessor = (AxolotlAccessor) axolotl;
        this.setFlags(EnumSet.of(Flag.JUMP, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.accessor.pawspals$isTame()) return false;
        if (!this.accessor.pawspals$isOrderedToSit()) return false;
        if (!this.axolotl.isInWater() && !this.axolotl.onGround()) return false;
        LivingEntity owner = this.accessor.pawspals$getOwner();
        if (owner == null || owner.level() != this.axolotl.level()) return true;
        if (this.axolotl.distanceToSqr(owner) < 144.0
                && owner.getLastHurtByMob() != null) return false;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.accessor.pawspals$isOrderedToSit();
    }

    @Override
    public void start() {
        this.axolotl.getNavigation().stop();
        this.accessor.pawspals$setInSittingPose(true);
    }

    @Override
    public void stop() {
        this.accessor.pawspals$setInSittingPose(false);
    }
}
