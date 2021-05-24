package com.wynntils.transition;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface ICommandSender {
    String getName();

    default ITextComponent getDisplayName() {
        return null;
    }

    default void sendMessage(ITextComponent p_sendMessage_1_) {
    }

    boolean canUseCommand(int var1, String var2);

    default BlockPos getPosition() {
        return null;
    }

    default Object getPositionVector() {
        return null;
    }

    World getEntityWorld();

    @Nullable
    default Entity getCommandSenderEntity() {
        return null;
    }

    default boolean sendCommandFeedback() {
        return false;
    }

    default void setCommandStat(Object p_setCommandStat_1_, int p_setCommandStat_2_) {
    }

    @Nullable
    MinecraftServer getServer();
}
