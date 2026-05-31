package net.zimi.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class FireOverlayMixin {

    @Unique
    private static int customFireTicks = 0;
    @Unique
    private static int maxFireTicks = 0;
    @Unique
    private static int lastPlayerTickCount = 0;

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void customRenderFire(PoseStack poseStack, MultiBufferSource bufferSource, TextureAtlasSprite sprite, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            if (player.tickCount != lastPlayerTickCount) {
                lastPlayerTickCount = player.tickCount;
                if (customFireTicks > 0) {
                    customFireTicks--;
                }
            }

            int realTicks = player.getRemainingFireTicks();
            if (realTicks > customFireTicks) {
                customFireTicks = realTicks;
                maxFireTicks = realTicks;
            }

            if (customFireTicks <= 0) {
                customFireTicks = 300;
                maxFireTicks = 300;
            }

            float percentLeft = (float) customFireTicks / maxFireTicks;
            percentLeft = Math.max(0.0f, Math.min(1.0f, percentLeft));

            float yOffset = (1.0f - percentLeft) * 0.35f;
            float opacity = 0.3f + (percentLeft * 0.6f);

            VertexConsumer builder = bufferSource.getBuffer(RenderTypes.fireScreenEffect(sprite.atlasLocation()));
            float u0 = sprite.getU0();
            float u1 = sprite.getU1();
            float v0 = sprite.getV0();
            float v1 = sprite.getV1();

            for (int i = 0; i < 2; ++i) {
                poseStack.pushPose();

                poseStack.translate((float)(-(i * 2 - 1)) * 0.24F, -0.3F - yOffset, 0.0F);
                poseStack.mulPose(Axis.YP.rotationDegrees((float)(i * 2 - 1) * 10.0F));

                Matrix4f pose = poseStack.last().pose();

                builder.addVertex(pose, -0.5F, -0.5F, -0.5F).setUv(u1, v1).setColor(1.0F, 1.0F, 1.0F, opacity);
                builder.addVertex(pose, 0.5F, -0.5F, -0.5F).setUv(u0, v1).setColor(1.0F, 1.0F, 1.0F, opacity);
                builder.addVertex(pose, 0.5F, 0.5F, -0.5F).setUv(u0, v0).setColor(1.0F, 1.0F, 1.0F, opacity);
                builder.addVertex(pose, -0.5F, 0.5F, -0.5F).setUv(u1, v0).setColor(1.0F, 1.0F, 1.0F, opacity);

                poseStack.popPose();
            }

            ci.cancel();
        }
    }
}