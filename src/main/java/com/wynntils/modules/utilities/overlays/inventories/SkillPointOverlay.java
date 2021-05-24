/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.events.custom.PacketEvent;
import com.wynntils.core.framework.enums.MouseButton;
import com.wynntils.core.framework.enums.SkillPoint;
import com.wynntils.core.framework.enums.SpellType;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.ui.elements.GuiTextFieldWynn;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.core.overlays.inventories.ChestReplacer;
import com.wynntils.modules.utilities.UtilitiesModule;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import com.wynntils.modules.utilities.instances.SkillPointAllocation;
import com.wynntils.modules.utilities.overlays.ui.SkillPointLoadoutUI;
import net.minecraft.client.audio.SimpleSound;
import com.wynntils.transition.GlStateManager;
import com.wynntils.transition.RenderHelper;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkillPointOverlay implements Listener {

    private static final Pattern SKILLPOINT_PATTERN = Pattern.compile(".*?([-0-9]+)(?=\\spoints).*");
    private static final Pattern[] MODIFIER_PATTERNS = {
            Pattern.compile("- Strength: ([-+0-9]+)"),
            Pattern.compile("- Dexterity: ([-+0-9]+)"),
            Pattern.compile("- Intelligence: ([-+0-9]+)"),
            Pattern.compile("- Defence: ([-+0-9]+)"),
            Pattern.compile("- Agility: ([-+0-9]+)"),
    };

    private static final int SAVE_SLOT = 1;
    private static final int LOAD_SLOT = 3;

    private GuiTextFieldWynn nameField;

    private int skillPointsRemaining;
    private SkillPointAllocation loadedBuild = null;
    private boolean startBuild = false;
    private boolean itemsLoaded = false;
    private float buildPercentage = 0.0f;

    @SubscribeEvent
    public void onChestClose(GuiOverlapEvent.ChestOverlap.GuiClosed e) {
        nameField = null;
        itemsLoaded = false;

        Keyboard.enableRepeatEvents(false);
    }

    @SubscribeEvent
    public void onDrawScreen(GuiOverlapEvent.ChestOverlap.DrawScreen e) {
        if (!Reference.onWorld) return;
        if (!Utils.isCharacterInfoPage(e.getGui())) return;

        addManaTables(e.getGui());
    }

    @SubscribeEvent
    public void onChestInventory(GuiOverlapEvent.ChestOverlap.DrawScreen.Pre e) {
        Matcher m = Utils.CHAR_INFO_PAGE_TITLE.matcher(e.getGui().getLowerInv().getName());
        if (!m.find()) return;

        skillPointsRemaining = Integer.parseInt(m.group(1));

        // load/save loadout items
        ItemStack save = new ItemStack(Items.WRITABLE_BOOK);
        save.setStackDisplayName(TextFormatting.GOLD + "[>] Save current loadout");
        ItemUtils.replaceLore(save, Arrays.asList(TextFormatting.GRAY + "Allows you to save this loadout with a name."));
        e.getGui().getMenu().getSlot(SAVE_SLOT).set(save);

        ItemStack load = new ItemStack(Items.ENCHANTED_BOOK);
        load.setStackDisplayName(TextFormatting.GOLD + "[>] Load loadout");
        ItemUtils.replaceLore(load, Arrays.asList(TextFormatting.GRAY + "Allows you to load one of your saved loadouts."));
        e.getGui().getMenu().getSlot(LOAD_SLOT).set(load);

        // skill point allocating
        if (!itemsLoaded || startBuild) {
            startBuild = false;
            allocateSkillPoints(e.getGui());
        }

        for (int i = 0; i < e.getGui().getLowerInv().getContainerSize(); i++) {
            ItemStack stack = e.getGui().getLowerInv().getItem(i);
            if (stack.isEmpty() || !stack.hasCustomHoverName()) continue; // display name also checks for tag compound

            String lore = McIf.getTextWithoutFormattingCodes(ItemUtils.getStringLore(stack));
            String name = McIf.getTextWithoutFormattingCodes(stack.getDisplayName());
            int value = 0;

            if (name.contains("Upgrade")) {// Skill Points
                Matcher spm = SKILLPOINT_PATTERN.matcher(lore);
                if (!spm.find()) continue;

                value = Integer.parseInt(spm.group(1));
            } else if (name.contains("Profession")) { // Profession Icons
                int start = lore.indexOf("Level: ") + 7;
                int end = lore.indexOf("XP: ");

                value = Integer.parseInt(lore.substring(start, end));
            } else if (name.contains("'s Info")) { // Combat level on Info
                int start = lore.indexOf("Combat Lv: ") + 11;
                int end = lore.indexOf("Class: ");

                value = Integer.parseInt(lore.substring(start, end));
            } else if (name.contains("Damage Info")) { //Average Damage
                Pattern pattern = Pattern.compile("Total Damage \\(\\+Bonus\\): ([0-9]+)-([0-9]+)");
                Matcher m2  = pattern.matcher(lore);
                if (!m2.find()) continue;

                int min = Integer.parseInt(m2.group(1));
                int max = Integer.parseInt(m2.group(2));

                value = Math.round((max + min) / 2.0f);
            } else if (name.contains("Daily Rewards")) { //Daily Reward Multiplier
                int start = lore.indexOf("Streak Multiplier: ") + 19;
                int end = lore.indexOf("Log in everyday to");

                value = Integer.parseInt(lore.substring(start, end));
            } else continue;

            stack.setCount(value <= 0 ? 1 : value);
        }
    }

    @SubscribeEvent
    public void onChestForeground(GuiOverlapEvent.ChestOverlap.DrawGuiContainerForegroundLayer e) {
        if (!Reference.onWorld) return;
        if (!Utils.isCharacterInfoPage(e.getGui())) return;

        // draw name field
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 500);
        if (nameField != null) nameField.drawTextBox();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChestGui(GuiOverlapEvent.ChestOverlap.HoveredToolTip.Pre e) {
        if (!Reference.onWorld || !Utils.isCharacterInfoPage(e.getGui())) return;

        for (Slot s : e.getGui().getMenu().slots) {
            String name = McIf.getTextWithoutFormattingCodes(s.getItem().getDisplayName());
            SkillPoint skillPoint = SkillPoint.findSkillPoint(name);
            if (skillPoint == null) continue;

            ScreenRenderer.beginGL(e.getGui().getGuiLeft() , e.getGui().getGuiTop());
            {
                GlStateManager.translate(0, 0, 251);
                ScreenRenderer r = new ScreenRenderer();
                RenderHelper.disableStandardItemLighting();
                r.drawString(skillPoint.getColoredSymbol(), s.xPos + 2, s.yPos, CommonColors.WHITE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
            }
            ScreenRenderer.endGL();
        }
    }

    @SubscribeEvent
    public void onSlotClicked(GuiOverlapEvent.ChestOverlap.HandleMouseClick e) {
        if (!Reference.onWorld || !Utils.isCharacterInfoPage(e.getGui())) return;

        if (e.getSlotId() == SAVE_SLOT) {
            nameField = new GuiTextFieldWynn(200, McIf.mc().font, 8, 5, 130, 10);
            nameField.setFocused(true);
            nameField.setValue("Enter build name");
            Keyboard.enableRepeatEvents(true);

            e.setCanceled(true);
        } else if (e.getSlotId() == LOAD_SLOT) {
            McIf.mc().setScreen(
                    new SkillPointLoadoutUI(this, McIf.mc().screen,
                            new Inventory("Skill Points Loadouts", false, 54))
            );

            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMouseClicked(GuiOverlapEvent.ChestOverlap.MouseClicked e) {
        if (!Reference.onWorld || !Utils.isCharacterInfoPage(e.getGui())) return;

        int offsetMouseX = e.getMouseX() - e.getGui().getGuiLeft();
        int offsetMouseY = e.getMouseY() - e.getGui().getGuiTop();

        // handle mouse input on name editor
        if (nameField == null) return;

        nameField.mouseClicked(offsetMouseX, offsetMouseY, e.getMouseButton());
        if (e.getMouseButton() == MouseButton.LEFT.ordinal()) { // left click
            if (nameField.isFocused()) {
                nameField.setCursorPositionEnd();
                nameField.setSelectionPos(0);
                return;
            }

            nameField.setSelectionPos(nameField.getCursorPosition());
        }
    }

    @SubscribeEvent
    public void onKeyTyped(GuiOverlapEvent.ChestOverlap.KeyTyped e) {
        if (!Reference.onWorld || !Utils.isCharacterInfoPage(e.getGui())) return;

        // handle typing in text boxes
        if (nameField == null || !nameField.isFocused()) return;

        if (e.getKeyCode() == GLFW.GLFW_KEY_ENTER) {
            String name = nameField.getValue();
            nameField = null;

            name = name.replaceAll("&([a-f0-9k-or])", "§$1");
            UtilitiesConfig.INSTANCE.skillPointLoadouts.put(name, getSkillPoints(e.getGui()));
            UtilitiesConfig.INSTANCE.saveSettings(UtilitiesModule.getModule());
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.NOTE_BLOCK_PLING, 1f));
        } else if (e.getKeyCode() == GLFW.GLFW_KEY_ESCAPE) {
            nameField = null;
            loadedBuild = null;
            buildPercentage = 0.0f;
        } else {
            nameField.textboxKeyTyped(e.getTypedChar(), e.getKeyCode());
        }

        e.setCanceled(true);
    }

    @SubscribeEvent
    public void removeLoadPling(PacketEvent<SPacketCustomSound> e) {
        if (!Reference.onWorld || loadedBuild == null) return;
        if (!e.getPacket().getSoundName().equalsIgnoreCase("entity.experience_orb.pickup")) return;

        e.setCanceled(true);
    }

    private String remainingLevelsDescription(int remainingLevels) {
        return "" + TextFormatting.GOLD + remainingLevels + TextFormatting.GRAY + " point" + (remainingLevels == 1 ? "" : "s");
    }

    private int getIntelligencePoints(ItemStack stack) {
        String lore = McIf.getTextWithoutFormattingCodes(ItemUtils.getStringLore(stack));
        int start = lore.indexOf(" points ") - 3;

        return (start < 0) ? 0 : Integer.parseInt(lore.substring(start, start + 3).trim());
    }

    public void addManaTables(ChestReplacer gui) {
        ItemStack stack = gui.getLowerInv().getItem(11);
        if (stack.isEmpty() || !stack.hasCustomHoverName()) return; // display name also checks for tag compound

        int intelligencePoints = getIntelligencePoints(stack);
        if (stack.getTag().contains("wynntilsAnalyzed")) return;

        int closestUpgradeLevel = Integer.MAX_VALUE;
        int level = PlayerInfo.get(CharacterData.class).getLevel();

        List<String> newLore = new LinkedList<>();

        for (int j = 0; j < 4; j++) {
            SpellType spell = SpellType.forClass(PlayerInfo.get(CharacterData.class).getCurrentClass(), j + 1);

            if (spell.getUnlockLevel(1) <= level) {
                int nextUpgrade = spell.getNextManaReduction(level, intelligencePoints);
                if (nextUpgrade < closestUpgradeLevel) {
                    closestUpgradeLevel = nextUpgrade;
                }
                int manaCost = spell.getManaCost(level, intelligencePoints);
                String spellName = PlayerInfo.get(CharacterData.class).isReskinned() ? spell.getReskinned() : spell.getName();
                String spellInfo = TextFormatting.LIGHT_PURPLE + spellName + " Spell: " + TextFormatting.AQUA
                        + "-" + manaCost + " ✺";
                if (nextUpgrade < Integer.MAX_VALUE) {
                    spellInfo += TextFormatting.GRAY + " (-" + (manaCost - 1) + " ✺ in "
                            + remainingLevelsDescription(nextUpgrade - intelligencePoints) + ")";
                }
                newLore.add(spellInfo);
            }
        }

        List<String> loreTag = new LinkedList<>(ItemUtils.getLore(stack));
        if (closestUpgradeLevel < Integer.MAX_VALUE) {
            loreTag.add("");
            loreTag.add(TextFormatting.GRAY + "Next upgrade: At " + TextFormatting.WHITE + closestUpgradeLevel
                    + TextFormatting.GRAY + " points (in " + remainingLevelsDescription(closestUpgradeLevel - intelligencePoints) + ")");
        }

        loreTag.add("");
        loreTag.addAll(newLore);

        ItemUtils.replaceLore(stack, loreTag);
        stack.getTag().putBoolean("wynntilsAnalyzed", true);
    }

    private SkillPointAllocation getSkillPoints(ChestReplacer gui) {
        int[] sp = new int[5];

        for (int i = 0; i < 5; i++) {
            ItemStack stack = gui.getLowerInv().getItem(i + 9); // sp indicators start at 9
            Matcher m = SKILLPOINT_PATTERN.matcher(McIf.getTextWithoutFormattingCodes(ItemUtils.getStringLore(stack)));
            if (!m.find()) continue;

            sp[i] = Integer.parseInt(m.group(1));
        }

        // following code subtracts gear sp from total sp to find player-allocated sp
        ItemStack info = gui.getLowerInv().getItem(6); // player info slot
        for (String line : ItemUtils.getLore(info)) {
            String unformattedLine = McIf.getTextWithoutFormattingCodes(line);
            for (int i = 0; i < 5; i++) {
                Matcher m = MODIFIER_PATTERNS[i].matcher(unformattedLine);
                if (!m.find()) continue;

                int modifier = Integer.parseInt(m.group(1));
                sp[i] -= modifier;
            }
        }

        return new SkillPointAllocation(sp[0], sp[1], sp[2], sp[3], sp[4]);
    }

    public void loadBuild(SkillPointAllocation build) {
        if (build.getTotalSkillPoints() > skillPointsRemaining) {
            StringTextComponent text = new StringTextComponent("Not enough free skill points!");
            text.getStyle().setColor(TextFormatting.RED);

            McIf.sendMessage(text);
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.BLOCK_ANVIL_PLACE, 1f));
            return;
        }

        loadedBuild = build;
        startBuild = true;
    }

    private void allocateSkillPoints(ChestReplacer gui) {
        if (loadedBuild == null) return;
        if (gui.getMenu().getSlot(9).getItem().isEmpty()) return;
        itemsLoaded = true;

        int[] currentSp = getSkillPoints(gui).getAsArray();
        int[] buildSp = loadedBuild.getAsArray();

        float perSkill = 1 / (float) loadedBuild.getTotalSkillPoints();

        for (int i = 0; i < 5; i++) {
            if (currentSp[i] >= buildSp[i]) continue;
            int button = (buildSp[i] - currentSp[i] >= 5) ? 1 : 0; // right click if difference >= 5 for efficiency

            buildPercentage += perSkill * (button == 1 ? 5 : 0);

            CClickWindowPacket packet = new CClickWindowPacket(gui.getMenu().windowId, 9 + i, button,
                    ClickType.PICKUP, gui.getMenu().getSlot(9 + i).getItem(),
                    gui.getMenu().getNextTransactionID(McIf.player().inventory));

            McIf.mc().getSoundManager().play(
                    SimpleSound.forUI(SoundEvents.ITEM_PICKUP, 0.3f + (1.2f * buildPercentage)));

            McIf.mc().getConnection().send(packet);
            return; // can only click once at a time
        }

        McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.PLAYER_LEVELUP, 1f));
        loadedBuild = null; // we've fully loaded the build if we reach this point
        buildPercentage = 0.0f;
    }

}
