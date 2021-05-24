/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IngredientFilterOverlay implements Listener {

    private final static List<String> professionArray = new ArrayList<>(Arrays.asList("-", "None", "Ⓐ", "Cooking", "Ⓓ", "Jeweling", "Ⓔ", "Scribing", "Ⓕ", "Tailoring", "Ⓖ", "Weapon smithing", "Ⓗ", "Armouring", "Ⓘ", "Woodworking", "Ⓛ", "Alchemism"));

    @SubscribeEvent
    public void init(GuiOverlapEvent.ChestOverlap.InitGui e) {
        if (!Reference.onWorld || !UtilitiesConfig.Items.INSTANCE.filterEnabled) return;

        e.getButtonList().add(
                new Button(11,
                        (e.getGui().width - e.getGui().getXSize()) / 2 - 20,
                        (e.getGui().height - e.getGui().getYSize()) / 2 + 15,
                        18, 18,
                        RarityColorOverlay.getProfessionFilter()
                )
        );
    }

    @SubscribeEvent
    public void renderPost(GuiOverlapEvent.ChestOverlap.DrawScreen.Post e) {
        e.getButtonList().forEach(gb -> {
            if (gb.id == 11 && gb.isMouseOver()) {
                e.getGui().drawHoveringText(professionArray.get(professionArray.indexOf(gb.getMessage()) + 1), e.getMouseX(), e.getMouseY());
            }
        });
    }

    @SubscribeEvent
    public void mouseClicked(GuiOverlapEvent.ChestOverlap.MouseClicked e) {
        e.getButtonList().forEach(gb -> {
            if (gb.id == 11 && gb.isMouseOver()) {
                char c;
                if (e.getMouseButton() == 0) {
                    c = professionArray.get((professionArray.indexOf(gb.getMessage()) + 2) % 18).charAt(0);
                    gb.setMessage(Character.toString(c));
                    RarityColorOverlay.setProfessionFilter(gb.getMessage());
                } else if (e.getMouseButton() == 1) {
                    c = professionArray.get((professionArray.indexOf(gb.getMessage()) + 16) % 18).charAt(0);
                    gb.setMessage(Character.toString(c));
                    RarityColorOverlay.setProfessionFilter(gb.getMessage());
                } else if (e.getMouseButton() == 2) {
                    RarityColorOverlay.setProfessionFilter("-");
                    gb.setMessage("-");
                }
                gb.playPressSound(McIf.mc().getSoundManager());
            }
        });
    }
}
