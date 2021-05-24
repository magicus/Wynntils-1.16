/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.chat.overlays.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.McIf;
import com.wynntils.modules.chat.instances.ChatTab;
import com.wynntils.modules.chat.managers.TabManager;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import net.minecraft.client.gui.widget.*;
import static net.minecraft.util.text.TextFormatting.*;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;


public class TabGUI extends Screen {

    int id;
    ChatTab tab;

    public TabGUI(int id) {
        this.id = id;

        if (id != -2)
            tab = TabManager.getTabById(id);
    }
    List<CheckboxButton> simpleRegexSettings = new ArrayList<>();

    // ui things
    Button saveButton;
    Button deleteButton;
    Button advancedButton;
    Button closeButton;
    CheckboxButton lowPriority;
    CheckboxButton allRegex;
    CheckboxButton localRegex;
    CheckboxButton guildRegex;
    CheckboxButton partyRegex;
    CheckboxButton shoutsRegex;
    CheckboxButton pmRegex;
    TextFieldWidget nameTextField;
    TextFieldWidget regexTextField;
    TextFieldWidget autoCommandField;
    TextFieldWidget orderNbField;

    // labels
    GuiLabel nameLabel;
    GuiLabel regexLabel;
    GuiLabel autoCommand;
    GuiLabel orderNb;
    GuiLabel simpleSettings;

