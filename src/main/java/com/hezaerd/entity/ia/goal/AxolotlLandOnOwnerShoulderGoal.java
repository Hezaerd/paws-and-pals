package com.hezaerd.entity.ia.goal;

import com.hezaerd.accessor.AxolotlAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

public class AxolotlLandOnOwnerShoulderGoal extends Goal {
    private static final int RIDE_COOLDOWN_TICKS = 100;

    private final Axolotl axolotl;
    private final AxolotlAccessor accessor;
    private boolean isSittingOnShoulder;

    public AxolotlLandOnOwnerShoulderGoal(Axolotl axolotl) {
        this.axolotl = axolotl;
        this.accessor = (AxolotlAccessor) axolotl;
    }

    @Override
    public boolean canUse() {
        if (!(this.accessor.pawspals$getOwner() instanceof ServerPlayer owner)) {
            return false;
        }
        boolean ownerCanBeSatOn = !owner.isSpectator()
                && !owner.getAbilities().flying
                && !owner.isInWater()
                && !owner.isInPowderSnow;
        return !this.accessor.pawspals$isOrderedToSit()
                && ownerCanBeSatOn
                && this.axolotl.tickCount > RIDE_COOLDOWN_TICKS;
    }

    @Override
    public boolean isInterruptable() {
        return !this.isSittingOnShoulder;
    }

    @Override
    public void start() {
        this.isSittingOnShoulder = false;
    }

    @Override
    public void tick() {
        if (!this.isSittingOnShoulder
                && !this.accessor.pawspals$isInSittingPose()
                && !this.axolotl.isLeashed()) {
            if (this.accessor.pawspals$getOwner() instanceof ServerPlayer owner
                    && this.axolotl.getBoundingBox().intersects(owner.getBoundingBox())) {
                this.isSittingOnShoulder = this.accessor.pawspals$setEntityOnShoulder(owner);
            }
        }
    }
}
