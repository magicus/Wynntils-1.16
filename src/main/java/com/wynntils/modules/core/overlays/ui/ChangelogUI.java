/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChangelogUI extends Screen {

    private static final CustomColor SCROLL_BACKGROUND = new CustomColor(191, 159, 110);
    private static final CustomColor SCROLL_ACTIVE = new CustomColor(248, 207, 145);

    ScreenRenderer renderer = new ScreenRenderer();

    Screen previousGui;

    List<String> changelogContent = new ArrayList<>();

    int scrollbarPosition = 0;
    int scrollbarSize;

    boolean major;

    public ChangelogUI(List<String> changelogContent, boolean major) {
        this(null, changelogContent, major);
    }

    public ChangelogUI(Screen previousGui, List<String> changelogContent, boolean major) {
        super(StringTextComponent.EMPTY);
        this.previousGui = previousGui;

        this.major = major;

        if (changelogContent == null || changelogContent.isEmpty()) {
            changelogContent = Collections.singletonList("<Error>");
        }

        for (String rawText : changelogContent)
            this.changelogContent.addAll(Arrays.asList(StringUtils.wrapText(rawText, 40)));

        if (this.changelogContent.size() <= 15) scrollbarSize = 118;
        else {
            scrollbarSize = (int)(118 * Math.pow(0.5d, this.changelogContent.size()/15f));
        }
    }

    public static void loadChangelogAndShow(boolean major, boolean forceLatest) {
        loadChangelogAndShow(null, major, forceLatest);
    }

    /**
     * Displays an intermediary loading screen (Currently just a changelog that says "Loading...")
     * whilst a web request is made in a separate thread.
     *
     * @param previousGui The gui to return to when exiting both the loading GUI and the changelog when it opens
     * @param major {@link WebManager#getChangelog(boolean, boolean)}'s first argument (Stable or CE?)
     * @param forceLatest {@link WebManager#getChangelog(boolean, boolean)}'s second argument (Latest or current changelog?)
     */
    public static void loadChangelogAndShow(Screen previousGui, boolean major, boolean forceLatest) {

        Screen loadingScreen = new ChangelogUI(previousGui, Collections.singletonList("Loading..."), major);
        McIf.mc().setScreen(loadingScreen);
        if (McIf.mc().screen != loadingScreen) {
            // Changed by an event handler
            return;
        }

        new Thread(() -> {
            if (McIf.mc().screen != loadingScreen) {
                return;
            }
            List<String> changelog = WebManager.getChangelog(major, forceLatest);
            if (McIf.mc().screen != loadingScreen) {
                return;
            }

            McIf.mc().submit(() -> {
                if (McIf.mc().screen != loadingScreen) {
                    return;
                }

                ChangelogUI gui = new ChangelogUI(previousGui, changelog, major);
                McIf.mc().setScreen(gui);
            });

        }, "wynntils-changelog").start();
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        ScreenRenderer.beginGL(0, 0);

        float middleX = width/2f; float middleY = height/2f;

        renderer.drawRect(Textures.UIs.changelog, (int)middleX - 150, (int)middleY - 100, 0, 0, 300, 200);
        renderer.drawString("Changelog " + (CoreDBConfig.INSTANCE.updateStream == UpdateStream.CUTTING_EDGE && !major ? "B" + Reference.BUILD_NUMBER : "v" + Reference.VERSION), middleX - 105, middleY - 83, CommonColors.RED, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

        // scrollbar
        renderer.drawRect(SCROLL_BACKGROUND, (int)middleX + 119, (int)middleY - 80, (int)middleX + 119 + 5, (int)middleY + 40);
        renderer.drawRect(SCROLL_ACTIVE, (int)middleX + 120, (int)middleY - 79 + scrollbarPosition, (int)middleX + 123, (int)middleY - 79 + scrollbarSize + scrollbarPosition);

        // text area
        ScreenRenderer.enableScissorTest((int) middleX - 110, (int) middleY - 71, 205, 155);
        float scrollPercent = scrollbarPosition/(118f - scrollbarSize);

        int textX = (int)middleX - 105;
        int baseY = (int)middleY - 70;

        float scrollPositionOffset = scrollbarSize == 118 ? 0 : (((changelogContent.size() / 15.0f) * 159) * scrollPercent);
        for (String changelogLine : changelogContent) {
            renderer.drawString(changelogLine.replace("%user%", McIf.mc().getSession().getUsername()), textX, baseY - scrollPositionOffset, CommonColors.BROWN, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

            baseY += 10;
        }

        ScreenRenderer.endGL();
    }

    public void updateScrollbarPosition(boolean down) {
        if (down) {
            if (scrollbarPosition + 4 > 118 - scrollbarSize)  {
                scrollbarPosition = 118 - scrollbarSize;
                return;
            }
            scrollbarPosition+=4;
        } else {
            if (scrollbarPosition - 4 < 0) {
                scrollbarPosition = 0;
                return;
            }
            scrollbarPosition-=4;
        }
    }

    @Override
    public void handleMouseInput() {
        int mDWheel = Mouse.getEventDWheel() * CoreDBConfig.INSTANCE.scrollDirection.getScrollDirection();

        if (mDWheel <= -1) {
            updateScrollbarPosition(true);
        } else if (mDWheel >= 1) updateScrollbarPosition(false);

    }

    @Override
    public boolean keyPressed(int charType, int keyCode, int j) {
        if (keyCode == 1) {  // ESC
            McIf.mc().setScreen(previousGui);
            if (McIf.mc().screen == null) McIf.mc().setIngameFocus();
            return true;
        }
        return false;
    }

}
