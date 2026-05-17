package com.hezaerd.accessor;

import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.jspecify.annotations.Nullable;

public interface AvatarRenderStateAccessor {
    Axolotl.@Nullable Variant pawspals$getAxolotlOnLeftShoulder();

    void pawspals$setAxolotlOnLeftShoulder(Axolotl.@Nullable Variant variant);

    Axolotl.@Nullable Variant pawspals$getAxolotlOnRightShoulder();

    void pawspals$setAxolotlOnRightShoulder(Axolotl.@Nullable Variant variant);
}
