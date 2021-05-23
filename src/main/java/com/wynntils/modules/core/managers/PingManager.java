/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.core.managers;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.utils.helpers.CommandResponse;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.regex.Pattern;

public class PingManager {

    private static final Pattern pattern = Pattern.compile("(§4/toggle)");

    private static long lastPing = 1000;
    private static long lastCall = 0;

    public static void calculatePing() {
        if (!Reference.onWorld
            || !PlayerInfo.get(CharacterData.class).isLoaded()
            || McIf.mc().screen instanceof ChatScreen
            || System.currentTimeMillis() - lastCall < 15000) return;

        CommandResponse response = new CommandResponse("/toggle", (m, t) -> {
            lastPing = System.currentTimeMillis() - lastCall;
            Reference.LOGGER.debug("Updated user ping to " + lastPing + "ms");
        }, pattern);

        response.setCancel(true);

        lastCall = System.currentTimeMillis();
        response.executeCommand();
    }

    /**
     * Returns the approximate player ping
     *
     * @return the approximate player ping
     */
    public static long getLastPing() {
        return lastPing;
    }

}
