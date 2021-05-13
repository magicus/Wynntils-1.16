/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils;

import com.wynntils.core.CoreManager;
import com.wynntils.core.events.custom.ClientEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.framework.rendering.WynnRenderItem;
import com.wynntils.core.framework.rendering.textures.Mappings;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.modules.ModuleRegistry;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.modules.core.overlays.ui.ModConflictScreen;
import com.wynntils.modules.map.MapModule;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.modules.map.overlays.objects.MapApiIcon;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class ModCore {

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class GatherDataSubscriber {
        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            System.out.println("Got DATA event " + event.getModContainer().getModInfo().getDisplayName());
        }
    }
    public static File jarFile = null;

    @Mod.EventHandler
    public void preInit(FMLCommonSetupEvent e) {
        Reference.VERSION = null; // e.getModMetadata().version;
        String[] splitDescription = "1 0".split(" "); // e.getModMetadata().description.split(" ");
        try {
            Reference.BUILD_NUMBER = Integer.parseInt(splitDescription[splitDescription.length - 1]);
        } catch (NumberFormatException ignored) {}

        jarFile = null; //e.getSourceFile();

        boolean isFmlDevEnv = true; //(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment")
        Reference.developmentEnvironment = (isFmlDevEnv)
                || (System.getProperty("wynntils.development") != null && System.getProperty("wynntils.development").equals("true"));
        // Reference.developmentEnvironment = false;  // Uncomment to test updater

        if (Reference.developmentEnvironment)
            Reference.LOGGER.info("Development environment detected, automatic update detection disabled");

        WebManager.setupUserAccount();
        WebManager.setupWebApi(true);

        CoreManager.preModules();

        ModuleRegistry.registerModules();
        FrameworkManager.startModules();

        CoreManager.afterModules();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        Map<String, String> conflicts = new HashMap<>();
        Optional<? extends ModContainer> modOpt = ModList.get().getModContainerById("labymod");
        if (modOpt.isPresent()) {
            ModContainer mod = modOpt.get();
            conflicts.put(mod.getModInfo().getDisplayName(), mod.getModInfo().getVersion().toString());
        }

        if (!conflicts.isEmpty()) throw new ModConflictScreen(conflicts);

        WynnRenderItem.inject();

        FrameworkManager.postEnableModules();

        // HeyZeer0: This will reload our cache if a texture or similar is applied
        // This also immediately loads it
        ((SimpleReloadableResourceManager)Minecraft.getInstance().getResourceManager()).registerReloadListener(resourceManager -> {
            Textures.loadTextures();
            Mappings.loadMappings();
            MapApiIcon.resetApiMarkers();
        });

        if (MapConfig.INSTANCE.enabledMapIcons.containsKey("tnt")) {
            MapConfig.INSTANCE.enabledMapIcons = MapConfig.resetMapIcons(false);
            MapConfig.INSTANCE.saveSettings(MapModule.getModule());
        }

        CrashReportExtender.registerCrashCallable(new ICrashCallable() {
            @Override
            public String getLabel() {
                return "Wynntils Details";
            }

            @Override
            public String call() {
                UpdateStream stream = CoreDBConfig.INSTANCE == null ? null : CoreDBConfig.INSTANCE.updateStream;
                return "Running Wynntils v" + Reference.VERSION + " in " + stream + ", " + (Reference.developmentEnvironment ? "being a dev env" : "at a normal env") + (Reference.onBeta ? " (This crash occured on the Hero Beta)" : "");
            }
        });

        FrameworkManager.getEventBus().post(new ClientEvent.Ready());
    }

    public static Minecraft mc() {
        return Minecraft.getInstance();
    }

}