    @Override
    public void init() {
        labelList.clear();
        simpleRegexSettings.clear();

        int x = width / 2; int y = height / 2;

        // General
        buttons.add(saveButton = new Button(0, x - 90, y + 40, 40, 20, GREEN + "Save"));
        buttons.add(deleteButton = new Button(1, x - 45, y + 40, 40, 20, DARK_RED + "Delete"));
        buttons.add(closeButton = new Button(2, x + 50, y + 40, 40, 20, WHITE + "Close"));
        buttons.add(advancedButton = new Button(4, x - 65, y - 60, 130, 20, "Show Advanced Settings"));

        deleteButton.enabled = (id != -2) && TabManager.getAvailableTabs().size() > 1;

        nameTextField = new TextFieldWidget(3, McIf.mc().font, x - 110, y - 90, 80, 20);
        nameTextField.setVisible(true);
        nameTextField.setEnabled(true);
        nameTextField.setEnableBackgroundDrawing(true);
        nameTextField.setMaxLength(10);

        autoCommandField = new TextFieldWidget(3, McIf.mc().font, x - 12, y - 90, 80, 20);
        autoCommandField.setVisible(true);
        autoCommandField.setEnabled(true);
        autoCommandField.setEnableBackgroundDrawing(true);
        autoCommandField.setMaxLength(10);

        orderNbField = new TextFieldWidget(3, McIf.mc().font, x + 85, y - 90, 25, 20);
        orderNbField.setVisible(true);
        orderNbField.setEnabled(true);
        orderNbField.setEnableBackgroundDrawing(true);
        orderNbField.setMaxLength(2);

        buttons.add(lowPriority = new CheckboxButton(3, x - 100, y + 22, "Low Priority", true));

        // Simple
        labelList.add(simpleSettings = new GuiLabel(McIf.mc().font, 4, x - 100, y - 35, 10, 10, 0xFFFFFF));
        simpleSettings.addLine("Message types " + RED + "*");

        simpleRegexSettings.add(allRegex = new CheckboxButton(10, x - 100, y - 25, "All", false));
        simpleRegexSettings.add(localRegex = new CheckboxButton(11, x - 50, y - 25, "Local", false));
        simpleRegexSettings.add(guildRegex = new CheckboxButton(12, x, y - 25, "Guild", false));
        simpleRegexSettings.add(partyRegex = new CheckboxButton(13, x + 50, y - 25, "Party", false));
        simpleRegexSettings.add(shoutsRegex = new CheckboxButton(14, x - 100, y - 10, "Shouts", false));
        simpleRegexSettings.add(pmRegex = new CheckboxButton(15, x - 50, y - 10, "PMs", false));
        buttons.addAll(simpleRegexSettings);
        applyRegexSettings();
        // Advanced
        regexTextField = new TextFieldWidget(3, McIf.mc().font, x - 100, y - 20, 200, 20);
        regexTextField.setVisible(false);
        regexTextField.setEnabled(true);
        regexTextField.setEnableBackgroundDrawing(true);
        regexTextField.setMaxLength(400);

        if (tab != null) {
            nameTextField.setValue(tab.getName());
            regexTextField.setValue(tab.getRegex().replace("§", "&"));
            lowPriority.setIsChecked(tab.isLowPriority());
            autoCommandField.setValue(tab.getAutoCommand());
            orderNbField.setValue(Integer.toString(tab.getOrderNb()));
            checkIfRegexIsValid();
        }

        labelList.add(nameLabel = new GuiLabel(McIf.mc().font, 0, x - 110, y - 105, 10, 10, 0xFFFFFF));
        nameLabel.addLine("Name " + RED + "*");
        labelList.add(regexLabel = new GuiLabel(McIf.mc().font, 1, x - 100, y - 35, 10, 10, 0xFFFFFF));
        regexLabel.addLine("Regex " + RED + "*");
        regexLabel.visible = false;
        labelList.add(autoCommand = new GuiLabel(McIf.mc().font, 2, x - 12, y - 105, 10, 10, 0xFFFFFF));
        autoCommand.addLine("Auto Command");
        labelList.add(orderNb = new GuiLabel(McIf.mc().font, 3, x + 85, y - 105, 10, 10, 0xFFFFFF));
        orderNb.addLine("Order #");

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onClose() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(Button button) throws IOException {
        super.actionPerformed(button);

        if (button == closeButton) McIf.mc().setScreen(new ChatGUI());
        else if (button == saveButton) {
            if (id == -2) {
                TabManager.registerNewTab(new ChatTab(nameTextField.getValue(), regexTextField.getValue(), regexSettingsCreator(), autoCommandField.getValue(), lowPriority.isChecked(), orderNbField.getValue().matches("[0-9]+") ? Integer.parseInt(orderNbField.getValue()) : 0));
            } else {
                TabManager.updateTab(id, nameTextField.getValue(), regexTextField.getValue(), regexSettingsCreator(), autoCommandField.getValue(), lowPriority.isChecked(), orderNbField.getValue().matches("[0-9]+") ? Integer.parseInt(orderNbField.getValue()) : 0);
            }
            McIf.mc().setScreen(new ChatGUI());
        } else if (button == deleteButton) {
            McIf.mc().setScreen(new ConfirmScreen((result, cc) -> {
                if (result) {
                    int c = TabManager.deleteTab(id);
                    if (ChatOverlay.getChat().getCurrentTabId() == id) ChatOverlay.getChat().setCurrentTab(c);
                    McIf.mc().setScreen(new ChatGUI());
                } else {
                    McIf.mc().setScreen(this);
                }
            }, WHITE + (BOLD + "Do you really want to delete this chat tab?"), RED + "This action is irreversible!", 0));
        } else if (button == advancedButton) {
            boolean simple;
            if (button.getMessage().equals("Show Advanced Settings")) {
                button.setMessage("Hide Advanced Settings");
                simple = false;
            } else {
                button.setMessage("Show Advanced Settings");
                simple = true;
            }
            regexTextField.setVisible(!simple);
            regexLabel.visible = !simple;
            simpleSettings.visible = simple;
            simpleRegexSettings.forEach(b -> b.visible = simple);
        } else if (button == allRegex) {
            simpleRegexSettings.forEach(b -> b.setIsChecked(((CheckboxButton) button).isChecked()));
        }
        if (button.id >= 10 && button.id <= 16) {
            regexTextField.setValue(regexCreator());
            checkIfRegexIsValid();
        }
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        super.render(matrix, mouseX, mouseY, partialTicks);

        if (nameTextField != null) nameTextField.drawTextBox();
        if (regexTextField != null) regexTextField.drawTextBox();
        if (autoCommandField != null) autoCommandField.drawTextBox();
        if (orderNbField != null) orderNbField.drawTextBox();

        if (mouseX >= nameTextField.x && mouseX < nameTextField.x + nameTextField.width && mouseY >= nameTextField.y && mouseY < nameTextField.y + nameTextField.height)
            drawHoveringText(Arrays.asList(GREEN + (BOLD + "Name"), GRAY + "This is how your tab", GRAY + "will be named", "", RED + "Required"), mouseX, mouseY);

        if (regexTextField.getVisible() && mouseX >= regexTextField.x && mouseX < regexTextField.x + regexTextField.width && mouseY >= regexTextField.y && mouseY < regexTextField.y + regexTextField.height)
            drawHoveringText(Arrays.asList(GREEN + (BOLD + "RegEx"), GRAY + "This will parse the chat", " ", GREEN + "You can learn RegEx at", GOLD + "https://regexr.com/", "", RED + "Required"), mouseX, mouseY);

        if (mouseX >= autoCommandField.x && mouseX < autoCommandField.x + autoCommandField.width && mouseY >= autoCommandField.y && mouseY < autoCommandField.y + autoCommandField.height)
            drawHoveringText(Arrays.asList(GREEN + (BOLD + "Auto Command"), GRAY + "This will automatically", GRAY + "put this command before", GRAY + "any message.", "", RED + "Optional"), mouseX, mouseY);

        if (mouseX >= orderNbField.x && mouseX < orderNbField.x + orderNbField.width && mouseY >= orderNbField.y && mouseY < orderNbField.y + orderNbField.height)
            drawHoveringText(Arrays.asList(GREEN + (BOLD + "Order number"), GRAY + "This determines the", GRAY + "arrangement of the", GRAY + "chat tabs.", DARK_GRAY + "(lowest to highest)", RED + "Optional"), mouseX, mouseY);

        if (mouseX >= lowPriority.x && mouseX < lowPriority.x + lowPriority.width && mouseY >= lowPriority.y && mouseY < lowPriority.y + lowPriority.height)
            drawHoveringText(Arrays.asList(GREEN + (BOLD + "Low priority"), GRAY + "If selected, messages", GRAY + "will attempt to match", GRAY + "with other tabs first.", "", GRAY + "This will also duplicate", GRAY + "messages across other", GRAY + "low priority tabs.", RED + "Optional"), mouseX, mouseY);

        if (advancedButton.getMessage().equals("Show Advanced Settings")) {
            if (mouseX >= allRegex.x && mouseX < allRegex.x + allRegex.width && mouseY >= allRegex.y && mouseY < allRegex.y + allRegex.height) {
                drawHoveringText(Arrays.asList(GREEN + (BOLD + "Message Type: All"), GRAY + "This will send all", GRAY + "messages, except those", GRAY + "deselected to this tab."), mouseX, mouseY);
            } else if (mouseX >= localRegex.x && mouseX < localRegex.x + localRegex.width && mouseY >= localRegex.y && mouseY < localRegex.y + localRegex.height) {
                drawHoveringText(Arrays.asList(GREEN + (BOLD + "Message Type: Local"), GRAY + "This will send all", GRAY + "messages send by nearby", GRAY + "players to this tab."), mouseX, mouseY);
            } else if (mouseX >= guildRegex.x && mouseX < guildRegex.x + guildRegex.width && mouseY >= guildRegex.y && mouseY < guildRegex.y + guildRegex.height) {
                drawHoveringText(Arrays.asList(GREEN + (BOLD + "Message Type: Guild"), GRAY + "This will send all", GRAY + "messages send by guild", GRAY + "members to this tab."), mouseX, mouseY);
            } else if (mouseX >= partyRegex.x && mouseX < partyRegex.x + partyRegex.width && mouseY >= partyRegex.y && mouseY < partyRegex.y + partyRegex.height) {
                drawHoveringText(Arrays.asList(GREEN + (BOLD + "Message Type: Party"), GRAY + "This will send all", GRAY + "messages send by party", GRAY + "members to this tab."), mouseX, mouseY);
            } else if (mouseX >= shoutsRegex.x && mouseX < shoutsRegex.x + shoutsRegex.width && mouseY >= shoutsRegex.y && mouseY < shoutsRegex.y + shoutsRegex.height) {
                drawHoveringText(Arrays.asList(GREEN + (BOLD + "Message Type: Shouts"), GRAY + "This will send all", GRAY + "shouts messages", GRAY + "to this tab."), mouseX, mouseY);
            } else if (mouseX >= pmRegex.x && mouseX < pmRegex.x + pmRegex.width && mouseY >= pmRegex.y && mouseY < pmRegex.y + pmRegex.height) {
                drawHoveringText(Arrays.asList(GREEN + (BOLD + "Message Type: PMs"), GRAY + "This will send all", GRAY + "private messages", GRAY + "to this tab."), mouseX, mouseY);
            }
        }

        if (saveButton.enabled && mouseX >= saveButton.x && mouseX < saveButton.x + saveButton.width && mouseY >= saveButton.y && mouseY < saveButton.y + saveButton.height)
            drawHoveringText(Arrays.asList(GREEN + (BOLD + "Save"), GRAY + "Click here to save", GRAY + "this chat tab."), mouseX, mouseY);

        if (deleteButton.enabled && mouseX >= deleteButton.x && mouseX < deleteButton.x + deleteButton.width && mouseY >= deleteButton.y && mouseY < deleteButton.y + deleteButton.height)
            drawHoveringText(Arrays.asList(DARK_RED + (BOLD + "Delete"), GRAY + "Click here to delete", GRAY + "this chat tab.", "", RED + "Irreversible action"), mouseX, mouseY);

        saveButton.enabled = !regexTextField.getValue().isEmpty() && regexValid && !nameTextField.getValue().isEmpty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean result = super.mouseClicked(mouseX, mouseY, mouseButton);

        regexTextField.mouseClicked(mouseX, mouseY, mouseButton);
        nameTextField.mouseClicked(mouseX, mouseY, mouseButton);
        autoCommandField.mouseClicked(mouseX, mouseY, mouseButton);
        orderNbField.mouseClicked(mouseX, mouseY, mouseButton);
        // Actually we should track the return of all componentts
        return result;
    }

    @Override
    public boolean keyPressed(int typedChar, int keyCode, int j) {
        boolean result = super.keyPressed(typedChar, keyCode, j);

        nameTextField.keyPressed(typedChar, keyCode, j);
        autoCommandField.keyPressed(typedChar, keyCode, j);
        orderNbField.keyPressed(typedChar, keyCode, j);
        if (regexTextField.keyPressed(typedChar, keyCode, j)) checkIfRegexIsValid();
        return result;
    }

    boolean regexValid = false;

    private void checkIfRegexIsValid() {
        try {
            Pattern.compile(regexTextField.getValue());
            regexTextField.setTextColor(0x55FF55);
            regexValid = true;
            return;
        } catch (Exception ignored) { }

        regexTextField.setTextColor(0xFF5555);
        regexValid = false;
    }

    private Map<String, Boolean> regexSettingsCreator() {
        if (advancedButton.getMessage().equals("Hide Advanced Settings")) return null;

        Map<String, Boolean> r = new HashMap<>();
        simpleRegexSettings.forEach(b-> r.put(b.getMessage(), b.isChecked()));
        return r;
    }

    private void applyRegexSettings() {
        if (tab == null || tab.getRegexSettings() == null) return;
        tab.getRegexSettings().forEach((k, v) -> {
            for (CheckboxButton cb: simpleRegexSettings) {
                if (cb.getMessage().equals(k)) {
                    cb.setIsChecked(v);
                }
            }
        });
    }

    private String regexCreator() {
        if (advancedButton.getMessage().equals("Hide Advanced Settings")) return "";

        Map<String, Boolean> regexSettings = regexSettingsCreator();
        List<String> result = new ArrayList<>();
        boolean allIsPresent = regexSettings.get("All");

        regexSettings.forEach((k, v) -> {
            if ((v && !allIsPresent) || (allIsPresent && !v)) {
                switch (k) {
                    case "Local":
                        result.add("^&7\\[\\d+\\*?\\/\\w{2}");
                        break;
                    case "Guild":
                        result.add(TabManager.DEFAULT_GUILD_REGEX);
                        break;
                    case "Party":
                        result.add(TabManager.DEFAULT_PARTY_REGEX);
                        break;
                    case "Shouts":
                        result.add("(^&3.*shouts:)");
                        break;
                    case "PMs":
                        result.add("(&7\\[.*\u27A4.*&7\\])");
                        break;
                }
            }
        });

        if (allIsPresent && result.size() > 0) {
            return String.format("^((?!%s).)*$", String.join("|", result));
        } else if (allIsPresent) {
            return ".*";
        } else {
            return String.join("|", result);
        }
    }
}
