/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.utils;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.utils.reflections.ReflectionFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConnectingScreen;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.resources.LegacyResourcePackWrapper;
import net.minecraft.client.resources.LegacyResourcePackWrapperV4;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resources.*;
import net.minecraft.resources.data.PackMetadataSection;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

public class ServerUtils {

    public static void connect(ServerData serverData) {
        connect(serverData, true);
    }

    public static void connect(ServerData serverData, boolean unloadCurrentServerResourcePack) {
        connect(new MultiplayerScreen(new MainMenuScreen()), serverData, unloadCurrentServerResourcePack);
    }

    public static void connect(Screen backGui, ServerData serverData) {
        connect(backGui, serverData, false);
    }

    /**
     * Connect to a server, possibly disconnecting if already on a world.
     *
     * @param backGui GUI to return to on failure
     * @param serverData The server to connect to
     * @param unloadCurrentServerResourcePack If false, retain the same server resource pack between disconnecting and connecting
     */
    public static void connect(Screen backGui, ServerData serverData, boolean unloadCurrentServerResourcePack) {
        disconnect(false, unloadCurrentServerResourcePack);
        McIf.mc().setScreen(new ConnectingScreen(backGui, McIf.mc(), serverData));
    }

    public static void disconnect() {
        disconnect(true);
    }

    public static void disconnect(boolean switchGui) {
        disconnect(switchGui, false);
    }

    /**
     * Disconnect from the current server
     *
     * @param switchGui If true, the current gui is changed (to the main menu in singleplayer, or multiplayer gui)
     * @param unloadServerPack if false, disconnect without refreshing resources by unloading the server resource pack
     */
    public static void disconnect(boolean switchGui, boolean unloadServerPack) {
        ClientWorld world = McIf.world();
        if (world == null) return;

        boolean singlePlayer = McIf.mc().isLocalServer();
        boolean realms = !singlePlayer && McIf.mc().isConnectedToRealms();

        world.disconnect();
        if (unloadServerPack) {
            McIf.mc().loadLevel(null);
        } else {
            loadWorldWithoutUnloadingServerResourcePack(null);
        }

        if (!switchGui) return;
        if (singlePlayer) {
            McIf.mc().setScreen(new MainMenuScreen());
        } else if (realms) {
            // Should not be possible because Wynntils will
            // never be running on the latest version of Minecraft
//            new RealmsBridge().switchToRealms(new MainMenuScreen());
        } else {
            McIf.mc().setScreen(new MultiplayerScreen(new MainMenuScreen()));
        }
    }

    public static void loadWorldWithoutUnloadingServerResourcePack(ClientWorld world) {
        loadWorldWithoutUnloadingServerResourcePack(world, "");
    }

    private static class FakeResourcePackRepositoryHolder {
        // Will only be created by classloader when used
        //       this.resourcePackRepository = new ;
        static final ResourcePackList instance = new ResourcePackList(ServerUtils::createClientPackAdapter, McIf.mc().getClientPackSource(), new FolderPackFinder(McIf.mc().getResourcePackDirectory(), IPackNameDecorator.DEFAULT)) {
            @Override
            public void close() {
                // Don't
            }
        };
    }

    ///// FIXME: BEGIN HACK. This should be replaced with Mixin access

    private static ResourcePackInfo createClientPackAdapter(String p_228011_0_, boolean p_228011_1_, Supplier<IResourcePack> p_228011_2_, IResourcePack p_228011_3_, PackMetadataSection p_228011_4_, ResourcePackInfo.Priority p_228011_5_, IPackNameDecorator p_228011_6_) {
        int i = p_228011_4_.getPackFormat();
        Supplier<IResourcePack> supplier = p_228011_2_;
        if (i <= 3) {
            supplier = adaptV3(p_228011_2_);
        }

        if (i <= 4) {
            supplier = adaptV4(supplier);
        }

        return new ResourcePackInfo(p_228011_0_, p_228011_1_, supplier, p_228011_3_, p_228011_4_, p_228011_5_, p_228011_6_, p_228011_3_.isHidden());
    }

    private static Supplier<IResourcePack> adaptV3(Supplier<IResourcePack> p_228021_0_) {
        return () -> {
            return new LegacyResourcePackWrapper(p_228021_0_.get(), LegacyResourcePackWrapper.V3);
        };
    }

    private static Supplier<IResourcePack> adaptV4(Supplier<IResourcePack> p_228022_0_) {
        return () -> {
            return new LegacyResourcePackWrapperV4(p_228022_0_.get());
        };
    }

    ///// END HACK

    public static synchronized void loadWorldWithoutUnloadingServerResourcePack(ClientWorld world, String loadingMessage) {
        ResourcePackList original = McIf.mc().getResourcePackRepository();

        ReflectionFields.Minecraft_resourcePackRepository.setValue(McIf.mc(), FakeResourcePackRepositoryHolder.instance);
        // FIXME: note that world is always null!
        McIf.mc().loadLevel(loadingMessage);
        ReflectionFields.Minecraft_resourcePackRepository.setValue(McIf.mc(), original);
    }

    public static ServerData getWynncraftServerData(boolean addNew) {
        return getWynncraftServerData(new ServerList(Minecraft.getInstance()), addNew, Reference.ServerIPS.GAME);
    }

    public static ServerData getWynncraftServerData(ServerList serverList, boolean addNew) {
        return getWynncraftServerData(serverList, addNew, Reference.ServerIPS.GAME);
    }

    /**
     * @param serverList A ServerList
     * @param addNew If true and no server data is found, the newly created server data will be added and saved.
     * @param ip The ip to use if not found
     * @return The server data in the given serverList for Wynncraft (Or a new one if none are found)
     */
    public static ServerData getWynncraftServerData(ServerList serverList, boolean addNew, String ip) {
        ServerData server = null;

        int i = 0, count = serverList.size();
        for (; i < count; ++i) {
            server = serverList.get(i);
            if (server.ip.toLowerCase(Locale.ROOT).contains("wynncraft")) {
                break;
            }
        }

        if (i >= count) {
            server = new ServerData("Wynncraft", ip, false);
            if (addNew) {
                serverList.add(server);
                serverList.save();
            }
        }

        return server;
    }

    public static ServerData changeServerIP(ServerData serverData, String newIp, String defaultName) {
        return changeServerIP(new ServerList(Minecraft.getInstance()), serverData, newIp, defaultName);
    }

    /**
     * Change the ip of a ServerData and save the results
     *
     * @param list The server list
     * @param serverData The old server data
     * @param newIp The ip to change to
     * @param defaultName The name to set to if the server data is not found
     * @return The newly saved ServerData
     */
    public static ServerData changeServerIP(ServerList list, ServerData serverData, String newIp, String defaultName) {
        if (serverData == null) {
            list.add(serverData = new ServerData(defaultName, newIp, false));
            list.save();
            return serverData;
        }

        for (int i = 0, length = list.size(); i < length; ++i) {
            ServerData fromList = list.get(i);
            if (Objects.equals(fromList.ip, serverData.ip) && Objects.equals(fromList.name, serverData.name)) {
                // Found the server data; Replace the ip
                fromList.ip = newIp;
                list.save();
                return fromList;
            }
        }

        // Not found
        list.add(serverData = new ServerData(defaultName, newIp, false));
        list.save();
        return serverData;
    }

}
