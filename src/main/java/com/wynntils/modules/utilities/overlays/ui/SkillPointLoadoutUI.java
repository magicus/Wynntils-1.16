/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.utilities.overlays.ui;

import com.wynntils.McIf;
import com.wynntils.core.framework.enums.SkillPoint;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.modules.utilities.UtilitiesModule;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import com.wynntils.modules.utilities.instances.ContainerBuilds;
import com.wynntils.modules.utilities.instances.SkillPointAllocation;
import com.wynntils.modules.utilities.overlays.inventories.SkillPointOverlay;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import com.wynntils.transition.GlStateManager;
import net.minecraft.item.Items;
import net.minecraft.util.SoundEvents;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class SkillPointLoadoutUI extends FakeGuiContainer {

    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    private final SkillPointOverlay parent;
    private final Screen spMenu;
    private final Inventory inventory;
    private final int inventoryRows;

    public SkillPointLoadoutUI(SkillPointOverlay parent, Screen spMenu, Inventory inventory) {
        super(new ContainerBuilds(inventory, McIf.player()));
        this.parent = parent;
        this.spMenu = spMenu;
        this.inventory = inventory;
        this.inventoryRows = inventory.getContainerSize() / 9;
        this.ySize = this.inventoryRows * 18;
    }

    @Override
    public void init() {
        super.init();
        inventory.clear();

        int i = 0;
        for (String name : UtilitiesConfig.INSTANCE.skillPointLoadouts.keySet()) {
            if (i > 53) break;
            SkillPointAllocation sp = UtilitiesConfig.INSTANCE.skillPointLoadouts.get(name);

            int levelRequirement = (sp.getTotalSkillPoints() / 2) + 1;
            boolean hasRequirement = PlayerInfo.get(CharacterData.class).getLevel() >= levelRequirement;

            ItemStack buildStack = new ItemStack(Items.DIAMOND_AXE);
            buildStack.setDamageValue(hasRequirement ? 42 : 44);
            buildStack.setStackDisplayName(TextFormatting.DARK_AQUA + name);
            buildStack.setTagInfo("Unbreakable", new NBTTagByte((byte) 1));
            buildStack.setTagInfo("HideFlags", new NBTTagInt(6));

            List<String> lore = new ArrayList<>();
            if (sp.getStrength() > 0) lore.add(TextFormatting.GRAY + "-" + TextFormatting.DARK_GREEN + " " + sp.getStrength() + " " + SkillPoint.STRENGTH.getSymbol());
            if (sp.getDexterity() > 0) lore.add(TextFormatting.GRAY + "-" + TextFormatting.YELLOW + " " + sp.getDexterity() + " " + SkillPoint.DEXTERITY.getSymbol());
            if (sp.getIntelligence() > 0) lore.add(TextFormatting.GRAY + "-" + TextFormatting.AQUA + " " + sp.getIntelligence() + " " + SkillPoint.INTELLIGENCE.getSymbol());
            if (sp.getDefence() > 0) lore.add(TextFormatting.GRAY + "-" + TextFormatting.RED + " " + sp.getDefence() + " " + SkillPoint.DEFENCE.getSymbol());
            if (sp.getAgility() > 0) lore.add(TextFormatting.GRAY + "-" + TextFormatting.WHITE + " " + sp.getAgility() + " " + SkillPoint.AGILITY.getSymbol());

            // requirements
            lore.add(" ");
            lore.add((hasRequirement ? TextFormatting.GREEN + "✔" : TextFormatting.RED + "✖") +
                    TextFormatting.GRAY + " Combat Lv. Min: " + levelRequirement);

            // actions
            lore.add(" ");
            lore.add((hasRequirement ? (TextFormatting.GREEN) : TextFormatting.DARK_GRAY) + "Left-click to load");
            lore.add(TextFormatting.RED + "Right-click to delete");
            ItemUtils.replaceLore(buildStack, lore);

            inventory.setInventorySlotContents(i, buildStack);
            i++;
        }
    }

    @Override
    protected void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (slotIn == null || slotIn.getItem().isEmpty()) return;
        if (slotId >= UtilitiesConfig.INSTANCE.skillPointLoadouts.size()) return;

        String name = McIf.getTextWithoutFormattingCodes(slotIn.getItem().getDisplayName());
        if (mouseButton == 0) { // left click <-> load
            SkillPointAllocation aloc = getLoadout(name);
            if (aloc == null) return;

            int levelRequirement = (aloc.getTotalSkillPoints() / 2) + 1;
            if (PlayerInfo.get(CharacterData.class).getLevel() < levelRequirement) return;

            parent.loadBuild(aloc); // sends the allocated loadout into

            McIf.mc().setScreen(spMenu);
            return;
        }

        if (mouseButton == 1) { // right click <-> delete
            List<String> lore = ItemUtils.getLore(slotIn.getItem());
            if (lore.get(lore.size() - 1).contains("confirm")) { // confirm deletion
                McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.ENTITY_IRONGOLEM_HURT, 1f));

                removeLoadout(name);
                this.init();
                return;
            }

            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.BLOCK_ANVIL_LAND, 1f));
            lore.set(lore.size() -1, TextFormatting.DARK_RED + "> Right-click to confirm deletion");
            ItemUtils.replaceLore(slotIn.getItem(), lore);
        }
    }

    @Override
    protected void keyPressed(char typedChar, int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == McIf.mc().options.keyBindInventory.getKey().getValue()) {
            McIf.mc().setScreen(spMenu);
        }
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.render(matrix, mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(McIf.getUnformattedText(this.inventory.getDisplayName()), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        McIf.mc().getTextureManager().bind(CHEST_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.inventoryRows * 18 + 17);
        this.drawTexturedModalRect(i, j + this.inventoryRows * 18 + 17, 0, 213, this.xSize, 9);
    }

    private static SkillPointAllocation getLoadout(String name) {
        for (Entry<String, SkillPointAllocation> e : UtilitiesConfig.INSTANCE.skillPointLoadouts.entrySet()) {
            if (McIf.getTextWithoutFormattingCodes(e.getKey()).equals(name))
                return e.getValue();
        }
        return null;
    }

    private static void removeLoadout(String name) {
        for (Entry<String, SkillPointAllocation> e : UtilitiesConfig.INSTANCE.skillPointLoadouts.entrySet()) {
            if (McIf.getTextWithoutFormattingCodes(e.getKey()).equals(name)) {
                UtilitiesConfig.INSTANCE.skillPointLoadouts.remove(e.getKey());
                UtilitiesConfig.INSTANCE.saveSettings(UtilitiesModule.getModule());
                return;
            }
        }
    }

}
