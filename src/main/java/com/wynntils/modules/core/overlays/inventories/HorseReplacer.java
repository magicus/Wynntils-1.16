/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.inventory.HorseInventoryScreen;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.List;

public class HorseReplacer extends HorseInventoryScreen  {

    IInventory lowerInv, upperInv;

    public HorseReplacer(IInventory playerInv, IInventory horseInv, AbstractHorseEntity horse) {
        super(playerInv, horseInv, horse);

        this.lowerInv = playerInv; this.upperInv = horseInv;
    }

    public IInventory getUpperInv() {
        return upperInv;
    }

    public IInventory getLowerInv() {
        return lowerInv;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawScreen(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HandleMouseClick(this, slotIn, slotId, mouseButton, type)))
            super.slotClicked(slotIn, slotId, mouseButton, type);
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawGuiContainerForegroundLayer(this, mouseX, mouseY));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawGuiContainerBackgroundLayer(this, mouseX, mouseY));
    }

    @Override
    public void keyPressed(char typedChar, int keyCode) throws IOException {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.KeyTyped(this, typedChar, keyCode)))
            super.keyPressed(typedChar, keyCode);
    }

    @Override
    public void renderHoveredToolTip(int x, int y) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HoveredToolTip.Pre(this, x, y))) return;

        super.renderHoveredToolTip(x, y);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HoveredToolTip.Post(this, x, y));
    }

    @Override
    public void renderToolTip(ItemStack stack, int x, int y) {
        super.renderToolTip(stack, x, y);
    }

    @Override
    public void onClose() {
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.GuiClosed(this));
        super.onClose();
    }

    public List<Button> getButtonList() {
        return buttons;
    }

}
