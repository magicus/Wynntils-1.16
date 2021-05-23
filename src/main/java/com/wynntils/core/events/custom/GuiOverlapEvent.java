/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.events.custom;

import com.wynntils.core.utils.reflections.ReflectionFields;
import com.wynntils.modules.core.overlays.inventories.ChestReplacer;
import com.wynntils.modules.core.overlays.inventories.HorseReplacer;
import com.wynntils.modules.core.overlays.inventories.IngameMenuReplacer;
import com.wynntils.modules.core.overlays.inventories.InventoryReplacer;
import com.wynntils.modules.core.overlays.ui.PlayerInfoReplacer;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class GuiOverlapEvent<T extends AbstractGui> extends Event {

    protected T gui;

    protected GuiOverlapEvent(T gui) {
        this.gui = gui;
    }

    public T getGui() {
        return gui;
    }

    public static class GuiScreenOverlapEvent<T extends Screen> extends GuiOverlapEvent<T> {

        protected GuiScreenOverlapEvent(T guiScreen) {
            super(guiScreen);
        }

        public List<Button> getButtonList() {
            return ReflectionFields.GuiScreen_buttonList.getValue(getGui());
        }

    }

    public static class InventoryOverlap extends GuiScreenOverlapEvent<InventoryReplacer> {

        public InventoryOverlap(InventoryReplacer guiInventory) {
            super(guiInventory);
        }

        @Override
        public List<Button> getButtonList() {
            return getGui().getButtonList();
        }

        public static class DrawScreen extends InventoryOverlap {

            int mouseX, mouseY; float partialTicks;

            public DrawScreen(InventoryReplacer guiInventory, int mouseX, int mouseY, float partialTicks) {
                super(guiInventory);

                this.mouseX = mouseX; this.mouseY = mouseY; this.partialTicks = partialTicks;
            }

            public float getPartialTicks() {
                return partialTicks;
            }

            public int getMouseX() {
                return mouseX;
            }

            public int getMouseY() {
                return mouseY;
            }

        }

        public static class HandleMouseClick extends InventoryOverlap {

            Slot slotIn; int slotId, mouseButton; ClickType type;

            public HandleMouseClick(InventoryReplacer guiInventory, Slot slotIn, int slotId, int mouseButton, ClickType type)  {
                super(guiInventory);

                this.slotIn = slotIn; this.slotId = slotId; this.mouseButton = mouseButton; this.type = type;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

            public ClickType getType() {
                return type;
            }

            public int getMouseButton() {
                return mouseButton;
            }

            public int getSlotId() {
                return slotId;
            }

            public Slot getSlotIn() {
                return slotIn;
            }
        }

        public static class DrawGuiContainerForegroundLayer extends InventoryOverlap {

            int mouseX, mouseY;

            public DrawGuiContainerForegroundLayer(InventoryReplacer guiInventory, int mouseX, int mouseY) {
                super(guiInventory);

                this.mouseX = mouseX; this.mouseY = mouseY;
            }

            public int getMouseY() {
                return mouseY;
            }

            public int getMouseX() {
                return mouseX;
            }
        }

        public static class DrawGuiContainerBackgroundLayer extends InventoryOverlap {

            int mouseX, mouseY;

            public DrawGuiContainerBackgroundLayer(InventoryReplacer guiInventory, int mouseX, int mouseY) {
                super(guiInventory);

                this.mouseX = mouseX; this.mouseY = mouseY;
            }

            public int getMouseY() {
                return mouseY;
            }

            public int getMouseX() {
                return mouseX;
            }
        }

        public static class KeyTyped extends InventoryOverlap {

            char typedChar; int keyCode;

            public KeyTyped(InventoryReplacer guiInventory, char typedChar, int keyCode) {
                super(guiInventory);

                this.typedChar = typedChar;
                this.keyCode = keyCode;
            }

            public char getTypedChar() {
                return typedChar;
            }

            public int getKeyCode() {
                return keyCode;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

        }

        public static class HoveredToolTip extends InventoryOverlap {

            int x, y;

            public HoveredToolTip(InventoryReplacer guiInventory, int x, int y) {
                super(guiInventory);

                this.x = x;
                this.y = y;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public static class Pre extends HoveredToolTip {

                public Pre(InventoryReplacer guiInventory, int x, int y) {
                    super(guiInventory, x, y);
                }
            }

            public static class Post extends HoveredToolTip {

                public Post(InventoryReplacer guiInventory, int x, int y) {
                    super(guiInventory, x, y);
                }
            }

        }

        public static class GuiClosed extends InventoryOverlap {

            public GuiClosed(InventoryReplacer guiInventory) {
                super(guiInventory);
            }
        }

    }

    public static class ChestOverlap extends GuiScreenOverlapEvent<ChestReplacer> {

        public ChestOverlap(ChestReplacer guiInventory) {
            super(guiInventory);
        }

        @Override
        public List<Button> getButtonList() {
            return getGui().getButtonList();
        }

        public static class DrawScreen extends ChestOverlap {

            int mouseX, mouseY; float partialTicks;

            public DrawScreen(ChestReplacer guiChest, int mouseX, int mouseY, float partialTicks) {
                super(guiChest);

                this.mouseX = mouseX; this.mouseY = mouseY; this.partialTicks = partialTicks;
            }

            public float getPartialTicks() {
                return partialTicks;
            }

            public int getMouseX() {
                return mouseX;
            }

            public int getMouseY() {
                return mouseY;
            }

            public static class Pre extends DrawScreen {

                public Pre(ChestReplacer guiChest, int mouseX, int mouseY, float partialTicks) {
                    super(guiChest, mouseX, mouseY, partialTicks);
                }

                @Override
                public boolean isCancelable() {
                    return true;
                }

            }

            public static class Post extends DrawScreen {

                public Post(ChestReplacer guiChest, int mouseX, int mouseY, float partialTicks) {
                    super(guiChest, mouseX, mouseY, partialTicks);
                }

            }

        }

        public static class HandleMouseClick extends ChestOverlap {

            Slot slotIn; int slotId, mouseButton; ClickType type;

            public HandleMouseClick(ChestReplacer guiChest, Slot slotIn, int slotId, int mouseButton, ClickType type)  {
                super(guiChest);

                this.slotIn = slotIn; this.slotId = slotId; this.mouseButton = mouseButton; this.type = type;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

            public ClickType getType() {
                return type;
            }

            public int getMouseButton() {
                return mouseButton;
            }

            public int getSlotId() {
                return slotId;
            }

            public Slot getSlotIn() {
                return slotIn;
            }
        }

        public static class MouseClickMove extends ChestOverlap {

            int mouseX;
            int mouseY;
            int clickedMouseButton;
            long timeSinceLastClick;

            public MouseClickMove(ChestReplacer guiChest, int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick)  {
                super(guiChest);

                this.gui = guiChest;
                this.mouseX = mouseX;
                this.mouseY = mouseY;
                this.clickedMouseButton = clickedMouseButton;
                this.timeSinceLastClick = timeSinceLastClick;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

            public int getClickedMouseButton() {
                return clickedMouseButton;
            }

            public int getMouseX() {
                return mouseX;
            }

            public int getMouseY() {
                return mouseY;
            }

            public long getTimeSinceLastClick() {
                return timeSinceLastClick;
            }

        }

        public static class HandleMouseInput extends ChestOverlap {

            public HandleMouseInput(ChestReplacer guiChest)  {
                super(guiChest);
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

        }

        public static class DrawGuiContainerForegroundLayer extends ChestOverlap {

            int mouseX, mouseY;

            public DrawGuiContainerForegroundLayer(ChestReplacer guiChest, int mouseX, int mouseY) {
                super(guiChest);

                this.mouseX = mouseX; this.mouseY = mouseY;
            }

            public int getMouseY() {
                return mouseY;
            }

            public int getMouseX() {
                return mouseX;
            }
        }

        public static class DrawGuiContainerBackgroundLayer extends ChestOverlap {

            int mouseX, mouseY;

            public DrawGuiContainerBackgroundLayer(ChestReplacer guiChest, int mouseX, int mouseY) {
                super(guiChest);

                this.mouseX = mouseX; this.mouseY = mouseY;
            }

            public int getMouseY() {
                return mouseY;
            }

            public int getMouseX() {
                return mouseX;
            }
        }

        public static class KeyTyped extends ChestOverlap {

            char typedChar; int keyCode;

            public KeyTyped(ChestReplacer guiChest, char typedChar, int keyCode) {
                super(guiChest);

                this.typedChar = typedChar;
                this.keyCode = keyCode;
            }

            public char getTypedChar() {
                return typedChar;
            }

            public int getKeyCode() {
                return keyCode;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

        }

        public static class MouseClicked extends ChestOverlap {

            int mouseX, mouseY, mouseButton;

            public MouseClicked(ChestReplacer guiChest, int mouseX, int mouseY, int mouseButton) {
                super(guiChest);

                this.mouseX = mouseX; this.mouseY = mouseY; this.mouseButton = mouseButton;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

            public int getMouseY() {
                return mouseY;
            }

            public int getMouseX() {
                return mouseX;
            }

            public int getMouseButton() {
                return mouseButton;
            }

        }

        public static class InitGui extends ChestOverlap {

            List<Button> buttons;

            public InitGui(ChestReplacer guiChest, List<Button> buttons) {
                super(guiChest);
                this.buttons = buttons;
            }

            @Override
            public List<Button> getButtonList() {
                return buttons;
            }

        }

        public static class HoveredToolTip extends ChestOverlap {

            int x, y;

            public HoveredToolTip(ChestReplacer guiInventory, int x, int y) {
                super(guiInventory);

                this.x = x;
                this.y = y;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            @Cancelable
            public static class Pre extends HoveredToolTip {

                public Pre(ChestReplacer guiInventory, int x, int y) {
                    super(guiInventory, x, y);
                }
            }

            public static class Post extends HoveredToolTip {

                public Post(ChestReplacer guiInventory, int x, int y) {
                    super(guiInventory, x, y);
                }
            }

        }

        public static class GuiClosed extends ChestOverlap {

            public GuiClosed(ChestReplacer guiInventory) {
                super(guiInventory);
            }
        }

    }

    public static class HorseOverlap extends GuiScreenOverlapEvent<HorseReplacer> {

        public HorseOverlap(HorseReplacer guiHorse) {
            super(guiHorse);
        }

        @Override
        public List<Button> getButtonList() {
            return getGui().getButtonList();
        }

        public static class DrawScreen extends HorseOverlap {

            int mouseX, mouseY; float partialTicks;

            public DrawScreen(HorseReplacer guiHorse, int mouseX, int mouseY, float partialTicks) {
                super(guiHorse);

                this.mouseX = mouseX; this.mouseY = mouseY; this.partialTicks = partialTicks;
            }

            public float getPartialTicks() {
                return partialTicks;
            }

            public int getMouseX() {
                return mouseX;
            }

            public int getMouseY() {
                return mouseY;
            }

        }

        public static class HandleMouseClick extends HorseOverlap {

            Slot slotIn; int slotId, mouseButton; ClickType type;

            public HandleMouseClick(HorseReplacer guiHorse, Slot slotIn, int slotId, int mouseButton, ClickType type)  {
                super(guiHorse);

                this.slotIn = slotIn; this.slotId = slotId; this.mouseButton = mouseButton; this.type = type;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

            public ClickType getType() {
                return type;
            }

            public int getMouseButton() {
                return mouseButton;
            }

            public int getSlotId() {
                return slotId;
            }

            public Slot getSlotIn() {
                return slotIn;
            }
        }

        public static class DrawGuiContainerForegroundLayer extends HorseOverlap {

            int mouseX, mouseY;

            public DrawGuiContainerForegroundLayer(HorseReplacer guiHorse, int mouseX, int mouseY) {
                super(guiHorse);

                this.mouseX = mouseX; this.mouseY = mouseY;
            }

            public int getMouseY() {
                return mouseY;
            }

            public int getMouseX() {
                return mouseX;
            }
        }

        public static class DrawGuiContainerBackgroundLayer extends HorseOverlap {

            int mouseX, mouseY;

            public DrawGuiContainerBackgroundLayer(HorseReplacer guiHorse, int mouseX, int mouseY) {
                super(guiHorse);

                this.mouseX = mouseX; this.mouseY = mouseY;
            }

            public int getMouseY() {
                return mouseY;
            }

            public int getMouseX() {
                return mouseX;
            }
        }

        public static class KeyTyped extends HorseOverlap {

            char typedChar; int keyCode;

            public KeyTyped(HorseReplacer guiHorse, char typedChar, int keyCode) {
                super(guiHorse);

                this.typedChar = typedChar;
                this.keyCode = keyCode;
            }

            public char getTypedChar() {
                return typedChar;
            }

            public int getKeyCode() {
                return keyCode;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }
        }

        public static class HoveredToolTip extends HorseOverlap {

            int x, y;

            public HoveredToolTip(HorseReplacer guiInventory, int x, int y) {
                super(guiInventory);

                this.x = x;
                this.y = y;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public static class Pre extends HoveredToolTip {

                public Pre(HorseReplacer guiInventory, int x, int y) {
                    super(guiInventory, x, y);
                }

                @Override
                public boolean isCancelable() {
                    return true;
                }

            }

            public static class Post extends HoveredToolTip {

                public Post(HorseReplacer guiInventory, int x, int y) {
                    super(guiInventory, x, y);
                }
            }

        }

        public static class GuiClosed extends HorseOverlap {

            public GuiClosed(HorseReplacer guiHorse) {
                super(guiHorse);
            }
        }

    }

    public static class IngameMenuOverlap extends GuiScreenOverlapEvent<IngameMenuReplacer> {

        public IngameMenuOverlap(IngameMenuReplacer ingameMenuReplacer) {
            super(ingameMenuReplacer);
        }

        @Override
        public List<Button> getButtonList() {
            return getGui().getButtonList();
        }

        public static class DrawScreen extends IngameMenuOverlap {

            int mouseX, mouseY; float partialTicks;

            public DrawScreen(IngameMenuReplacer ingameMenuReplacer, int mouseX, int mouseY, float partialTicks) {
                super(ingameMenuReplacer);

                this.mouseX = mouseX; this.mouseY = mouseY; this.partialTicks = partialTicks;
            }

            public int getMouseX() {
                return mouseX;
            }

            public int getMouseY() {
                return mouseY;
            }

            public float getPartialTicks() {
                return partialTicks;
            }
        }

        public static class MouseClicked extends IngameMenuOverlap {

            int mouseX, mouseY, mouseButton;

            public MouseClicked(IngameMenuReplacer ingameMenuReplacer, int mouseX, int mouseY, int mouseButton) {
                super(ingameMenuReplacer);

                this.mouseX = mouseX; this.mouseY = mouseY; this.mouseButton = mouseButton;
            }

            public int getMouseY() {
                return mouseY;
            }

            public int getMouseX() {
                return mouseX;
            }

            public int getMouseButton() {
                return mouseButton;
            }

        }

        public static class InitGui extends IngameMenuOverlap {

            List<Button> buttons;

            public InitGui(IngameMenuReplacer ingameMenuReplacer, List<Button> buttons) {
                super(ingameMenuReplacer);

                this.buttons = buttons;
            }

            @Override
            public List<Button> getButtonList() {
                return buttons;
            }

        }

        public static class ActionPerformed extends IngameMenuOverlap {

            Button button;

            public ActionPerformed(IngameMenuReplacer ingameMenuReplacer, Button button) {
                super(ingameMenuReplacer);

                this.button = button;
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

            public Button getButton() {
                return button;
            }

        }

    }

    public static class PlayerInfoOverlap extends GuiOverlapEvent<PlayerInfoReplacer> {

        public PlayerInfoOverlap(PlayerInfoReplacer replacer) {
            super(replacer);
        }

        public static class RenderList extends PlayerInfoOverlap {

            public RenderList(PlayerInfoReplacer replacer) {
                super(replacer);
            }

            @Override
            public boolean isCancelable() {
                return true;
            }

        }

    }
}
