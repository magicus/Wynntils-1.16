/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.map.overlays.ui;

import com.wynntils.McIf;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.framework.ui.UI;
import com.wynntils.core.framework.ui.elements.UIEColorWheel;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.map.MapModule;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.modules.map.instances.WaypointProfile;
import com.wynntils.modules.map.instances.WaypointProfile.WaypointType;
import com.wynntils.modules.map.overlays.objects.MapWaypointIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.GuiLabel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;

public class WaypointCreationMenu extends UI {
    private GuiLabel nameFieldLabel;
    private TextFieldWidget nameField;
    private GuiLabel xCoordFieldLabel;
    private TextFieldWidget xCoordField;
    private GuiLabel yCoordFieldLabel;
    private TextFieldWidget yCoordField;
    private GuiLabel zCoordFieldLabel;
    private TextFieldWidget zCoordField;
    private GuiLabel coordinatesLabel;
    private Button defaultVisibilityButton;
    private Button alwaysVisibleButton;
    private Button hiddenButton;
    private UIEColorWheel colorWheel;
    private Button saveButton;
    private Button cancelButton;
    private Button waypointTypeNext;
    private Button waypointTypeBack;

    private boolean isUpdatingExisting;
    private WaypointProfile wp;
    private MapWaypointIcon wpIcon;
    private Screen previousGui;

    private int initialX;
    private int initialZ;

    private WaypointCreationMenuState state;

    public WaypointCreationMenu(Screen previousGui) {
        this.previousGui = previousGui;

        initialX = McIf.player().getPosition().getX();
        initialZ = McIf.player().getPosition().getZ();
    }

    // Create a waypoint at a position other than the current player's position
    public WaypointCreationMenu(Screen previousGui, int initialX, int initialZ) {
        this.previousGui = previousGui;

        this.initialX = initialX;
        this.initialZ = initialZ;
    }

    public WaypointCreationMenu(WaypointProfile wp, Screen previousGui) {
        this(previousGui);
        this.wp = wp;
        isUpdatingExisting = true;
    }

