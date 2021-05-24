package com.wynntils.transition;

import net.minecraft.command.CommandException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;


public interface ICommand extends Comparable<ICommand> {
    String getName();

    String getUsage(ICommandSender var1);

    List<String> getAliases();

    void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException;

    boolean checkPermission(MinecraftServer var1, ICommandSender var2);

    List<String> getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4);

    boolean isUsernameIndex(String[] var1, int var2);
}
