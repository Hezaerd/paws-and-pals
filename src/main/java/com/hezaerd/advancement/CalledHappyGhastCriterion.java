package com.hezaerd.advancement;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class CalledHappyGhastCriterion extends SimpleCriterionTrigger<CalledHappyGhastCriterion.Conditions> {
    @Override
    public @NonNull Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, conditions -> true);
    }

    public record Conditions(Optional<ContextAwarePredicate> player) implements SimpleInstance {
        public static final Codec<Conditions> CODEC =
                ContextAwarePredicate.CODEC.optionalFieldOf("player")
                        .xmap(Conditions::new, Conditions::player)
                        .codec();

        @Override
        public @NonNull Optional<ContextAwarePredicate> player() {
            return this.player;
        }
    }

}