    @Override public void onInit() { }
    @Override public void onTick() { }
    @Override public void onWindowUpdate() {
        buttons.clear();

        nameField = new TextFieldWidget(0, McIf.mc().font, this.width/2 - 80, this.height/2 - 70, 160, 20);
        xCoordField = new TextFieldWidget(1, McIf.mc().font, this.width/2 - 65, this.height/2 - 30, 40, 20);
        zCoordField = new TextFieldWidget(2, McIf.mc().font, this.width/2 - 5, this.height/2 - 30, 40, 20);
        yCoordField = new TextFieldWidget(3, McIf.mc().font, this.width/2 + 55, this.height/2 - 30, 25, 20);
        buttons.add(waypointTypeNext = new Button(97, this.width/2 - 40, this.height/2 + 10, 18, 18, ">"));
        buttons.add(waypointTypeBack = new Button(98, this.width/2 - 80, this.height/2 + 10, 18, 18, "<"));

        int visibilityButtonWidth = 100;
        int visibilityButtonHeight = this.height/2 + 40;
        buttons.add(defaultVisibilityButton = new Button(99, this.width/2 - 3 * visibilityButtonWidth / 2 - 2, visibilityButtonHeight, visibilityButtonWidth, 18, "Default"));
        buttons.add(alwaysVisibleButton = new Button(100, this.width/2 - visibilityButtonWidth / 2, visibilityButtonHeight, visibilityButtonWidth, 18, "Always Visible"));
        buttons.add(hiddenButton = new Button(101, this.width/2 + visibilityButtonWidth / 2 + 2, visibilityButtonHeight, visibilityButtonWidth, 18, "Hidden"));

        int saveButtonHeight = this.height - 80 > visibilityButtonHeight + 20 ? this.height - 80 : this.height - 60;
        buttons.add(cancelButton = new Button(102, this.width/2 - 71, saveButtonHeight, 45, 18, "Cancel"));
        buttons.add(saveButton = new Button(103, this.width/2 + 25, saveButtonHeight, 45, 18, "Save"));
        saveButton.enabled = false;

        xCoordField.setValue(Integer.toString(initialX));
        zCoordField.setValue(Integer.toString(initialZ));
        yCoordField.setValue(Integer.toString(McIf.player().getPosition().getY()));

        nameFieldLabel = new GuiLabel(McIf.mc().font, 0, this.width/2 - 80, this.height/2 - 81, 40, 10, 0xFFFFFF);
        nameFieldLabel.addLine("Waypoint Name:");
        xCoordFieldLabel = new GuiLabel(McIf.mc().font, 1, this.width/2 - 75, this.height/2 - 24, 40, 10, 0xFFFFFF);
        xCoordFieldLabel.addLine("X");
        yCoordFieldLabel = new GuiLabel(McIf.mc().font, 2, this.width/2 + 45, this.height/2 - 24, 40, 10, 0xFFFFFF);
        yCoordFieldLabel.addLine("Y");
        zCoordFieldLabel = new GuiLabel(McIf.mc().font, 3, this.width/2 - 15, this.height/2 - 24, 40, 10, 0xFFFFFF);
        zCoordFieldLabel.addLine("Z");
        coordinatesLabel = new GuiLabel(McIf.mc().font, 3, this.width/2 - 80, this.height/2 - 41, 40, 10, 0xFFFFFF);
        coordinatesLabel.addLine("Coordinates:");

        boolean returning = state != null;  // true if reusing gui (i.e., returning from another gui)

        if (!returning) {
            UIElements.add(colorWheel = new UIEColorWheel(0.5f, 0.5f, 0, 9, 20, 20, true, this::setColor, this));
        }

        WaypointProfile wp = returning ? wpIcon.getWaypointProfile() : this.wp;
        CustomColor color = wp == null ? CommonColors.WHITE : wp.getColor();
        if (color == null) {
            color = CommonColors.WHITE;
        }

        if (wp == null) {
            setWpIcon(WaypointType.FLAG, 0, color);
        } else if (!returning) {
            nameField.setValue(wp.getName());
            xCoordField.setValue(Integer.toString((int) wp.getX()));
            yCoordField.setValue(Integer.toString((int) wp.getY()));
            zCoordField.setValue(Integer.toString((int) wp.getZ()));

            setWpIcon(wp.getType(), wp.getZoomNeeded(), color);
        }

        if (returning) {
            state.resetState(this);
        } else {
            state = new WaypointCreationMenuState();
            state.putState(this);
            colorWheel.setColor(color);
        }

        isAllValidInformation();

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onCloseWynntils() {
        Keyboard.enableRepeatEvents(false);
    }

    private void setWpIcon(WaypointType type, int zoomNeeded, CustomColor colour) {
        wpIcon = new MapWaypointIcon(new WaypointProfile("", 0, 0, 0, colour, type, zoomNeeded));

        final int disabledColour = 10526880;
        final int enabledColour = 0;

        defaultVisibilityButton.packedFGColour = disabledColour;
        alwaysVisibleButton.packedFGColour = disabledColour;
        hiddenButton.packedFGColour = disabledColour;

        switch (zoomNeeded) {
            case MapWaypointIcon.ANY_ZOOM:
                alwaysVisibleButton.packedFGColour = enabledColour;
                break;
            case MapWaypointIcon.HIDDEN_ZOOM:
                hiddenButton.packedFGColour = enabledColour;
                break;
            default:
                defaultVisibilityButton.packedFGColour = enabledColour;
        }
    }

    private int getZoomNeeded() {
        return wpIcon.getWaypointProfile().getZoomNeeded();
    }

    private void setZoomNeeded(int zoomNeeded) {
        setWpIcon(getWaypointType(), zoomNeeded, getColor());
    }

    private WaypointType getWaypointType() {
        return wpIcon.getWaypointProfile().getType();
    }

    private void setWaypointType(WaypointType waypointType) {
        setWpIcon(waypointType, getZoomNeeded(), getColor());
    }

    private CustomColor getColor() {
        CustomColor c = wpIcon.getWaypointProfile().getColor();
        return c == null ? CommonColors.WHITE : c;
    }

    private void setColor(CustomColor color) {
        setWpIcon(getWaypointType(), getZoomNeeded(), color == null ? CommonColors.WHITE : color);
    }

    @Override public void onRenderPreUIE(ScreenRenderer renderer) {}

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        super.render(matrix, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onRenderPostUIE(ScreenRenderer renderer) {
        if (nameField != null) nameField.drawTextBox();
        if (xCoordField != null) xCoordField.drawTextBox();
        if (yCoordField != null) yCoordField.drawTextBox();
        if (zCoordField != null) zCoordField.drawTextBox();

        nameFieldLabel.drawLabel(McIf.mc(), mouseX, mouseY);
        xCoordFieldLabel.drawLabel(McIf.mc(), mouseX, mouseY);
        yCoordFieldLabel.drawLabel(McIf.mc(), mouseX, mouseY);
        zCoordFieldLabel.drawLabel(McIf.mc(), mouseX, mouseY);
        coordinatesLabel.drawLabel(McIf.mc(), mouseX, mouseY);

        font.drawString("Icon:", this.width / 2.0f - 80, this.height / 2.0f, 0xFFFFFF, true);
        font.drawString("Colour:", this.width / 2.0f, this.height / 2.0f, 0xFFFFFF, true);

        float centreX = this.width / 2f - 60 + 9;
        float centreZ = this.height / 2f + 10 + 9;
        float multiplier = 9f / Math.max(wpIcon.getSizeX(), wpIcon.getSizeZ());
        wpIcon.renderAt(renderer, centreX, centreZ, multiplier, 1);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        state.putState(this);

        super.mouseClicked(mouseX, mouseY, mouseButton);

        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        xCoordField.mouseClicked(mouseX, mouseY, mouseButton);
        yCoordField.mouseClicked(mouseX, mouseY, mouseButton);
        zCoordField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j) {
        if (keyCode == GLFW.GLFW_KEY_TAB) {
            Utils.tab(
                Utils.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || Utils.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT) ? -1 : +1,
                nameField, xCoordField, zCoordField, yCoordField, colorWheel.textBox.textField
            );
            return true;
        }
        boolean result = super.keyPressed(typedChar, keyCode, j);
        nameField.keyPressed(typedChar, keyCode, j);
        xCoordField.keyPressed(typedChar, keyCode, j);
        yCoordField.keyPressed(typedChar, keyCode, j);
        zCoordField.keyPressed(typedChar, keyCode, j);
        isAllValidInformation();
        return result;
    }

    @Override
    protected void actionPerformed(Button button) {
        if (button == saveButton) {
            WaypointProfile newWp = new WaypointProfile(
                    nameField.getValue().trim(),
                    Integer.parseInt(xCoordField.getValue().trim()), Integer.parseInt(yCoordField.getValue().trim()), Integer.parseInt(zCoordField.getValue().trim()),
                    getColor(), getWaypointType(), getZoomNeeded()
            );
            if (isUpdatingExisting) {
                newWp.setGroup(wp.getGroup());
                MapConfig.Waypoints.INSTANCE.waypoints.set(MapConfig.Waypoints.INSTANCE.waypoints.indexOf(wp), newWp);
            } else {
                newWp.setGroup(newWp.getType());
                MapConfig.Waypoints.INSTANCE.waypoints.add(newWp);
            }
            MapConfig.Waypoints.INSTANCE.saveSettings(MapModule.getModule());
            Utils.setScreen(previousGui == null ? new MainWorldMapUI() : previousGui);
        } else if (button == cancelButton) {
            Utils.setScreen(previousGui == null ? new MainWorldMapUI() : previousGui);
        } else if (button == waypointTypeNext) {
            setWaypointType(WaypointType.values()[(getWaypointType().ordinal() + 1) % WaypointType.values().length]);
        } else if (button == waypointTypeBack) {
            setWaypointType(WaypointType.values()[(getWaypointType().ordinal() + (WaypointType.values().length - 1)) % WaypointType.values().length]);
        } else if (button == defaultVisibilityButton) {
            setZoomNeeded(0);
        } else if (button == alwaysVisibleButton) {
            setZoomNeeded(MapWaypointIcon.ANY_ZOOM);
        } else if (button == hiddenButton) {
            setZoomNeeded(MapWaypointIcon.HIDDEN_ZOOM);
        }
    }

    private void isAllValidInformation() {
        boolean xValid = StringUtils.isValidInteger(xCoordField.getValue().trim());
        boolean yValid = StringUtils.isValidInteger(yCoordField.getValue().trim());
        boolean zValid = StringUtils.isValidInteger(zCoordField.getValue().trim());
        xCoordField.setTextColor(xValid ? 0xFFFFFF : 0xFF6666);
        yCoordField.setTextColor(yValid ? 0xFFFFFF : 0xFF6666);
        zCoordField.setTextColor(zValid ? 0xFFFFFF : 0xFF6666);
        saveButton.enabled = xValid && yValid && zValid && !nameField.getValue().isEmpty() && getWaypointType() != null;
    }

    private static class WaypointCreationMenuState {
        String nameField;
        String xCoordField;
        String yCoordField;
        String zCoordField;

        void putState(WaypointCreationMenu menu) {
            nameField = menu.nameField.getValue();
            xCoordField = menu.xCoordField.getValue();
            yCoordField = menu.yCoordField.getValue();
            zCoordField = menu.zCoordField.getValue();
        }

        void resetState(WaypointCreationMenu menu) {
            menu.nameField.setValue(nameField);
            menu.xCoordField.setValue(xCoordField);
            menu.yCoordField.setValue(yCoordField);
            menu.zCoordField.setValue(zCoordField);
        }
    }
}
