/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.wynntils.McIf;
import org.lwjgl.glfw.GLFW;

import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.SpecialRendering;
import com.wynntils.core.framework.rendering.colors.MinecraftChatColors;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.framework.ui.elements.GuiTextFieldWynn;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.modules.core.overlays.inventories.ChestReplacer;
import com.wynntils.modules.utilities.UtilitiesModule;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;

import net.minecraft.client.gui.screen.Screen;
import com.wynntils.transition.GlStateManager;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BankOverlay implements Listener {

    private static final Pattern PAGE_PATTERN = Pattern.compile("\\[Pg\\. ([0-9]*)\\] [a-z_A-Z0-9 ]+'s? Bank");

    private static final ResourceLocation COLUMN_ARROW = new ResourceLocation("minecraft:textures/wynn/gui/column_arrow_right.png");

    private static final int PAGE_FORWARD = 8;
    private static final int PAGE_BACK = 17;
    private static final int[] QA_SLOTS = {7, 16, 25, 34, 43, 52};
    private static final int[] QA_DEFAULTS = {1, 5, 9, 13, 17, 21};
    private static final int QA_BUTTONS = 6;

    private boolean inBank = false;
    private boolean itemsLoaded = false;
    private int page = 0;
    private int destinationPage = 0;
    private int searching = 0;

    private boolean textureLoaded = false;

    private boolean editButtonHover = false;
    private GuiTextFieldWynn nameField = null;
    private GuiTextFieldWynn searchField = null;
    private final ScreenRenderer renderer = new ScreenRenderer();

    public static List<ItemStack> searchedItems = new ArrayList<>();

    @SubscribeEvent
    public void onBankClose(GuiOverlapEvent.ChestOverlap.GuiClosed e) {
        // reset everything
        page = 0;
        inBank = false;
        itemsLoaded = false;
        nameField = null;
        searchedItems.clear();
        Keyboard.enableRepeatEvents(false);
    }

    @SubscribeEvent
    public void onBankInit(GuiOverlapEvent.ChestOverlap.InitGui e) {
        Matcher m = PAGE_PATTERN.matcher(McIf.getTextWithoutFormattingCodes(e.getGui().getLowerInv().getName()));
        if (!m.matches()) return;

        inBank = true;
        page = Integer.parseInt(m.group(1));
        updateMaxPages();

        if (UtilitiesConfig.Bank.INSTANCE.pageNames.containsKey(page))
            updateName(e.getGui().getLowerInv());

        if (destinationPage == page) destinationPage = 0; // if we've already arrived, reset destination

        if (searchField == null && UtilitiesConfig.Bank.INSTANCE.showBankSearchBar) {
            int nameWidth = McIf.mc().font.width(McIf.getUnformattedText(e.getGui().getUpperInv().getDisplayName()));
            searchField = new GuiTextFieldWynn(201, McIf.mc().font, nameWidth + 13, 128, 157 - nameWidth, 10);
            searchField.setValue("Search...");
        }

        textureLoaded = isTextureLoaded(COLUMN_ARROW);

        Keyboard.enableRepeatEvents(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBankDrawBackground(GuiOverlapEvent.ChestOverlap.DrawGuiContainerBackgroundLayer e) {
        if (!inBank) return;

        // searched item highlight
        for (Slot s : e.getGui().getMenu().slots) {
            if (s.getItem().isEmpty() || !s.getItem().hasCustomHoverName()) continue;
            if (!searchedItems.contains(s.getItem())) continue;

            SpecialRendering.renderGodRays(e.getGui().getGuiLeft() + s.xPos + 5,
                    e.getGui().getGuiTop() + s.yPos + 6, 0, 5f, 35, UtilitiesConfig.Bank.INSTANCE.searchHighlightColor);
        }

        if (!textureLoaded) return;
        if (!UtilitiesConfig.Bank.INSTANCE.showQuickAccessIcons) return;

        // quick access icons
        for (int i = 0; i < QA_BUTTONS; i++) {
            Slot s = e.getGui().getMenu().getSlot(QA_SLOTS[i]);

            s.set(new ItemStack(Blocks.SNOW));
            McIf.mc().getTextureManager().bind(COLUMN_ARROW);

            GlStateManager.pushMatrix();
            {
                { // gl setting
                    GlStateManager.scale(1.1f, 1.1f, 1.1f);
                    GlStateManager.color(1f, 1f, 1f);
                    GlStateManager.disableLighting();
                }

                Screen.drawModalRectWithCustomSizedTexture((int) ((e.getGui().getGuiLeft() + s.xPos - 8) / 1.1f) - 1, (int) ((e.getGui().getGuiTop() + s.yPos - 8) / 1.1f) - 1, 0, 0, 32, 32, 32, 32);
            }
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBankDrawForeground(GuiOverlapEvent.ChestOverlap.DrawGuiContainerForegroundLayer e) {
        if (!inBank) return;

        searchPageForItems(e.getGui());
        checkItemsLoaded(e.getGui());

        int x = e.getGui().getXSize() - 19; int y = 2;

        ScreenRenderer.beginGL(0, 0);
        {
            { // quick access numbers
                int[] destinations = getQuickAccessDestinations();
                for (int i = 0; i < QA_BUTTONS; i++) {
                    Slot s = e.getGui().getMenu().getSlot(QA_SLOTS[i]);
                    int destination = destinations[i];

                    if (UtilitiesConfig.Bank.INSTANCE.showQuickAccessNumbers) {
                        GlStateManager.translate(0, 0, 300F);
                        renderer.drawString(String.valueOf(destination), s.xPos + 8, s.yPos + 4, MinecraftChatColors.WHITE,
                                SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NORMAL);
                        GlStateManager.translate(0, 0, -300F);
                    }

                    ItemStack is = s.getItem();
                    is.setStackDisplayName(TextFormatting.GRAY + "Jump to Page " + destination);

                    if (!UtilitiesConfig.Bank.INSTANCE.pageNames.containsKey(destination)) continue;
                    ItemUtils.replaceLore(is, Arrays.asList(TextFormatting.GRAY + " - " + UtilitiesConfig.Bank.INSTANCE.pageNames.get(destination)));
                }
            }

            { // textboxes
                GlStateManager.translate(0, 0, 300F);
                if (nameField != null) nameField.drawTextBox();
                if (searchField != null) searchField.drawTextBox();
                GlStateManager.translate(0, 0, -300F);
            }

            { // draw page name edit button
                renderer.color(1f, 1f, 1f, 1f);
                renderer.drawRect(Textures.UIs.character_selection, x, y, x + 6, y + 12, 182, 102, 190, 118);
            }
        }
        ScreenRenderer.endGL();

        // mouse over
        // is mouse over edit button
        if (e.getMouseX() >= e.getGui().getGuiLeft() + x - 4 && e.getMouseX() <= e.getGui().getGuiLeft() + x + 6 + 4 &&
                e.getMouseY() >= e.getGui().getGuiTop() + y && e.getMouseY() <= e.getGui().getGuiTop() + y + 12) {
            editButtonHover = true;
            e.getGui().drawHoveringText(Arrays.asList(
                    nameField == null ? TextFormatting.GOLD + "[>] Change current page name" : TextFormatting.RED + "[X] Cancel operation",
                    nameField == null ? TextFormatting.GRAY + "Right-click to reset to default" : TextFormatting.GRAY + "Write in the left text field."
                    ),
                    e.getMouseX() - e.getGui().getGuiLeft(), e.getMouseY() - e.getGui().getGuiTop());
            return;
        }

        editButtonHover = false;
    }

    @SubscribeEvent
    public void onSlotClicked(GuiOverlapEvent.ChestOverlap.HandleMouseClick e) {
        if (!inBank || e.getSlotIn() == null) return;
        Slot s = e.getSlotIn();

        // override default quick access if custom destination is defined
        int[] destinations = getQuickAccessDestinations();
        for (int i = 0; i < QA_BUTTONS; i++) {
            if (s.slotNumber != QA_SLOTS[i]) continue;
            if (destinations[i] == QA_DEFAULTS[i]) break; // same page

            e.setCanceled(true);
            destinationPage = destinations[i];
            gotoPage(e.getGui());
            break;
        }

        // auto page searching
        if (!isSearching() || !UtilitiesConfig.Bank.INSTANCE.autoPageSearch
                || !(s.slotNumber == PAGE_FORWARD || s.slotNumber == PAGE_BACK)) return;

        searching = (s.slotNumber == PAGE_FORWARD) ? 1 : -1;
        destinationPage = page + searching;
        gotoPage(e.getGui());

        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onMouseClicked(GuiOverlapEvent.ChestOverlap.MouseClicked e) {
        if (!inBank) return;

        int offsetMouseX = e.getMouseX() - e.getGui().getGuiLeft();
        int offsetMouseY = e.getMouseY() - e.getGui().getGuiTop();

        // handle mouse input on name editor
        if (nameField != null) nameField.mouseClicked(offsetMouseX, offsetMouseY, e.getMouseButton());

        // handle mouse input on search box
        if (searchField != null) {
            searchField.mouseClicked(offsetMouseX, offsetMouseY, e.getMouseButton());
            if (e.getMouseButton() == 0) { // left click
                if (searchField.isFocused()) {
                    searchField.setCursorPositionEnd();
                    searchField.setSelectionPos(0);
                } else {
                    searchField.setSelectionPos(searchField.getCursorPosition());
                }
            }
        }

        // handle mouse input on edit button
        if (!editButtonHover) return;

        if (e.getMouseButton() == 0) {
            if (nameField != null) { // hide if clicking again
                nameField = null;
                updateName(e.getGui().getLowerInv());
                return;
            }

            ((Inventory) e.getGui().getLowerInv()).setCustomName("");
            nameField = new GuiTextFieldWynn(200, McIf.mc().font, 8, 5, 120, 10);
            nameField.setFocused(true);

            if (UtilitiesConfig.Bank.INSTANCE.pageNames.containsKey(page))
                nameField.setValue(UtilitiesConfig.Bank.INSTANCE.pageNames.get(page).replace("§", "&"));

            return;
        }

        if (e.getMouseButton() != 1) return;
        if (UtilitiesConfig.Bank.INSTANCE.pageNames.remove(page) != null) updateName(e.getGui().getLowerInv());

        UtilitiesConfig.Bank.INSTANCE.saveSettings(UtilitiesModule.getModule());
    }

    @SubscribeEvent
    public void onKeyTyped(GuiOverlapEvent.ChestOverlap.KeyTyped e) {
        if (!inBank) return;

        // handle typing in text boxes
        if (nameField != null && nameField.isFocused()) {
            e.setCanceled(true);
            if (e.getKeyCode() == GLFW.GLFW_KEY_ENTER) {
                String name = nameField.getValue();
                nameField = null;

                name = name.replaceAll("&([a-f0-9k-or])", "§$1");
                UtilitiesConfig.Bank.INSTANCE.pageNames.put(page, name);
                UtilitiesConfig.Bank.INSTANCE.saveSettings(UtilitiesModule.getModule());
                updateName(e.getGui().getLowerInv());
            } else if (e.getKeyCode() == GLFW.GLFW_KEY_ESCAPE) {
                nameField = null;
                updateName(e.getGui().getLowerInv());
            } else {
                nameField.keyPressed(e.getTypedChar(), e.getKeyCode(), 0);
            }
        } else if (searchField != null && searchField.isFocused()) {
            e.setCanceled(true);
            if (e.getKeyCode() == GLFW.GLFW_KEY_ESCAPE) {
                searchField.setFocused(false);
            } else if (e.getKeyCode() == GLFW.GLFW_KEY_ENTER && isSearching()) {
                searching = 1;
                destinationPage = page + 1;
                gotoPage(e.getGui());
            } else {
                searchField.keyPressed(e.getTypedChar(), e.getKeyCode(), 0);
            }
        } else if (e.getKeyCode() == GLFW.GLFW_KEY_ESCAPE || e.getKeyCode() == McIf.mc().options.keyBindInventory.getKey().getValue()) { // bank was closed by player
            destinationPage = 0;
            searchField = null;
            searching = 0;
        }
    }

    private void checkItemsLoaded(ChestReplacer bankGui) {
        if (itemsLoaded) return;

        // if one of these is in inventory, items have loaded in
        if(!bankGui.getMenu().getSlot(PAGE_FORWARD).getItem().isEmpty() || !bankGui.getMenu().getSlot(PAGE_BACK).getItem().isEmpty()) {
            itemsLoaded = true;
            searchBank(bankGui);
            if (destinationPage != 0 && destinationPage != page)
                gotoPage(bankGui);

        }
    }

    private void updateName(IInventory bankGui) {
        String name = (UtilitiesConfig.Bank.INSTANCE.pageNames.containsKey(page))
                ? UtilitiesConfig.Bank.INSTANCE.pageNames.get(page) : TextFormatting.DARK_GRAY
                        + McIf.player().getName() + "'s" + TextFormatting.BLACK + " Bank";

        ((Inventory) bankGui).setCustomName(TextFormatting.BLACK + "[Pg. " + page + "] " + name);
    }

    private void gotoPage(ChestReplacer bankGui) {
        // check if we've already arrived somehow
        if (destinationPage == page) {
            destinationPage = 0;
            return;
        }

        int hop = (destinationPage / 4) * 4 + 1;

        // don't assume we can hop to a page that's greater than the destination
        if (hop > UtilitiesConfig.Bank.INSTANCE.maxPages && hop > destinationPage) hop -=4;

        CClickWindowPacket packet = null;
        if (Math.abs(destinationPage - hop) >= Math.abs(destinationPage - page)) { // we already hopped, or started from a better/equivalent spot
            if (page < destinationPage) { // destination is in front of us
                ItemStack is = bankGui.getMenu().getSlot(PAGE_FORWARD).getItem();

                // ensure arrow is there
                if (!is.hasCustomHoverName() || !is.getDisplayName().contains(">" + TextFormatting.DARK_GREEN + ">" + TextFormatting.GREEN + ">" + TextFormatting.DARK_GREEN + ">" + TextFormatting.GREEN + ">")) {
                    destinationPage = 0;
                    searching = 0;
                    return;
                }
                packet = new CClickWindowPacket(bankGui.getMenu().windowId, PAGE_FORWARD, 0, ClickType.PICKUP, is,
                                bankGui.getMenu().getNextTransactionID(McIf.player().inventory));
            } else {
                ItemStack is = bankGui.getMenu().getSlot(PAGE_BACK).getItem();

                // ensure arrow is there
                if (!is.hasCustomHoverName() || !is.getDisplayName().contains("<" + TextFormatting.DARK_GREEN + "<" + TextFormatting.GREEN + "<" + TextFormatting.DARK_GREEN + "<" + TextFormatting.GREEN + "<")) {
                    destinationPage = 0;
                    searching = 0;
                    return;
                }
                packet = new CClickWindowPacket(bankGui.getMenu().windowId, PAGE_BACK, 0, ClickType.PICKUP, is,
                                bankGui.getMenu().getNextTransactionID(McIf.player().inventory));
            }
        } else { // attempt to hop using default quick access buttons
            int slotId = QA_SLOTS[(hop / 4)];
            packet = new CClickWindowPacket(bankGui.getMenu().windowId, slotId, 0, ClickType.PICKUP, bankGui.getMenu().getSlot(slotId).getItem(),
                            bankGui.getMenu().getNextTransactionID(McIf.player().inventory));
        }

        McIf.mc().getConnection().send(packet);
    }

    private void searchPageForItems(ChestReplacer bankGui) {
        searchedItems.clear();
        if (!isSearching()) return;

        String searchText = searchField.getValue().toLowerCase();
        for (int i = 0; i < bankGui.getLowerInv().getContainerSize(); i++) {
            if (i % 9 > 6) continue; // ignore sidebar items

            ItemStack is = bankGui.getLowerInv().getItem(i);
            if (McIf.getTextWithoutFormattingCodes(is.getDisplayName()).toLowerCase().contains(searchText)) searchedItems.add(is);
        }
    }

    private void searchBank(ChestReplacer bankGui) {
        if (searching == 0) return;
        if (searchedItems.isEmpty()) { // continue searching
            destinationPage = page + searching;
            gotoPage(bankGui);
            return;
        }

        searching = 0; // item found, search is done
    }

    private boolean isSearching() {
        return (searchField != null && !searchField.getValue().equals("Search...") && !searchField.getValue().isEmpty());
    }

    private boolean isTextureLoaded(ResourceLocation resourceLocation) {
        ITextureObject texture = McIf.mc().getTextureManager().getTexture(resourceLocation);
        if (texture == null) {
            return McIf.mc().getTextureManager().loadTexture(resourceLocation, new SimpleTexture(resourceLocation));
        }
        return (!texture.equals(TextureUtil.MISSING_TEXTURE));
    }

    private void updateMaxPages() {
        if (UtilitiesConfig.Bank.INSTANCE.maxPages >= page) return;

        UtilitiesConfig.Bank.INSTANCE.maxPages = page;
        UtilitiesConfig.Bank.INSTANCE.saveSettings(UtilitiesModule.getModule());
    }

    private static int[] getQuickAccessDestinations() {
        return new int[] {
                UtilitiesConfig.Bank.INSTANCE.quickAccessOne,
                UtilitiesConfig.Bank.INSTANCE.quickAccessTwo,
                UtilitiesConfig.Bank.INSTANCE.quickAccessThree,
                UtilitiesConfig.Bank.INSTANCE.quickAccessFour,
                UtilitiesConfig.Bank.INSTANCE.quickAccessFive,
                UtilitiesConfig.Bank.INSTANCE.quickAccessSix,
        };
    }

}
