package com.hezaerd.accessor;

import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public interface PlayerAccessor {
    void pawspals$setShoulderAxolotlLeft(Axolotl.@Nullable Variant variant);

    Optional<Axolotl.Variant> pawspals$getShoulderAxolotlLeft();

    void pawspals$setShoulderAxolotlRight(Axolotl.@Nullable Variant variant);

    Optional<Axolotl.Variant> pawspals$getShoulderAxolotlRight();
}
