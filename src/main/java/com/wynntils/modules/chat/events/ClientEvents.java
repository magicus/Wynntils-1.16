/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.chat.events;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.WynnWorldEvent;
import com.wynntils.core.events.custom.WynncraftServerEvent;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.objects.Pair;
import com.wynntils.core.utils.reflections.ReflectionFields;
import com.wynntils.modules.chat.configs.ChatConfig;
import com.wynntils.modules.chat.managers.ChatManager;
import com.wynntils.modules.chat.managers.HeldItemChatManager;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import com.wynntils.modules.chat.overlays.gui.ChatGUI;
import com.wynntils.webapi.services.TranslationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

public class ClientEvents implements Listener {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (e.getGui() instanceof ChatScreen) {
            if (e.getGui() instanceof ChatGUI) return;
            String defaultText = ReflectionFields.GuiChat_defaultInputFieldText.getValue(e.getGui());

            e.setGui(new ChatGUI(defaultText));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatReceived(ClientChatReceivedEvent e) {
        ITextComponent msg = e.getMessage();
        if (McIf.getUnformattedText(msg).startsWith("[Info] ") && ChatConfig.INSTANCE.filterWynncraftInfo) {
            e.setCanceled(true);
        } else if (McIf.getFormattedText(msg).startsWith("\n                       " + TextFormatting.GOLD + TextFormatting.BOLD + "Welcome to Wynncraft!") &&
                !McIf.getFormattedText(msg).contains("n the Trade Market") && ChatConfig.INSTANCE.filterWynncraftInfo) {
            e.setCanceled(true);
        } else if (McIf.getFormattedText(msg).startsWith(TextFormatting.GRAY + "[You are now entering") && ChatConfig.INSTANCE.filterTerritoryEnter) {
            e.setCanceled(true);
        } else if (McIf.getFormattedText(msg).startsWith(TextFormatting.GRAY + "[You are now leaving") && ChatConfig.INSTANCE.filterTerritoryEnter) {
            e.setCanceled(true);
        }
    }

    /**
     * Used for replacing commands by others, also knows as, creating aliases
     *
     * Replacements:
     * /tell -> /msg
     * /xp -> /guild xp
     *
     * /guild att/a -> attack
     *        def/d -> defend
     *        c -> contribute
     * /party j -> join
     *        i -> invite
     *        l -> leave
     *        c -> create
     */
    @SubscribeEvent
    public void commandReplacements(ClientChatEvent e) {
        if (e.getMessage().startsWith("/tell")) e.setMessage(e.getMessage().replaceFirst("/tell", "/msg"));
        else if (e.getMessage().startsWith("/xp")) e.setMessage(e.getMessage().replaceFirst("/xp", "/guild xp"));
        else if (e.getMessage().startsWith("/gu")) e.setMessage(e.getMessage().replaceFirst(" att$", " attack").replaceFirst(" a$", " attack").replaceFirst(" def$", " defend").replaceFirst(" d$", " defend").replaceFirst(" c$", " contribute"));
        else if (e.getMessage().startsWith("/pa")) e.setMessage(e.getMessage().replaceFirst(" j ", " join ").replaceFirst(" i ", " invite ").replaceFirst(" l$", " leave").replaceFirst(" c$", " create"));
    }


    @SubscribeEvent
    public void onWynnLogin(WynncraftServerEvent.Login e) {
        ReflectionFields.IngameGui_persistantChatGUI.setValue(McIf.mc().gui, new ChatOverlay());
        TranslationManager.init();
    }

    @SubscribeEvent
    public void onWynnLogout(WynncraftServerEvent.Leave e) {
        TranslationManager.shutdown();
    }

    @SubscribeEvent
    public void onSendMessage(ClientChatEvent e) {
        if (e.getMessage().startsWith("/")) return;

        Pair<String, Boolean> message = ChatManager.applyUpdatesToServer(e.getMessage());
        e.setMessage(message.a);
        if (message.b || message.a.isEmpty() || message.a.trim().isEmpty()) {
            e.setCanceled(true);
            return;
        }

        if (!ChatOverlay.getChat().getCurrentTab().getAutoCommand().isEmpty())
            e.setMessage(ChatOverlay.getChat().getCurrentTab().getAutoCommand() + " " + e.getMessage());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (!Reference.onWorld) return;

        HeldItemChatManager.onTick();
    }

    @SubscribeEvent
    public void onLeaveWorld(WynnWorldEvent.Leave e) {
        ChatManager.onLeave();
    }

}
