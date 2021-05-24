/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.ui.elements;

import java.awt.Color;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class GuiTextFieldWynn extends TextFieldWidget {

    private static final Color TEXT_FIELD_COLOR_1 = new Color(87, 65, 51);
    private static final Color TEXT_FIELD_COLOR_2 = new Color(120, 90, 71);

    public GuiTextFieldWynn(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height) {

        // TextFieldWidget(McIf.mc().font, this.position.getDrawingX(), this.position.getDrawingY(), width, 20, McIf.toTextComponent(text));
        super(fontrendererObj, x, y, width, height, McIf.toTextComponent(""));

        this.setBordered(false);
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        fill(matrices, this.x - 2, this.y - 1, this.x + this.width - 1, this.y + this.height - 1, TEXT_FIELD_COLOR_1.getRGB());
        fill(matrices, this.x -1, this.y, this.x + this.width - 2, this.y + this.height - 2, TEXT_FIELD_COLOR_2.getRGB());
        super.renderButton(matrices, mouseX, mouseY, partialTicks);
    }
}
