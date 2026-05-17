package com.hezaerd.mixin;

import com.hezaerd.accessor.AvatarRenderStateAccessor;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements AvatarRenderStateAccessor {
    @Unique
    public Axolotl.@Nullable Variant axolotlOnLeftShoulder;

    @Unique
    public Axolotl.@Nullable Variant axolotlOnRightShoulder;

    @Override
    public Axolotl.@Nullable Variant pawspals$getAxolotlOnLeftShoulder() {
        return axolotlOnLeftShoulder;
    }

    @Override
    public void pawspals$setAxolotlOnLeftShoulder(Axolotl.@Nullable Variant variant) {
        this.axolotlOnLeftShoulder = variant;
    }

    @Override
    public Axolotl.@Nullable Variant pawspals$getAxolotlOnRightShoulder() {
        return axolotlOnRightShoulder;
    }

    @Override
    public void pawspals$setAxolotlOnRightShoulder(Axolotl.@Nullable Variant variant) {
        this.axolotlOnRightShoulder = variant;
    }
}
