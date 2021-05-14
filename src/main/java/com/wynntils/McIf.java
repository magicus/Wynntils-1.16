/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;

/**
 * The Wynntils Minecraft Interface (MC IF).
 *
 * This class wraps all Minecraft functionality that we need but do not want to
 * depend on directly, for instance due to version disparity.
 */
public class McIf {
    public static String getUnformattedText(ITextComponent msg) {
        // FIXME: Need better implementation!
        return msg.toString();
    }

    public static String getFormattedText(ITextComponent msg) {
        // FIXME: Need better implementation!
        return msg.toString();
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

    public static ClientWorld world() {
        return mc().level;
    }

    public static ClientPlayerEntity player() {
        return mc().player;
    }

    /**
     * Return the system time in milliseconds
     * @return
     */
    public static long getSystemTime()
    {
        return Util.getMillis();
    }
}
