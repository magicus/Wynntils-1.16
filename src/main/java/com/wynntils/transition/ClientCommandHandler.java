package com.wynntils.transition;


import net.minecraft.server.MinecraftServer;

public class ClientCommandHandler {
    public static final ClientCommandHandler instance = new ClientCommandHandler();
    public String[] latestAutoComplete = null;

    public ClientCommandHandler() {
    }

    public int executeCommand(ICommandSender sender, String message) {


        return 0;
    }

    public ICommand registerCommand(ICommand command) {
        return command;
    }

    public void autoComplete(String leftOfCursor) {


    }

    protected MinecraftServer getServer() {
        return null;
    }
}
