/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.questbook.overlays.ui;

import com.wynntils.McIf;
import com.wynntils.core.framework.enums.wynntils.WynntilsSound;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MainPage extends QuestBookPage {

    public MainPage() {
        super("User Profile", false, null);
    }

    @Override
    public void init() {
        super.init();
        pages = (int) Math.ceil(Arrays.stream(QuestBookPages.values()).max(Comparator.comparingInt(QuestBookPages::getSlotNb)).get().getSlotNb() / 4d);
        refreshAccepts();
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        int x = width / 2; int y = height / 2;
        int posX = (x - mouseX); int posY = (y - mouseY);
        hoveredText = new ArrayList<>();

        ScreenRenderer.beginGL(0, 0);
        {
            int right = (posX + 80);
            if (posX >= 0) right = 80;

            int up = (posY) + 30;
            if (posY >= 109) up = 109;
            if (posY <= -109) up = -109;

            InventoryScreen.drawEntityOnScreen(x + 80, y + 30, 30, right, up, McIf.player());
        }
        ScreenRenderer.endGL();

        ScreenRenderer.beginGL(0, 0);
        {
            render.drawRect(Textures.UIs.quest_book, x-168, y-81, 34, 222, 168, 33);

            String guild;
            if (WebManager.getPlayerProfile() != null)
                guild = WebManager.getPlayerProfile().getGuildRank() != null ? WebManager.getPlayerProfile().getGuildName() + " " + WebManager.getPlayerProfile().getGuildRank().getStars() : WebManager.getPlayerProfile().getGuildName();
            else
                guild = "";
            render.drawString(TextFormatting.DARK_AQUA + guild, x + 80, y - 53, CommonColors.CYAN, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
            render.drawString(McIf.player().getName(), x + 80, y - 43, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
            render.drawString(PlayerInfo.get(CharacterData.class).getCurrentClass().toString() + " Level " + PlayerInfo.get(CharacterData.class).getLevel(), x + 80, y + 40, CommonColors.PURPLE, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
            render.drawString("In Development", x + 80, y + 50, CommonColors.RED, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
            render.drawString(WebManager.getCurrentSplash(), x + 82, y + 70, CommonColors.RAINBOW, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);

            int boxTop = y - 18;
            int boxBottom = y + 12;
            selected = 0;
            for (QuestBookPages val: QuestBookPages.values()) {
                QuestBookPage qbp = val.getPage();
                if (qbp.getIcon() == null || !(val.getSlotNb() > (currentPage - 1) * 4 && val.getSlotNb() <= (currentPage) * 4)) continue;

                boolean hovering = posX >= (120 - 35 * ((val.getSlotNb() - 1) % 4)) && posX <= (150 - 35 * ((val.getSlotNb() - 1) % 4)) && posY >= -10 && posY <= 20;
                int leftX = x - 150 + 35 * ((val.getSlotNb() - 1) % 4);

                if (hovering) {
                    selected = val.getSlotNb();
                    render.drawRect(selected_cube, leftX, boxTop, leftX + 30, boxBottom);
                } else {
                    render.drawRect(unselected_cube, leftX, boxTop, leftX + 30, boxBottom);
                }
                render.drawRect(Textures.UIs.quest_book, leftX + (15 - qbp.getIcon().getWidth()/2), boxTop + (15 - qbp.getIcon().getHeight()/2), qbp.getIcon().getX1(), qbp.getIcon().getY1(hovering), qbp.getIcon().getWidth(), qbp.getIcon().getHeight());
                if (hovering) {
                    hoveredText = qbp.getHoveredDescription();
                }
            }

            // but next and back button
            if (currentPage == pages) {
                render.drawRect(Textures.UIs.quest_book, x - 64, y + 24, x - 80, y + 15, 238, 243, 254, 234);
            } else {
                if (posX >= 65 && posX <= 80 && posY >= -24 & posY <= -15) {
                    selected = -2;
                    render.drawRect(Textures.UIs.quest_book, x - 64, y + 24, x - 80, y + 15, 238, 243, 254, 234);
                } else {
                    render.drawRect(Textures.UIs.quest_book, x - 64, y + 24, x - 80, y + 15, 222, 243, 238, 234);
                }
            }

            if (currentPage == 1) {
                render.drawRect(Textures.UIs.quest_book, x - 101, y + 15, 238, 234, 16, 9);
            } else {
                if (posX >= 86 && posX <= 101 && posY >= -24 & posY <= -15) {
                    selected = -1;
                    render.drawRect(Textures.UIs.quest_book, x - 101, y + 15, 238, 234, 16, 9);
                } else {
                    render.drawRect(Textures.UIs.quest_book, x - 101, y + 15, 222, 234, 16, 9);
                }
            }

            render.drawString("Select an option to continue", x - 81, y - 30, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);

            render.drawSplitString("Welcome to Wynntils. You can see your statistics on the right or select some of the options above for more features",
                    155, x - 150, y + 30, 10, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

            render.drawRect(Textures.UIs.quest_book, x + 20, y - 90, 224, 253, 17, 18);
            render.drawRect(Textures.UIs.quest_book, x + 48, y - 90, 224, 253, 17, 18);
            render.drawRect(Textures.UIs.quest_book, x + 74, y - 90, 224, 253, 17, 18);
            render.drawRect(Textures.UIs.quest_book, x + 100, y - 90, 224, 253, 17, 18);
            render.drawRect(Textures.UIs.quest_book, x + 125, y - 90, 224, 253, 17, 18);

            ScreenRenderer.scale(2f);
            render.drawString("User Profile", (x - 158f) / 2.0f, (y - 74) / 2.0f, CommonColors.YELLOW, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
        }
        ScreenRenderer.endGL();
        renderHoveredText(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (selected > 0) {
            WynntilsSound.QUESTBOOK_PAGE.play();
            QuestBookPages.getPageBySlot(selected).open(false);
            return true;
        } else if (selected == -1) {
            goBack();
            return true;
        } else if (selected == -2) {
            goForward();
            return true;
        }
        return false;
    }
}
