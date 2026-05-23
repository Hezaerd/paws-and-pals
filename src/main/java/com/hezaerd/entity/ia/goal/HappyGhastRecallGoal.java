package com.hezaerd.entity.ia.goal;

import com.hezaerd.accessor.HappyGhastAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.EnumSet;

public class HappyGhastRecallGoal extends Goal {
    private static final double ARRIVAL_DISTANCE_SQ = 100.0; // ~10 blocks
    private static final double TELEPORT_DISTANCE_SQ = 2304.0; // 48 blocks

    private final HappyGhast ghast;
    private final HappyGhastAccessor accessor;
    @Nullable
    private Player target;

    public HappyGhastRecallGoal(HappyGhast ghast) {
        this.ghast = ghast;
        this.accessor = (HappyGhastAccessor) ghast;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.accessor.pawspals$getRecallTicks() <= 0) return false;
        if (this.ghast.isBaby() || this.ghast.isVehicle() || this.ghast.isOnStillTimeout() || this.ghast.isLeashed()) {
            this.accessor.pawspals$clearRecall();
            return false;
        }

        this.target = this.resolveTarget();
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        if (this.target == null) return;

        int ticksLeft = this.accessor.pawspals$getRecallTicks() - 1;
        this.accessor.pawspals$setRecallTicks(ticksLeft);

        if (this.ghast.distanceToSqr(this.target) <= ARRIVAL_DISTANCE_SQ || ticksLeft <= 0) {
            this.accessor.pawspals$clearRecall();
            return;
        }

        if (this.ghast.distanceToSqr(this.target) >= TELEPORT_DISTANCE_SQ) {
            this.tryTeleportNearTarget();
            return;
        }

        Vec3 hoverTarget = this.target.position().add(0.0, 6.0, 0.0);
        this.ghast.getMoveControl().setWantedPosition(
                hoverTarget.x, hoverTarget.y, hoverTarget.z, 1.25
        );
        this.ghast.getLookControl().setLookAt(this.target, 10.0F, this.ghast.getMaxHeadXRot());
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Nullable
    private Player resolveTarget() {
        if (!(this.ghast.level() instanceof ServerLevel serverLevel)) return null;

        var uuid = this.accessor.pawspals$getRecallTarget();
        if (uuid == null) return null;

        Player player = serverLevel.getServer().getPlayerList().getPlayer(uuid);
        if (player == null || !player.isAlive() || player.isSpectator()) {
            this.accessor.pawspals$clearRecall();
            return null;
        }
        return player;
    }

    private void tryTeleportNearTarget() {
        if (this.target == null) return;
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = this.target.getBlockX() + this.ghast.getRandom().nextIntBetweenInclusive(-4, 4);
            int z = this.target.getBlockZ() + this.ghast.getRandom().nextIntBetweenInclusive(-4, 4);
            int y = this.target.getBlockY() + this.ghast.getRandom().nextIntBetweenInclusive(4, 8);
            if (this.ghast.level().isEmptyBlock(new net.minecraft.core.BlockPos(x, y, z))
                    && this.ghast.level().isEmptyBlock(new net.minecraft.core.BlockPos(x, y + 1, z))) {
                this.ghast.snapTo(x + 0.5, y, z + 0.5, this.ghast.getYRot(), this.ghast.getXRot());
                this.ghast.getMoveControl().setWait();
                return;
            }
        }
    }
}
