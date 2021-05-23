/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

/**
 * The Wynntils Minecraft Interface (MC IF).
 *
 * This class wraps all Minecraft functionality that we need but do not want to
 * depend on directly, for instance due to version disparity.
 */
public class McIf {
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");

    public static String getUnformattedText(ITextComponent msg) {
        // FIXME: Need better implementation!
        return msg.toString();
    }

    public static String getFormattedText(ITextComponent msg) {
        // FIXME: Need better implementation!
        return msg.toString();
    }

    /**
     * This is a wrapper for methods that returned a String in 1.12.
     * FIXME: will need individual consideration to figure out proper replacement.
     * @param msg
     * @return
     */
    public static String toText(ITextComponent msg) {
        // FIXME: Need better implementation!
        if (msg == null) return "";
        return msg.toString();
    }

    public static String getTextWithoutFormattingCodes(@Nullable String text)
    {
        return text == null ? null : FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    public static String getTextWithoutFormattingCodes(@Nullable ITextComponent msg)
    {
        return getTextWithoutFormattingCodes(McIf.toText(msg));
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

    public static MatrixStack matrix() {
        return new MatrixStack();
    }

    public static void sendMessage(ITextComponent msg) {
        mc().gui.getChat().addMessage(msg);
    }

}
