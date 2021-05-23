/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.core.framework.enums.MouseButton;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.framework.ui.elements.UIEClickZone;
import com.wynntils.core.framework.ui.elements.UIEColorWheel;
import com.wynntils.core.framework.ui.elements.UIEList;
import com.wynntils.core.framework.ui.elements.UIETextBox;
import net.minecraft.client.gui.screen.Screen;
import com.wynntils.transition.GlStateManager;
import net.minecraft.util.text.StringTextComponent;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public abstract class UI extends Screen {
    private ScreenRenderer screenRenderer = new ScreenRenderer();
    protected long ticks = 0;
    protected int screenWidth = 0, screenHeight = 0, mouseX = 0, mouseY = 0;
    protected List<UIElement> UIElements = new ArrayList<>();

    private boolean initiated = false;

    protected UI() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        if (!initiated) { initiated = true; onInit(); }
        super.render(matrix, mouseX, mouseY, partialTicks);
        this.mouseX = mouseX;
        this.mouseY = mouseY;

        ScreenRenderer.beginGL(0, 0);

        screenWidth = ScreenRenderer.screen.getGuiScaledWidth();
        screenHeight = ScreenRenderer.screen.getGuiScaledHeight();

        onRenderPreUIE(screenRenderer);
        for (UIElement uie : UIElements) {
            uie.position.refresh(ScreenRenderer.screen);
            if (!uie.visible) continue;
            uie.render(mouseX, mouseY);
        }

        onRenderPostUIE(screenRenderer);

        ScreenRenderer.endGL();
    }

    @Override public void tick() {
        ticks++; onTick();
        for (UIElement uie : UIElements)
            uie.tick(ticks);
    }
    @Override public void init() { if (!initiated) { initiated = true; onInit(); } onWindowUpdate(); }
    @Override public void onClose() {
        onCloseWynntils();}

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);
        try {
            for (UIElement uie : UIElements)
                if (uie instanceof UIEList) {
                    List<UIElement> UIElements_old = this.UIElements;
                    this.UIElements = ((UIEList) uie).elements;
                    mouseClicked(mouseX, mouseY, mouseButton);
                    this.UIElements = UIElements_old;
                } else if (uie instanceof UIEClickZone)
                    ((UIEClickZone) uie).click((int) mouseX, (int) mouseY, mouseButton > 2 ? MouseButton.UNKNOWN : MouseButton.values()[mouseButton], this);
        } catch (ConcurrentModificationException ignored) {}
        // FIXME: We should probably include the result of our elements...
        return result;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        boolean result = super.mouseReleased(mouseX, mouseY, state);
        for (UIElement uie : UIElements) {
            if (uie instanceof UIEList) {
                List<UIElement> UIElements_old = this.UIElements;
                this.UIElements = ((UIEList) uie).elements;
                mouseReleased(mouseX, mouseY, state);
                this.UIElements = UIElements_old;
            } else if (uie instanceof UIEClickZone)
                ((UIEClickZone) uie).release((int) mouseX, (int) mouseY, state > 2 ? MouseButton.UNKNOWN : MouseButton.values()[state], this);
        }
        // FIXME: We should probably include the result of our elements...
        return result;
    }

    // FIXME: the change from mouseClickMove to mouseDragged is problematic
    // What is d1/d2? What happened to timeSinceLastClick?
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double d1, double d2) {
        // FIXME: should we not call super?
        for (UIElement uie : UIElements) {
            if (uie instanceof UIEList) {
                List<UIElement> UIElements_old = this.UIElements;
                this.UIElements = ((UIEList) uie).elements;
                mouseDragged(mouseX, mouseY, mouseButton, d1, d2);
                this.UIElements = UIElements_old;
            } else if (uie instanceof UIEClickZone)
                ((UIEClickZone) uie).clickMove((int) mouseX, (int) mouseY, mouseButton > 2 ? MouseButton.UNKNOWN : MouseButton.values()[mouseButton], (int) d1, this);
        }
        // FIXME: We should probably include the result of our elements...
        return true;
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j) {
        boolean result = super.keyPressed(typedChar, keyCode, j);
        for (UIElement uie : UIElements) {
            if (uie instanceof UIEList) {
                List<UIElement> UIElements_old = this.UIElements;
                this.UIElements = ((UIEList) uie).elements;
                keyPressed(typedChar, keyCode, j);
                this.UIElements = UIElements_old;
            } else if (uie instanceof UIETextBox) {
                ((UIETextBox) uie).keyPressed(typedChar, keyCode, j,this);
            } else if (uie instanceof UIEColorWheel)
                ((UIEColorWheel) uie).keyPressed(typedChar, keyCode, j, this);
        }
        // FIXME: We should probably include the result of our elements...
        return result;
    }

    // v  USE THESE INSTEAD OF GUISCREEN METHODS IF POSSIBLE  v \\
    public abstract void onInit();
    public abstract void onCloseWynntils();
    public abstract void onTick();
    public abstract void onRenderPreUIE(ScreenRenderer render);
    public abstract void onRenderPostUIE(ScreenRenderer render);
    public abstract void onWindowUpdate();

    @Override
    protected void fillGradient(MatrixStack matrices, int left, int top, int right, int bottom, int startColor, int endColor) {  // fix for alpha problems after doing default background
        super.fillGradient(matrices, left, top, right, bottom, startColor, endColor);
        GlStateManager.enableBlend();
    }

    public static void setupUI(UI ui) {
        for (Field f : ui.getClass().getFields()) {
            try {
                UIElement uie = (UIElement) f.get(ui);
                if (uie != null)
                    ui.UIElements.add(uie);
            } catch (Exception ignored) {}
        }
    }

    public void show() {
        setupUI(this);
        McIf.mc().setScreen(this);
    }

    public static abstract class CommonUIFeatures {
        static ScreenRenderer render = new ScreenRenderer();
        public static void drawBook() {
            int wh = ScreenRenderer.screen.getGuiScaledWidth()/2, hh = ScreenRenderer.screen.getGuiScaledHeight()/2;
            render.drawRect(Textures.UIs.book, wh - 200, hh - 110, wh + 200, hh + 110, 0, 0, 400, 220);
        }
        public static void drawScrollArea() {
            int wh = ScreenRenderer.screen.getGuiScaledWidth()/2, hh = ScreenRenderer.screen.getGuiScaledHeight()/2;
            render.drawRect(Textures.UIs.book_scrollarea_settings, wh - 190, hh - 100, wh - 12, hh + 85, 0, 0, 178, 185);
        }
    }
}
