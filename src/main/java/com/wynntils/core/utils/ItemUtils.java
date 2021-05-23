/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.utils;

import com.wynntils.McIf;
import com.wynntils.core.utils.objects.IntRange;
import com.wynntils.core.utils.reference.EmeraldSymbols;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.item.enums.ItemType;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemUtils {

    private static final Pattern LEVEL_PATTERN = Pattern.compile("(?:Combat|Crafting|Mining|Woodcutting|Farming|Fishing) Lv\\. Min: ([0-9]+)");
    private static final Pattern LEVEL_RANGE_PATTERN = Pattern.compile("Lv\\. Range: " + TextFormatting.WHITE.toString() + "([0-9]+)-([0-9]+)");

    public static final CompoundNBT UNBREAKABLE = new CompoundNBT();

    /**
     * Get the lore NBT tag from an item
     */
    public static ListNBT getLoreTag(ItemStack item) {
        if (item.isEmpty()) return null;
        CompoundNBT display = item.getTagElement("display");
        if (display == null || !display.contains("Lore")) return null;

        INBT loreBase = display.get("Lore");
        ListNBT lore;
        if (loreBase.getId() != 9) return null;

        lore = (ListNBT) loreBase;
        if (lore.getType() != ListNBT.TYPE) return null;

        return lore;
    }

    /**
     * Get the lore from an item
     *
     * @return an {@link List} containing all item lore
     */
    public static List<String> getLore(ItemStack item) {
        ListNBT loreTag = getLoreTag(item);

        List<String> lore = new ArrayList<>();
        if (loreTag == null) return lore;

        for (int i = 0; i < loreTag.size(); ++i) {
            lore.add(loreTag.getString(i));
        }

        return lore;
    }

    /**
     * Replace the lore on an item's NBT tag.
     *
     * @param stack
     * @param lore
     */
    public static void replaceLore(ItemStack stack, List<String> lore) {
        CompoundNBT nbt = stack.getTag();
        CompoundNBT display = nbt.getCompound("display");
        ListNBT tag = new ListNBT();
        lore.forEach(s -> tag.add(StringNBT.valueOf(s)));
        display.put("Lore", tag);
        nbt.put("display", display);
        stack.setTag(nbt);
    }

    /**
     * Same as {@link #getLore(ItemStack)}, but after calling
     * {@link TextFormatting#getTextWithoutFormattingCodes(String) getTextWithoutFormattingCodes} on each lore line
     *
     * @return A List containing all item lore without formatting codes
     */
    public static List<String> getUnformattedLore(ItemStack item) {
        ListNBT loreTag = getLoreTag(item);

        List<String> lore = new ArrayList<>();
        if (loreTag == null) return lore;

        for (int i = 0; i < loreTag.size(); ++i) {
            lore.add(McIf.getTextWithoutFormattingCodes(loreTag.getString(i)));
        }

        return lore;
    }

    public static String getStringLore(ItemStack is) {
        StringBuilder toReturn = new StringBuilder();
        for (String x : getLore(is)) {
            toReturn.append(x);
        }
        return toReturn.toString();
    }

    /**
     * Determines the equipment type of the given item.
     *
     * @param item
     * @return The ItemType of the item, or null if invalid or not an equipment piece
     */
    public static ItemType getItemType(ItemStack item) {
        for (Entry<ItemType, String[]> e : WebManager.getMaterialTypes().entrySet()) {
            for (String id : e.getValue()) {
                if (id.matches("[A-Za-z_:]+")) {
                    if (Item.getByNameOrId(id).equals(item.getItem())) return e.getKey();
                } else {
                    int damageValue = 0;

                    String[] values = id.split(":");
                    int i = Integer.parseInt(values[0]);
                    if (values.length == 2) {
                        damageValue = Integer.parseInt(values[1]);
                    }

                    if (Item.getId(item.getItem()) == i && item.getDamageValue() == damageValue) return e.getKey();
                }
            }
        }
        return null;
    }

    private static final Item EMERALD_BLOCK = Item.byBlock(Blocks.EMERALD_BLOCK);

    /**
     * @return the total amount of emeralds in an inventory, including blocks and le
     */
    public static int countMoney(IInventory inv) {
        if (inv == null) return 0;

        int money = 0;

        for (int i = 0, len = inv.getContainerSize(); i < len; i++) {
            ItemStack it = inv.getItem(i);
            if (it.isEmpty()) continue;

            if (it.getItem() == Items.EMERALD && it.getDisplayName().equals(TextFormatting.GREEN + "Emerald")) {
                money += it.getCount();
            } else if (it.getItem() == EMERALD_BLOCK && it.getDisplayName().equals(TextFormatting.GREEN + "Emerald Block")) {
                money += it.getCount() * 64;
            } else if (it.getItem() == Items.EXPERIENCE_BOTTLE && it.getDisplayName().equals(TextFormatting.GREEN + "Liquid Emerald")) {
                money += it.getCount() * (64 * 64);
            }
        }

        return money;
    }

    public static String describeMoney(int total) {
        int leCount = total / 4096;
        int leRest = total % 4096;
        int emCount = leRest % 64;
        int ebCount = leRest / 64;

        StringBuilder desc = new StringBuilder();
        if (leCount > 0) {
            desc.append(leCount);
            desc.append(" ");
            desc.append(EmeraldSymbols.LE);
            desc.append(" ");
        }
        if (ebCount > 0) {
            desc.append(ebCount);
            desc.append(" ");
            desc.append(EmeraldSymbols.BLOCKS);
            desc.append(" ");
        }
        if (emCount > 0) {
            desc.append(emCount);
            desc.append(" ");
            desc.append(EmeraldSymbols.EMERALDS);
            desc.append(" ");
        }
        return desc.toString();
    }

    public static IntRange getLevel(String lore) {
        Matcher m = LEVEL_PATTERN.matcher(lore);
        if (m.find()) return new IntRange(Integer.parseInt(m.group(1)));
        m = LEVEL_RANGE_PATTERN.matcher(lore);
        if (m.find()) return new IntRange(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
        return null;
    }

    static {
        UNBREAKABLE.putBoolean("Unbreakable", true);
    }

}
