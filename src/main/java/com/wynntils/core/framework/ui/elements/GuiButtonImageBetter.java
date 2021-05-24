/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.ui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ImageButton;
import com.wynntils.transition.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * Fixes net.minecraft.client.gui.widget.button.ImageButton so that it highlights properly
 * and can also be scaled
 */
public class GuiButtonImageBetter extends ImageButton {
    //    public Button(int x, int y, int w, int h, ITextComponent msg, Button.IPressable p_i232255_6_) {
    private static final Button highlightFixHovering = new Button(Integer.MIN_VALUE, Integer.MIN_VALUE, 1, 1, McIf.toTextComponent(""), ignored -> {});
    private static final Button highlightFixNoHovering = new Button(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0, McIf.toTextComponent(""), ignored -> {});

    private float scaleFactor;
    private float scaleFromX;
    private float scaleFromY;
    private int scaledStartX;
    private int scaledStartY;
    private int scaledEndX;
    private int scaledEndY;
    private boolean highlight;

    /*
       public ImageButton(int p_i244513_1_, int p_i244513_2_, int p_i244513_3_, int p_i244513_4_, int p_i244513_5_, int p_i244513_6_, int p_i244513_7_, ResourceLocation p_i244513_8_, int p_i244513_9_, int p_i244513_10_, Button.IPressable p_i244513_11_, Button.ITooltip p_i244513_12_, ITextComponent p_i244513_13_) {
      super(p_i244513_1_, p_i244513_2_, p_i244513_3_, p_i244513_4_, p_i244513_13_, p_i244513_11_, p_i244513_12_);
      this.textureWidth = p_i244513_9_;
      this.textureHeight = p_i244513_10_;
      this.xTexStart = p_i244513_5_;
      this.yTexStart = p_i244513_6_;
      this.yDiffTex = p_i244513_7_;
      this.resourceLocation = p_i244513_8_;
     */
    public GuiButtonImageBetter(int x, int y, int width, int height, int textureOffsetX, int textureOffsetY, int hoverImageYOffset, ResourceLocation resource, Button.IPressable pressable) {
        super(x, y, width, height, textureOffsetX, textureOffsetY, hoverImageYOffset, resource, null);
        setScaleFactor(1, width / 2f, height / 2f);
        highlight = true;
    }

    public GuiButtonImageBetter setScaleFactor(float scaleFactor, float originX, float originY) {
        this.scaleFactor = scaleFactor;
        this.scaleFromX = originX + x;
        this.scaleFromY = originY + y;
        float scaledStartX = scaleFromX - (scaleFromX - x) * scaleFactor;
        float scaledStartY = scaleFromY - (scaleFromY - y) * scaleFactor;
        this.scaledStartX = MathHelper.floor(scaledStartX);
        this.scaledStartY = MathHelper.floor(scaledStartY);
        this.scaledEndX = MathHelper.ceil(scaledStartX + width * scaleFactor);
        this.scaledEndY = MathHelper.ceil(scaledStartY + width * scaleFactor);
        return this;
    }

    public GuiButtonImageBetter setScaleFactor(float scaleFactor) {
        setScaleFactor(scaleFactor, scaleFromX - x, scaleFromY - y);
        return this;
    }

    public GuiButtonImageBetter setScaleOrigin(float originX, float originY) {
        setScaleFactor(scaleFactor, originX, originY);
        return this;
    }

    /**
     * If `doHighlight(false)` is called, doesn't highlight (yellow tint) when hovering
     */
    public GuiButtonImageBetter doHighlight(boolean highlight) {
        this.highlight = highlight;
        return this;
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        setColour(this.highlight && mouseX >= scaledStartX && mouseY >= scaledStartY && mouseX < scaledEndX && mouseY < scaledEndY, this.active);

        if (scaleFactor != 1f) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-scaleFromX, -scaleFromY, 0);
            GlStateManager.scale(scaleFactor, scaleFactor, 1);
            GlStateManager.translate(scaleFromX, scaleFromY, 0);
        }
        super.renderButton(matrices, mouseX, mouseY, partialTicks);
        if (scaleFactor != 1f) {
            GlStateManager.popMatrix();
        }

        setColour(false, true);
    }

    public static void setColour(boolean hovering, boolean active) {
        if (hovering) {
            highlightFixHovering.active = active;
            highlightFixHovering.renderButton(new MatrixStack(), Integer.MIN_VALUE, Integer.MIN_VALUE, 0);
        } else {
            highlightFixNoHovering.active = active;
            highlightFixNoHovering.renderButton(new MatrixStack(), 0, 0, 0);
        }
    }
}
