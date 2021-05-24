/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.map.overlays.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.core.framework.enums.MouseButton;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.ui.elements.UIEColorWheel;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.map.MapModule;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.modules.map.instances.MapProfile;
import com.wynntils.modules.map.instances.PathWaypointProfile;
import com.wynntils.modules.map.instances.PathWaypointProfile.PathPoint;
import com.wynntils.modules.map.overlays.objects.MapPathWaypointIcon;
import com.wynntils.modules.map.overlays.objects.WorldMapIcon;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.GuiLabel;
import net.minecraft.client.gui.widget.TextFieldWidget;
import com.wynntils.transition.GlStateManager;
import net.minecraftforge.fml.client.config.CheckboxButton;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.function.Consumer;

public class PathWaypointCreationUI extends WorldMapUI {
    private Button saveButton;
    private Button cancelButton;
    private Button resetButton;
    private Button clearButton;

    private GuiLabel nameFieldLabel;
    private TextFieldWidget nameField;
    private CheckboxButton hiddenBox;
    private CheckboxButton circularBox;

    private GuiLabel helpText;
    private CheckboxButton addToFirst;
    private CheckboxButton showIconsBox;

    private UIEColorWheel colorWheel;

    private PathWaypointProfile originalProfile;
    private PathWaypointProfile profile;
    private MapPathWaypointIcon icon;
    private WorldMapIcon wmIcon;

    private boolean hidden;

    public PathWaypointCreationUI() {
        this(null);
    }

    public PathWaypointCreationUI(PathWaypointProfile profile) {
        super();
        removeWorkingProfile();

        this.allowMovement = false;

        this.profile = new PathWaypointProfile(originalProfile = profile);
        icon = new MapPathWaypointIcon(this.profile);
        wmIcon = new WorldMapIcon(icon);
        hidden = !this.profile.isEnabled;
        this.profile.isEnabled = true;

        if (originalProfile != null && originalProfile.size() > 0) {
            updateCenterPosition(originalProfile.getPosX(), originalProfile.getPosZ());
        }
    }

    @Override
    public void init() {
        buttons.clear();

        super.init();

        buttons.add(saveButton = new Button(1, 22, 23, 60, 18, "Save"));
        buttons.add(cancelButton = new Button(3, 22, 46, 60, 18, "Cancel"));
        buttons.add(resetButton = new Button(3, 22, 69, 60, 18, "Reset"));
        buttons.add(clearButton = new Button(4, 22, 92, 60, 18, "Clear"));

        boolean returning = nameField != null;
        String name = returning ? nameField.getValue() : profile.name;

        nameField = new TextFieldWidget(0, McIf.mc().font, this.width - 183, 23, 160, 20);
        nameField.setValue(name);
        nameFieldLabel = new GuiLabel(McIf.mc().font, 0, this.width - 218, 30, 40, 10, 0xFFFFFF);
        nameFieldLabel.addLine("Name");

        if (!returning) {
            colorWheel = new UIEColorWheel(1, 0, -168, 46, 20, 20, true, profile::setColor, this);
            colorWheel.setColor(profile.getColor());
        }

        buttons.add(hiddenBox = new CheckboxButton(5, this.width - 143,  72, "Hidden", hidden));  // TODO: check align
        buttons.add(circularBox = new CheckboxButton(6, this.width - 83, 72, "Circular", profile.isCircular));

        helpText = new GuiLabel(McIf.mc().font, 1, 22, this.height - 36, 120, 10, 0xFFFFFF);
        helpText.addLine("Shift + drag to pan");
        helpText.addLine("Right click to remove points");

        buttons.add(addToFirst = new CheckboxButton(7, this.width - 100, this.height - 47, "Add to start", false));
        buttons.add(showIconsBox = new CheckboxButton(8, this.width - 100, this.height - 34, "Show icons", true));

    }

    @Override
    protected void forEachIcon(Consumer<WorldMapIcon> c) {
        super.forEachIcon(c);
        if (wmIcon != null) c.accept(wmIcon);
    }

    @Override
    protected void createIcons() {
        super.createIcons();
        removeWorkingProfile();
    }

    private void removeWorkingProfile() {
        // Remove the icon for the current path being created / edited, as it is handled separately
        if (profile == null) return;

        icons.removeIf(c -> c.getInfo() instanceof MapPathWaypointIcon && ((MapPathWaypointIcon) c.getInfo()).getProfile() == originalProfile);
    }

    private void setCircular() {
        if (circularBox.isChecked() != profile.isCircular) {
            profile.isCircular = circularBox.isChecked();
            onChange();
        }
    }

    private void onChange() {
        icon.profileChanged();
        resetIcon(wmIcon);
    }

    private void removeClosePoints(int worldX, int worldZ) {
        float scaleFactor = getScaleFactor();
        if (profile.size() != 0) {
            // On right click remove all close points
            boolean changed = false;
            while (profile.size() != 0) {
                PathPoint last = profile.getPoint(profile.size() - 1);
                int dx = worldX - last.getX();
                int dz = worldZ - last.getZ();
                int dist_sq = dx * dx + dz * dz;
                if (scaleFactor * dist_sq <= 100) {
                    profile.removePoint(profile.size() - 1);
                    changed = true;
                } else {
                    break;
                }
            }

            while (profile.size() != 0) {
                PathPoint first = profile.getPoint(0);
                int dx = worldX - first.getX();
                int dz = worldZ - first.getZ();
                int dist_sq = dx * dx + dz * dz;
                if (scaleFactor * dist_sq <= 100) {
                    profile.removePoint(0);
                    changed = true;
                } else {
                    break;
                }
            }

            if (changed) onChange();
        }
    }

