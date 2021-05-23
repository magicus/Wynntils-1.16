/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.visual.overlays;

import com.wynntils.McIf;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.rendering.instances.WindowedResolution;
import com.wynntils.modules.visual.configs.VisualConfig;
import com.wynntils.modules.visual.overlays.ui.CharacterSelectorUI;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class OverlayEvents implements Listener {

    private CharacterSelectorUI fakeCharacterSelector;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void initClassMenu(GuiOverlapEvent.ChestOverlap.InitGui e) {
        if (!VisualConfig.CharacterSelector.INSTANCE.enabled) return;
        if (!McIf.toText(e.getGui().getTitle()).contains("Select a Class")) return;

        WindowedResolution res = new WindowedResolution(480, 254);
        fakeCharacterSelector = new CharacterSelectorUI(null, e.getGui(), res.getScaleFactor());
        fakeCharacterSelector.setWorldAndResolution(McIf.mc(), e.getGui().width, e.getGui().height);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void closeCharacterMenu(GuiOverlapEvent.ChestOverlap.GuiClosed e) {
        if (!McIf.toText(e.getGui().getTitle()).contains("Select a Class")) return;

        fakeCharacterSelector = null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void replaceCharacterMenuDraw(GuiOverlapEvent.ChestOverlap.DrawScreen.Pre e) {
        if (fakeCharacterSelector == null) return;

        fakeCharacterSelector.render(e.getMouseX(), e.getMouseY(), e.getPartialTicks());
        e.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void replaceCharacterMenuClick(GuiOverlapEvent.ChestOverlap.MouseClicked e) {
        if (fakeCharacterSelector == null) return;

        fakeCharacterSelector.mouseClicked(e.getMouseX(), e.getMouseY(), e.getMouseButton());
        e.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void replaceMouseClickMove(GuiOverlapEvent.ChestOverlap.MouseClickMove e) {
        if (fakeCharacterSelector == null) return;

        fakeCharacterSelector.mouseDragged(e.getMouseX(), e.getMouseY(), e.getClickedMouseButton(), e.getTimeSinceLastClick(), 0);
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void replaceMouseInput(GuiOverlapEvent.ChestOverlap.HandleMouseInput e) {
        if (fakeCharacterSelector == null) return;

        fakeCharacterSelector.handleMouseInput();
    }

    @SubscribeEvent
    public void replaceKeyTyped(GuiOverlapEvent.ChestOverlap.KeyTyped e) {
        if (fakeCharacterSelector == null) return;

        fakeCharacterSelector.keyPressed(e.getTypedChar(), e.getKey().getValue());
        e.setCanceled(true);
    }

}
