/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.map.overlays.ui;

import com.wynntils.McIf;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.map.MapModule;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.modules.map.instances.PathWaypointProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PathWaypointOverwiewUI extends Screen {
    private Button nextPageBtn;
    private Button previousPageBtn;
    private Button exitBtn;
    private Button newBtn;
    private List<Button> editButtons = new ArrayList<>();

    private ScreenRenderer renderer = new ScreenRenderer();
    private List<PathWaypointProfile> paths;
    private int page;
    private int pageHeight;

    @Override
    public void init() {
        super.init();
        paths = MapConfig.Waypoints.INSTANCE.pathWaypoints;

        pageHeight = (this.height - 100) / 25;
        setEditButtons();
        this.buttons.add(newBtn = new Button(-1, this.width/2 - 20, this.height - 45, 40, 20, "NEW"));
        this.buttons.add(nextPageBtn = new Button(0, this.width/2 + 24, this.height - 45, 20, 20, ">"));
        this.buttons.add(previousPageBtn = new Button(1, this.width/2 - 44, this.height - 45, 20, 20, "<"));
        this.buttons.add(exitBtn = new Button(2, this.width - 40, 20, 20, 20, TextFormatting.RED + "X"));
        checkAvailablePages();
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.render(matrix, mouseX, mouseY, partialTicks);
        font.drawString(TextFormatting.BOLD + "Icon", this.width/2 - 185, 39, 0xFFFFFF);
        font.drawString(TextFormatting.BOLD + "Name", this.width/2 - 150, 39, 0xFFFFFF);
        drawCenteredString(font, TextFormatting.BOLD + "X", this.width/2 + 20, 39, 0xFFFFFF);
        drawCenteredString(font, TextFormatting.BOLD + "Z", this.width/2 + 60, 39, 0xFFFFFF);
        drawRect(this.width/2 - 185, 48, this.width/2 + 170, 49, 0xFFFFFFFF);

        ScreenRenderer.beginGL(0, 0);
        for (int i = 0; i < Math.min(pageHeight, paths.size() - pageHeight * page); i++) {
            PathWaypointProfile wp = paths.get(page * pageHeight + i);

            int colour = 0xFFFFFF;
            boolean hidden = !wp.isEnabled;
            if (hidden) {
                colour = 0x636363;
            }

            renderer.drawRect(CommonColors.BLACK, this.width / 2 - 180, 51 + 25 * i, this.width / 2 - 162, 69 + 25 * i);
            renderer.drawRect(wp.getColor(), this.width / 2 - 179, 52 + 25 * i, this.width / 2 - 163, 68 + 25 * i);

            font.drawString(wp.name, this.width/2 - 150, 56 + 25 * i, colour);
            drawCenteredString(font, Integer.toString(wp.getPosX()), this.width/2 + 20, 56 + 25 * i, colour);
            drawCenteredString(font, Integer.toString(wp.getPosZ()), this.width/2 + 60, 56 + 25 * i, colour);

            if (hidden) {
                drawHorizontalLine(this.width / 2 - 155, this.width / 2 + 75, 60 + 25 * i - 1, colour | 0xFF000000);
            }
        }
        ScreenRenderer.endGL();
    }

    @Override
    protected void actionPerformed(Button b) {
        if (b == nextPageBtn) {
            page++;
            checkAvailablePages();
            setEditButtons();
        } else if (b == previousPageBtn) {
            page--;
            checkAvailablePages();
            setEditButtons();
        } else if (b == exitBtn) {
            Utils.setScreen(new MainWorldMapUI());
        } else if (b.id % 10 == 3) {
            McIf.mc().setScreen(new PathWaypointCreationUI(paths.get(b.id / 10 + page * pageHeight)));
        } else if (b.id %10 == 5) {
            MapConfig.Waypoints.INSTANCE.pathWaypoints.remove(paths.get(b.id / 10 + page * pageHeight));
            MapConfig.Waypoints.INSTANCE.saveSettings(MapModule.getModule());
            McIf.mc().setScreen(new PathWaypointOverwiewUI());
        } else if (b == newBtn) {
            McIf.mc().setScreen(new PathWaypointCreationUI());
        }
    }

    private void checkAvailablePages() {
        nextPageBtn.enabled = paths.size() - page * pageHeight > pageHeight;
        previousPageBtn.enabled = page > 0;
    }

    private void setEditButtons() {
        this.buttons.removeAll(editButtons);
        editButtons.clear();
        for (int i = 0; i < Math.min(pageHeight, paths.size() - pageHeight * page); i++) {
            editButtons.add(new Button(3 + 10 * i, this.width/2 + 85, 50 + 25 * i, 40, 20,"Edit..."));
            editButtons.add(new Button(5 + 10 * i, this.width/2 + 130, 50 + 25 * i, 40, 20, "Delete"));
        }
        this.buttons.addAll(editButtons);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mDWheel = Mouse.getEventDWheel() * CoreDBConfig.INSTANCE.scrollDirection.getScrollDirection();
        if (mDWheel < 0 && nextPageBtn.enabled) {
            ++page;
            checkAvailablePages();
            setEditButtons();
        } else if (mDWheel > 0 && previousPageBtn.enabled) {
            --page;
            checkAvailablePages();
            setEditButtons();
        }
    }

}
