/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.managers;

import com.wynntils.McIf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class SpeedometerManager {

    public static double getCurrentSpeed() {
        ClientPlayerEntity player = McIf.player();

        double distX = player.getX() - player.prevPosX;
        double distZ = player.getZ() - player.prevPosZ;

        return (MathHelper.sqrt((distX * distX) + (distZ * distZ))) * 20d;
    }

}
