package com.hezaerd.accessor;

import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public interface HappyGhastAccessor {
    int pawspals$getRecallTicks();

    void pawspals$setRecallTicks(int ticks);

    @Nullable UUID pawspals$getRecallTarget();

    void pawspals$setRecallTarget(@Nullable UUID playerUuid);

    default void pawspals$startRecall(Player player, int ticks) {
        this.pawspals$setRecallTarget(player.getUUID());
        this.pawspals$setRecallTicks(ticks);
    }

    default void pawspals$clearRecall() {
        this.pawspals$setRecallTarget(null);
        this.pawspals$setRecallTicks(0);
    }
}