    private boolean handleMouse(int mouseX, int mouseY, int mouseButton) {
        if (isShiftKeyDown() || nameField.isFocused()) return false;

        for (Button button : buttons) {
            if (button.isMouseOver()) {
                return false;
            }
        }
        if (colorWheel.isHovering()) return false;
        if (mouseX >= nameField.x && mouseX < nameField.x + nameField.width && mouseY >= nameField.y && mouseY < nameField.y + nameField.height) return false;

        if (mouseButton == 0) {
            // Add points on left click
            MapProfile map = MapModule.getModule().getMainMap();
            int worldX = getMouseWorldX(mouseX, map);
            int worldZ = getMouseWorldZ(mouseY, map);

            if (profile.size() == 0) {
                profile.addPoint(new PathPoint(worldX, worldZ));
                onChange();
                return true;
            } else if (addToFirst.isChecked()) {
                PathPoint first = profile.getPoint(profile.size() - 1);
                int dx = worldX - first.getX();
                int dz = worldZ - first.getZ();
                int dist_sq = dx * dx + dz * dz;
                if (4 < dist_sq) {
                    profile.insertPoint(0, new PathPoint(worldX, worldZ));
                    onChange();
                    return true;
                }
            } else {
                PathPoint last = profile.getPoint(profile.size() - 1);
                int dx = worldX - last.getX();
                int dz = worldZ - last.getZ();
                int dist_sq = dx * dx + dz * dz;
                if (4 < dist_sq) {
                    profile.addPoint(new PathPoint(worldX, worldZ));
                    onChange();
                    return true;
                }
            }
        } else if (mouseButton == 1) {
            // Remove points close to right click
            MapProfile map = MapModule.getModule().getMainMap();
            int worldX = getMouseWorldX(mouseX, map);
            int worldZ = getMouseWorldZ(mouseY, map);

            removeClosePoints(worldX, worldZ);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (handleMouse((int) mouseX, (int) mouseY, mouseButton)) return true;

        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        MouseButton button = mouseButton == 0 ? MouseButton.LEFT : mouseButton == 1 ? MouseButton.RIGHT : mouseButton == 2 ? MouseButton.MIDDLE : MouseButton.UNKNOWN;
        colorWheel.click((int) mouseX, (int) mouseY, button, null);

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double d1, double d2) {
        if (handleMouse((int) mouseX, (int) mouseY, mouseButton)) return true;

        super.mouseDragged(mouseX, mouseY, mouseButton, d1, d2);
        return true;
    }

    @Override
    public void tick() {
        colorWheel.tick(0);
        super.tick();
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            Utils.tab(
                Utils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Utils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) ? -1 : +1,
                nameField, colorWheel.textBox.textField
            );
            return true;
        }
        super.keyPressed(typedChar, keyCode, j);
        colorWheel.keyPressed(typedChar, keyCode, j, null);
        nameField.keyPressed(typedChar, keyCode, j);
        return true;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        boolean isShiftKeyDown = isShiftKeyDown();

        updatePosition(mouseX, mouseY, !nameField.isFocused() && isShiftKeyDown && clicking[0] && !clicking[1]);
        if (isShiftKeyDown && clicking[1]) {
            updateCenterPositionWithPlayerPosition();
        }

        hidden = hiddenBox.isChecked();
        setCircular();

        ScreenRenderer.beginGL(0, 0);

        drawMap(mouseX, mouseY, partialTicks);

        if (showIconsBox.isChecked()) {
            drawIcons(mouseX, mouseY, partialTicks);
        } else {
            createMask();
            GlStateManager.enableBlend();
            wmIcon.render(mouseX, mouseY, partialTicks, getScaleFactor(), renderer);
            clearMask();
        }

        drawCoordinates(mouseX, mouseY, partialTicks);

        colorWheel.position.refresh();
        colorWheel.render(mouseX, mouseY);

        ScreenRenderer.endGL();


        if (nameField != null) nameField.drawTextBox();

        nameFieldLabel.drawLabel(McIf.mc(), mouseX, mouseY);
        helpText.drawLabel(McIf.mc(), mouseX, mouseY);

        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void actionPerformed(Button btn) {
        if (btn == saveButton) {
            profile.isEnabled = !hiddenBox.isChecked();
            setCircular();
            profile.name = nameField.getValue();
            if (originalProfile != null) {
                MapConfig.Waypoints.INSTANCE.pathWaypoints.set(MapConfig.Waypoints.INSTANCE.pathWaypoints.indexOf(originalProfile), profile);
            } else {
                MapConfig.Waypoints.INSTANCE.pathWaypoints.add(profile);
            }
            MapConfig.Waypoints.INSTANCE.saveSettings(MapModule.getModule());
            McIf.mc().setScreen(new PathWaypointOverwiewUI());
        } else if (btn == cancelButton) {
            McIf.mc().setScreen(new PathWaypointOverwiewUI());
        } else if (btn == resetButton) {
            McIf.mc().setScreen(new PathWaypointCreationUI(originalProfile));
        } else if (btn == clearButton) {
            int sz;
            while ((sz = profile.size()) != 0) profile.removePoint(sz - 1);
            onChange();
        } else if (btn == hiddenBox) {
            hidden = hiddenBox.isChecked();
        } else if (btn == circularBox) {
            setCircular();
        }
    }
}
