/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances.inventory;

import com.wynntils.ModCore;
import com.wynntils.modules.core.interfaces.IInventoryOpenAction;
import com.wynntils.modules.core.managers.PacketQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.util.EnumHand;

public class InventoryOpenByItem implements IInventoryOpenAction {

    private static final CPacketPlayerTryUseItem rightClick = new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND);
    public static final IPacket<?> ignoredPacket = rightClick;

    int inputSlot;

    public InventoryOpenByItem(int inputSlot) {
        this.inputSlot = inputSlot;
    }

    @Override
    public void onOpen(FakeInventory inv, Runnable onDrop) {
        Minecraft mc = ModCore.mc();

        PacketQueue.queueComplexPacket(rightClick, SPacketOpenWindow.class).setSender((conn, pack) -> {
            if (mc.player.inventory.selected != inputSlot) {
                conn.sendPacket(new CPacketHeldItemChange(inputSlot));
            }

            conn.sendPacket(pack);
            if (mc.player.inventory.selected != inputSlot) {
                conn.sendPacket(new CPacketHeldItemChange(mc.player.inventory.selected));
            }
        }).onDrop(onDrop);
    }

}
