/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.ui.elements;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.core.framework.enums.MouseButton;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.ui.UI;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.util.function.BiConsumer;

public class UIETextBox extends UIEClickZone {
    public TextFieldWidget textField;
    public boolean textDisappearsOnNextClick;
    public BiConsumer<UI, String> onTextChanged;

    public UIETextBox(float anchorX, float anchorY, int offsetX, int offsetY, int width, boolean active, String text, boolean textDisappearsOnNextClick, BiConsumer<UI, String> onTextChanged) {
        super(anchorX, anchorY, offsetX, offsetY, width, SmartFontRenderer.CHAR_HEIGHT, active, null);

        this.textField = new TextFieldWidget(McIf.mc().font, this.position.getDrawingX(), this.position.getDrawingY(), width, 20, McIf.toTextComponent(text));
        this.textField.setValue(text);
        this.textDisappearsOnNextClick = textDisappearsOnNextClick;
        this.onTextChanged = onTextChanged;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        super.render(mouseX, mouseY);

        this.textField.x = this.position.getDrawingX();
        this.textField.y = this.position.getDrawingY();
        this.textField.setEditable(active);
        float partialTicks = 0.0f; // not used by textField
        this.textField.renderButton(new MatrixStack(), mouseX, mouseY, partialTicks);
    }

    public boolean keyPressed(int c, int i, int j, UI ui) {
        String old = textField.getValue();
        boolean result = this.textField.keyPressed(c, i, j);
        this.onTextChanged.accept(ui, old);
        return result;
    }

    @Override
    public void tick(long ticks) {
        // FIXME: needed?
//        this.textField.updateCursorCounter();
    }

    @Override
    public void click(int mouseX, int mouseY, MouseButton button, UI ui) {
        this.textField.mouseClicked(mouseX, mouseY, button.ordinal());
        if (textDisappearsOnNextClick && (mouseX >= this.textField.x && mouseX < this.textField.x + this.textField.getWidth() && mouseY >= this.textField.y && mouseY < this.textField.y + this.textField.getHeight()) && button == MouseButton.LEFT) {
            textField.setValue("");
            textDisappearsOnNextClick = false;
        }
    }

    public void setColor(int color) {
        textField.setTextColor(color);
    }

    public void setValue(String textIn) {
        textField.setValue(textIn);
    }

    public String getValue() {
        return textField.getValue();
    }

    public void writeText(String textToWrite) {
        textField.insertText(textToWrite);
    }
}
