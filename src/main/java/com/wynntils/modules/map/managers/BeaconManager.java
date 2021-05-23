/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.map.managers;

import com.wynntils.McIf;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.utils.objects.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import static com.wynntils.transition.GlStateManager.*;

public class BeaconManager {

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final ResourceLocation beamResource = new ResourceLocation("textures/entity/beacon_beam.png");

    public static void drawBeam(Location loc, CustomColor color, float partialTicks) {
        EntityRendererManager renderManager = McIf.mc().getEntityRenderDispatcher();
        if (renderManager.renderViewEntity == null) return;

        float alpha = 1f;

        Vector3d positionVec = new Vector3d(loc.getX(), loc.getY() + 0.118D, loc.getZ());
        Vector3d playerVec = renderManager.renderViewEntity.getPositionVector();

        double distance = playerVec.distanceTo(positionVec);
        if (distance <= 4f || distance > 4000f) return;
        if (distance <= 8f) alpha = (float)(distance - 4f) / 3f;

        if (alpha > 1) alpha = 1;  // avoid excessive values

        alpha *= color.a;

        double maxDistance = McIf.mc().options.renderDistanceChunks * 16d;
        if (distance > maxDistance) {  // this will drag the beam to the visible area if outside of it
            // partial ticks aren't factored into player pos, so if we're going to use it for rendering, we need to recalculate to account for partial ticks
            Vector3d prevPosVec = new Vector3d(renderManager.renderViewEntity.prevPosX, renderManager.renderViewEntity.prevPosY, renderManager.renderViewEntity.prevPosZ);
            playerVec = playerVec.subtract(prevPosVec).scale(partialTicks).add(prevPosVec);

            Vector3d delta = positionVec.subtract(playerVec).normalize();
            positionVec = playerVec.add(delta.x * maxDistance, delta.y * maxDistance, delta.z * maxDistance);
        }

        drawBeam(positionVec.x - renderManager.viewerPosX, -renderManager.viewerPosY, positionVec.z - renderManager.viewerPosZ, alpha, color);
    }

    private static void drawBeam(double x, double y, double z, float alpha, CustomColor color) {
        pushAttrib();
        {
            McIf.mc().renderEngine.bind(beamResource);  // binds the texture
            glTexParameteri(3553, 10242, 10497);

            // beacon light animation
            float time = McIf.getSystemTime() / 50F;
            float offset = -(-time * 0.2F - MathHelper.fastFloor(-time * 0.1F)) * 0.6F;

            // positions
            double d1 = 256.0F * alpha;
            double d2 = -1f + offset;
            double d3 = 256.0F * alpha + d2;

            disableLighting();
            enableDepth();
            disableCull();
            enableBlend();
            tryBlendFuncSeparate(770, 771, 1, 0);
            color(1f, 1f, 1f, 1f);

            // drawing
            tessellator.getBuilder().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            BufferBuilder builder = tessellator.getBuilder();
            {
                builder.vertex(x + .2d, y + d1, z + .2d).uv(1d, d3).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .2d, y, z + .2d).uv(1d, d2).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .8d, y, z + .2d).uv(0d, d2).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .8d, y + d1, z + .2d).uv(0d, d3).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .8d, y + d1, z + .8d).uv(1d, d3).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .8d, y, z + .8d).uv(1d, d2).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .2d, y, z + .8d).uv(0d, d2).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .2d, y + d1, z + .8d).uv(0d, d3).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .8d, y + d1, z + .2d).uv(1d, d3).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .8d, y, z + .2d).uv(1d, d2).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .8d, y, z + .8d).uv(0d, d2).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .8d, y + d1, z + .8d).uv(0d, d3).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .2d, y + d1, z + .8d).uv(1d, d3).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .2d, y, z + .8d).uv(1d, d2).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .2d, y, z + .2d).uv(0d, d2).color(color.r, color.g, color.b, alpha).endVertex();
                builder.vertex(x + .2d, y + d1, z + .2d).uv(0d, d3).color(color.r, color.g, color.b, alpha).endVertex();
            }
            tessellator.end();

            // resetting
            color(1f, 1f, 1f, 1f);
            disableBlend();
            enableCull();
        }
        popAttrib();
    }

}
