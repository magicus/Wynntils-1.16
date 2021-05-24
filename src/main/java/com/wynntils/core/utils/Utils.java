/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wynntils.McIf;
import com.wynntils.core.utils.reflections.ReflectionFields;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final DataParameter<String> NAME_KEY = ReflectionFields.Entity_CUSTOM_NAME.getValue(Entity.class);
    private static final DataParameter<Boolean> NAME_VISIBLE_KEY = ReflectionFields.Entity_CUSTOM_NAME_VISIBLE.getValue(Entity.class);
    private static final DataParameter<Boolean> ITEM_KEY = ReflectionFields.ItemFrameEntity_ITEM.getValue(Entity.class);
    public static final Pattern CHAR_INFO_PAGE_TITLE = Pattern.compile("§c([0-9]+)§4 skill points? remaining");
    public static final Pattern SERVER_SELECTOR_TITLE = Pattern.compile("Wynncraft Servers(: Page \\d+)?");

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("wynntils-utilities-%d").build());
    private static Random random = new Random();

    private static ScorePlayerTeam previousTeam = null;

    /**
     * Runs a runnable after the determined time
     *
     * @param r the runnable
     * @param timeUnit the time unit
     * @param amount the amount of the specified time unit
     */
    public static ScheduledFuture<?> runAfter(Runnable r, TimeUnit timeUnit, long amount) {
        return executorService.scheduleAtFixedRate(r, 0, amount, timeUnit);
    }

    /**
     * @return the main random instance
     */
    public static Random getRandom() {
        return random;
    }

    public static ScheduledFuture runTaskTimer(Runnable r, TimeUnit timeUnit, long amount) {
        return executorService.scheduleAtFixedRate(r, 0, amount, timeUnit);
    }

    public static Future<?> runAsync(Runnable r) {
        return executorService.submit(r);
    }

    public static <T> Future<T> runAsync(Callable<T> r) {
        return executorService.submit(r);
    }

    private static final String[] directions = new String[]{ "N", "NE", "E", "SE", "S", "SW", "W", "NW" };

    /**
     * Get short direction string for a given yaw
     *
     * @param yaw player's yaw
     * @return Two or one character string
     */
    public static String getPlayerDirection(float yaw) {
        int index = (int) (MathHelper.positiveModulo(yaw + 202.5f, 360.0f) / 45.0f);

        return 0 <= index && index < 8 ? directions[index] : directions[0];
    }


    /**
     * Copy a file from a location to another
     *
     * @param sourceFile The source file
     * @param destFile Where it will be
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (destFile == null || !destFile.exists()) {
            destFile = new File(new File(sourceFile.getParentFile(), "mods"), "Wynntils.jar");
            sourceFile.renameTo(destFile);
            return;
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static float easeOut(float current, float goal, float jump, float speed) {
        if (Math.floor(Math.abs(goal - current) / jump) > 0) {
            return current + (goal - current) / speed;
        } else {
            return goal;
        }
    }

    public static String getPlayerHPBar(PlayerEntity entityPlayer) {
        int health = (int) (0.3f + (entityPlayer.getHealth() / entityPlayer.getMaxHealth()) * 15);  // 0.3f for better experience rounding off near full hp
        String healthBar = TextFormatting.DARK_RED + "[" + TextFormatting.RED + "|||||||||||||||" + TextFormatting.DARK_RED + "]";
        healthBar = healthBar.substring(0, 5 + Math.min(health, 15)) + TextFormatting.DARK_GRAY + healthBar.substring(5 + Math.min(health, 15));
        if (health < 8) { healthBar = healthBar.replace(TextFormatting.RED.toString(), TextFormatting.GOLD.toString()); }
        return healthBar;
    }

    /**
     * Return true if the Screen is the character information page (selected from the compass)
     */
    public static boolean isCharacterInfoPage(Screen gui) {
        if (!(gui instanceof ContainerScreen)) return false;
        Matcher m = CHAR_INFO_PAGE_TITLE.matcher(McIf.toText(gui.getTitle()));
        return m.find();
    }

    /**
     * @return true if the Screen is the server selection, false otherwise
     */
    public static boolean isServerSelector(Screen gui) {
        if (!(gui instanceof ContainerScreen)) return false;
        Matcher m = SERVER_SELECTOR_TITLE.matcher(McIf.toText(gui.getTitle()));
        return m.find();
    }

    /**
     * Creates a Fake scoreboard
     *
     * @param name Scoreboard Name
     * @param rule Collision Rule
     * @return the Scoreboard Team
     */
    public static ScorePlayerTeam createFakeScoreboard(String name, Team.CollisionRule rule) {
        Scoreboard scoreboard = McIf.world().getScoreboard();
        if (scoreboard.getPlayerTeam(name) != null) return scoreboard.getPlayerTeam(name);

        String player = McIf.toText(McIf.player().getName());
        if (scoreboard.getPlayersTeam(player) != null) previousTeam = scoreboard.getPlayersTeam(player);

        ScorePlayerTeam team = scoreboard.addPlayerTeam(name);
        team.setCollisionRule(rule);

        scoreboard.addPlayerToTeam(player, team);
        return team;
    }

    /**
     * Deletes a fake scoreboard from existence
     *
     * @param name the scoreboard name
     */
    public static void removeFakeScoreboard(String name) {
        Scoreboard scoreboard = McIf.world().getScoreboard();
        if (scoreboard.getPlayerTeam(name) == null) return;

        scoreboard.removePlayerTeam(scoreboard.getPlayerTeam(name));
        if (previousTeam != null) scoreboard.addPlayerToTeam(McIf.toText(McIf.player().getName()), previousTeam);
    }

    /**
     * Opens a guiScreen without cleaning the users keys/mouse movents
     *
     * @param screen the provided screen
     */
    public static void setScreen(Screen screen) {
        Screen oldScreen = McIf.mc().screen;

        GuiOpenEvent event = new GuiOpenEvent(screen);
        if (MinecraftForge.EVENT_BUS.post(event)) return;
        screen = event.getGui();

        if (oldScreen == screen) return;
        if (oldScreen != null) {
            oldScreen.removed();
        }

        McIf.mc().screen = screen;

        if (screen != null) {
            screen.init(McIf.mc(), McIf.mc().getWindow().getGuiScaledWidth(), McIf.mc().getWindow().getGuiScaledHeight());
            McIf.mc().noRender = false;
            NarratorChatListener.INSTANCE.sayNow(screen.getNarrationMessage());
        } else {
            McIf.mc().getSoundManager().resume();
        }
    }

    private static int doubleClickTime = -1;

    /**
     * @return Maximum milliseconds between clicks to count as a double click
     */
    public static int getDoubleClickTime() {
        if (doubleClickTime < 0) {
            Object prop = Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
            if (prop instanceof Integer) {
                doubleClickTime = (Integer) prop;
            }
            if (doubleClickTime < 0) {
                doubleClickTime = 500;
            }
        }
        return doubleClickTime;
    }

    /**
     * Write a String, `s`, to the clipboard. Clears if `s` is null.
     */
    public static void copyToClipboard(String s) {
        if (s == null) {
            clearClipboard();
        } else {
            copyToClipboard(new StringSelection(s));
        }
    }

    public static void copyToClipboard(StringSelection s) {
        if (s == null) {
            clearClipboard();
        } else {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(s, null);
        }
    }

    public static String getRawItemName(ItemStack stack) {
        final Pattern PERCENTAGE_PATTERN = Pattern.compile(" +\\[[0-9]+%\\]");
        final Pattern INGREDIENT_PATTERN = Pattern.compile(" +\\[✫+\\]");

        String name = McIf.toText(stack.getDisplayName());
        name = McIf.getTextWithoutFormattingCodes(name);
        name = PERCENTAGE_PATTERN.matcher(name).replaceAll("");
        name = INGREDIENT_PATTERN.matcher(name).replaceAll("");
        if (name.startsWith("Perfect ")) {
            name = name.substring(8);
        }
        name = com.wynntils.core.utils.StringUtils.normalizeBadString(name);
        return name;
    }

    /**
     * Transform an item name and encode it so it can be used in an URL.
     */
    public static String encodeItemNameForUrl(ItemStack stack) {
        String name = getRawItemName(stack);
        name = Utils.encodeUrl(name);
        return name;
    }

    /**
     * Open the specified URL in the user's browser if possible, otherwise copy it to the clipboard
     * and send it to chat.
     * @param url The url to open
     */
    public static void openUrl(String url) {
        try {
            if (Util.getPlatform() == Util.OS.WINDOWS) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (Util.getPlatform() == Util.OS.OSX) {
                Runtime.getRuntime().exec("open " + url);
                // Keys can get "stuck" in LWJGL on macOS when the Minecraft window loses focus.
                // Reset keyboard to solve this.
                // FIXME: hopefully fixed...
         //       Keyboard.destroy();
         //       Keyboard.create();
            } else {
                Runtime.getRuntime().exec("xdg-open " + url);
            }
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Utils.copyToClipboard(url);
        StringTextComponent text = new StringTextComponent("Error opening link, it has been copied to your clipboard\n");
        text.setStyle(text.getStyle().withColor(TextFormatting.DARK_RED));

        // FIXME: can be expressed more elegantly
        StringTextComponent urlComponent = new StringTextComponent(url);
        urlComponent.setStyle(urlComponent.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)));
        urlComponent.setStyle(urlComponent.getStyle().withColor(TextFormatting.DARK_AQUA));
        urlComponent.setStyle(urlComponent.getStyle().withUnderlined(true));
        text.append(urlComponent);

        McIf.sendMessage(text);
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException ignored) {
            // will not happen since UTF-8 is part of core charsets
            return null;
        }
    }

    public static String encodeForWikiTitle(String pageTitle) {
        return encodeUrl(pageTitle.replace(" ", "_"));
    }

    public static String encodeForCargoQuery(String name) {
        return encodeUrl("'" + name.replace("'", "\\'") + "'");
    }

    public static void clearClipboard() {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
            public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[0]; }
            public boolean isDataFlavorSupported(DataFlavor flavor) { return false; }
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException { throw new UnsupportedFlavorException(flavor); }
        }, null);
    }

    /**
     * @return A String read from the clipboard, or null if the clipboard does not contain a string
     */
    public static String pasteFromClipboard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            return null;
        }
    }

    public static void tab(int amount, TextFieldWidget... tabList) {
        tab(amount, Arrays.asList(tabList));
    }

    /**
     * Given a list of text fields, blur the currently focused field and focus the
     * next one (or previous one if amount is -1), wrapping around.
     * Focuses the first one if there is no focused field.
     */
    public static void tab(int amount, List<TextFieldWidget> tabList) {
        int focusIndex = -1;
        for (int i = 0; i < tabList.size(); ++i) {
            TextFieldWidget field = tabList.get(i);
            if (field.isFocused()) {
                focusIndex = i;
                field.setCursorPosition(0);
                field.setHighlightPos(0);
                field.setFocus(false);
                break;
            }
        }
        focusIndex = focusIndex == -1 ? 0 : Math.floorMod(focusIndex + amount, tabList.size());
        TextFieldWidget selected = tabList.get(focusIndex);
        selected.setFocus(true);
        selected.setCursorPosition(0);
        selected.setHighlightPos(selected.getValue().length());
    }

    public static String getNameFromMetadata(List <EntityDataManager.DataEntry<?>> dataManagerEntries) {
        assert NAME_KEY != null;
        if (dataManagerEntries != null) {
            for (EntityDataManager.DataEntry<?> entry : dataManagerEntries) {
                if (NAME_KEY.equals(entry.getAccessor())) {
                    return (String) entry.getValue();
                }
            }
        }
        return null;
    }

    public static boolean isNameVisibleFromMetadata(List <EntityDataManager.DataEntry<?>> dataManagerEntries) {
        assert NAME_VISIBLE_KEY != null;
        if (dataManagerEntries != null) {
            for (EntityDataManager.DataEntry<?> entry : dataManagerEntries) {
                if (NAME_VISIBLE_KEY.equals(entry.getAccessor())) {
                    return (Boolean) entry.getValue();
                }
            }
        }
        // assume false if not specified
        return false;
    }

    public static ItemStack getItemFromMetadata(List <EntityDataManager.DataEntry<?>> dataManagerEntries) {
        assert ITEM_KEY != null;
        if (dataManagerEntries != null) {
            for (EntityDataManager.DataEntry<?> entry : dataManagerEntries) {
                if (ITEM_KEY.equals(entry.getAccessor())) {
                    return (ItemStack) entry.getValue();
                }
            }
        }
        return ItemStack.EMPTY;
    }


    public static boolean isKeyDown(int keycode) {
        int state = GLFW.glfwGetKey(McIf.mc().getWindow().getWindow(), keycode);
        return (state == GLFW.GLFW_PRESS);
    }

    public static boolean isAirBlock(World world, BlockPos pos)
    {
        return world.getBlockState(pos).getBlock().isAir(world.getBlockState(pos), McIf.world(), pos);
    }

    // Alias if using already imported org.apache.commons.lang3.StringUtils
    public static class StringUtils extends com.wynntils.core.utils.StringUtils { }

}
