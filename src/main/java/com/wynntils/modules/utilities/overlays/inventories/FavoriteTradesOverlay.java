/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.reference.EmeraldSymbols;
import com.wynntils.modules.utilities.managers.KeyManager;
import net.minecraft.client.Minecraft;
import com.wynntils.transition.GlStateManager;
import com.wynntils.transition.RenderHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FavoriteTradesOverlay implements Listener {

    private final List<String> favorites_trade_items_lore = new ArrayList<>();

    @SubscribeEvent
    public void onKeyPress(GuiOverlapEvent.ChestOverlap.KeyTyped e) {
        if (!Reference.onWorld || !McIf.getFormattedText(e.getGui().getLowerInv().getDisplayName()).contains("Marketplace")) return;
        if (e.getKeyCode() != KeyManager.getFavoriteTradeKey().getKeyBinding().getKey().getValue()) return;

        if (e.getGui().getSlotUnderMouse() != null && McIf.player().inventory != e.getGui().getSlotUnderMouse().inventory) {
            toggleLockState(e.getGui().getSlotUnderMouse().getItem());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChestGui(GuiOverlapEvent.ChestOverlap.HoveredToolTip.Pre e) {
        if (!Reference.onWorld) return;
        if (!McIf.getFormattedText(e.getGui().getLowerInv().getDisplayName()).contains("Marketplace")) {
            if (!favorites_trade_items_lore.isEmpty())
                favorites_trade_items_lore.clear();
            return;
        }

        for (Slot s : e.getGui().getMenu().slots) {
            if (s.slotNumber >= e.getGui().getLowerInv().getContainerSize()) continue;
            if (isNotMarketItem(s.getItem())) continue;
            renderFavoriteItem(s, e.getGui().getGuiLeft(), e.getGui().getGuiTop());
        }
    }

    private boolean isNotMarketItem(ItemStack it) {
        List<String> lore = ItemUtils.getLore(it);
        if (lore.size() < 3) return true;
        char[] c = lore.get(2).toCharArray();
        for (char cc : c)
            if (cc == EmeraldSymbols.E)
                return false;
        return true;
    }

    private void renderFavoriteItem(Slot s, int guiLeft, int guiTop) {
        ItemStack it = s.getItem();
        ItemIdentificationOverlay.replaceLore(it);
        String lore = Arrays.toString(ItemUtils.getLore(it).toArray());
        if (!favorites_trade_items_lore.contains(lore)) return;

        ScreenRenderer.beginGL(0, 0);

        // HeyZeer0: this will make the lock appear over the item
        GlStateManager.translate(0, 0, 260);

        ScreenRenderer r = new ScreenRenderer();
        RenderHelper.disableStandardItemLighting();
        ScreenRenderer.scale(0.5f);
        r.drawRect(Textures.UIs.hud_overlays, (int)((guiLeft + s.xPos) / 0.5) + 20, (int)((guiTop + s.yPos) / 0.5) - 3, 51, 0, 17, 16);
        ScreenRenderer.endGL();
    }

    private void toggleLockState(ItemStack it) {
        if (!Reference.onWorld || isNotMarketItem(it)) return;

        ItemIdentificationOverlay.replaceLore(it);
        String lore = Arrays.toString(ItemUtils.getLore(it).toArray());

        if (!favorites_trade_items_lore.remove(lore)) {
            favorites_trade_items_lore.add(lore);
        }
    }

}
