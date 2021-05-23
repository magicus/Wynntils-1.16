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
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.core.managers.CompassManager;
import com.wynntils.modules.map.overlays.ui.MainWorldMapUI;
import com.wynntils.modules.questbook.configs.QuestBookConfig;
import com.wynntils.modules.questbook.enums.AnalysePosition;
import com.wynntils.modules.questbook.enums.DiscoveryType;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import com.wynntils.modules.questbook.instances.DiscoveryInfo;
import com.wynntils.modules.questbook.instances.IconContainer;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import com.wynntils.modules.questbook.managers.QuestManager;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.DiscoveryProfile;
import com.wynntils.webapi.profiles.TerritoryProfile;
import com.wynntils.webapi.request.Request;
import com.wynntils.webapi.request.RequestHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.MainWindow;
import com.wynntils.transition.GlStateManager;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DiscoveriesPage extends QuestBookPage {

    private List<DiscoveryInfo> discoverySearch;
    private DiscoveryInfo overDiscovery;

    private boolean territory = true;
    private boolean world = true;
    private boolean secret = true;
    private boolean undiscoveredTerritory = false;
    private boolean undiscoveredWorld = false;
    private boolean undiscoveredSecret = false;
    final static List<String> textLines =  Arrays.asList("Here you can see all", "the discoveries.", "", "You can use the filters below.");

    public DiscoveriesPage() {
        super("Discoveries", true, IconContainer.discoveriesIcon);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        int x = width / 2;
        int y = height / 2;
        int posX = (x - mouseX);
        int posY = (y - mouseY);
        hoveredText = new ArrayList<>();

        ScreenRenderer.beginGL(0, 0);
        {
            // Explanatory Text
            drawTextLines(textLines, x - 154, y - 30, 1);

            // Back button
            drawMenuButton(x, y, posX, posY);

            // World and Territory Progress icon
            if (posX >= 81 && posX <= 97 && posY >= 84 && posY <= 100) {
                render.drawRect(Textures.UIs.quest_book, x - 96, y - 100, 0, 271, 16, 16);

                hoveredText = new ArrayList<>(QuestManager.getDiscoveriesLore());
                if (!hoveredText.isEmpty()) {
                    hoveredText.remove(0);
                }
            } else {
                render.drawRect(Textures.UIs.quest_book, x - 96, y - 100, 0, 255, 16, 16);
            }

            // Secret Progress Icon
            if (posX >= 61 && posX <= 76 && posY >= 84 && posY <= 100) {
                render.drawRect(Textures.UIs.quest_book, x - 76, y - 100, 0, 303, 16, 16);

                hoveredText = new ArrayList<>(QuestManager.getSecretDiscoveriesLore());
                if (!hoveredText.isEmpty()) {
                    hoveredText.set(0, TextFormatting.AQUA + "Secret:");
                }
            } else {
                render.drawRect(Textures.UIs.quest_book, x - 76, y - 100, 0, 287, 16, 16);
            }

            // Next Page Button
            drawForwardAndBackButtons(x, y, posX, posY, currentPage, pages);

            // Discovered Territories Filter
            if (mouseX >= x - 130 && mouseX <= x - 100 && mouseY >= y + 15 && mouseY <= y + 45) {
                render.drawRect(selected_cube, x - 130, y + 15, x - 100, y + 45);
                hoveredText = Arrays.asList(TextFormatting.GREEN + "[>] Discovered Territory Discoveries", TextFormatting.GRAY + "Click to " + (territory ? "hide" : "show"));
                render.drawRect(Textures.UIs.quest_book, x - 127, y + 20, 305, 283, 24, 20);
            } else {
                render.drawRect((territory ? selected_cube_2 : unselected_cube), x - 130, y + 15, x - 100, y + 45);
                render.drawRect(Textures.UIs.quest_book, x - 127, y + 20, 305, 263, 24, 20);
            }

            // Undiscovered Territories Filter
            if (mouseX >= x - 130 && mouseX <= x - 100 && mouseY >= y + 50 && mouseY <= y + 80) {
                render.drawRect(selected_cube, x - 130, y + 50, x - 100, y + 80);
                hoveredText = Arrays.asList(TextFormatting.GREEN + "[>] Undiscovered Territory Discoveries", TextFormatting.GRAY + "Click to " + (undiscoveredTerritory ? "hide" : "show"));
                render.drawRect(Textures.UIs.quest_book, x - 126, y + 55, 283, 323, 21, 19);
            } else {
                render.drawRect((undiscoveredTerritory ? selected_cube_2 : unselected_cube), x - 130, y + 50, x - 100, y + 80);
                render.drawRect(Textures.UIs.quest_book, x - 121, y + 55, 288, 304, 11, 19);
            }

            // Discovered World Filter
            if (mouseX >= x - 95 && mouseX <= x - 65 && mouseY >= y + 15 && mouseY <= y + 45) {
                render.drawRect(selected_cube, x - 95, y + 15, x - 65, y + 45);
                hoveredText = Arrays.asList(TextFormatting.GREEN + "[>] Discovered World Discoveries", TextFormatting.GRAY + "Click to " + (world ? "hide" : "show"));
                render.drawRect(Textures.UIs.quest_book, x - 89, y + 20, 307, 241, 18, 20);
            } else {
                render.drawRect((world ? selected_cube_2 : unselected_cube), x - 95, y + 15, x - 65, y + 45);
                render.drawRect(Textures.UIs.quest_book, x - 89, y + 20, 307, 221, 18, 20);
            }

            // Undiscovered World Filter
            if (mouseX >= x - 95 && mouseX <= x - 65 && mouseY >= y + 50 && mouseY <= y + 80) {
                render.drawRect(selected_cube, x - 95, y + 50, x - 65, y + 80);
                hoveredText = Arrays.asList(TextFormatting.GREEN + "[>] Undiscovered World Discoveries", TextFormatting.GRAY + "Click to " + (undiscoveredWorld ? "hide" : "show"));
                render.drawRect(Textures.UIs.quest_book, x - 91, y + 56, 306, 322, 21, 17);
            } else {
                render.drawRect((undiscoveredWorld ? selected_cube_2 : unselected_cube), x - 95, y + 50, x - 65, y + 80);
                render.drawRect(Textures.UIs.quest_book, x - 89, y + 56, 308, 305, 17, 17);
            }

            // Discovered Secret Filter
            if (mouseX >= x - 60 && mouseX <= x - 30 && mouseY >= y + 15 && mouseY <= y + 45) {
                render.drawRect(selected_cube, x - 60, y + 15, x - 30, y + 45);
                hoveredText = Arrays.asList(TextFormatting.GREEN + "[>] Discovered Secret Discoveries", TextFormatting.GRAY + "Click to " + (secret ? "hide" : "show"));
                render.drawRect(Textures.UIs.quest_book, x - 55, y + 21, 284, 284, 20, 18);
            } else {
                render.drawRect((secret ? selected_cube_2 : unselected_cube), x - 60, y + 15, x - 30, y + 45);
                render.drawRect(Textures.UIs.quest_book, x - 55, y + 21, 284, 265, 20, 18);
            }

            // Undiscovered Secret Filter
            if (mouseX >= x - 60 && mouseX <= x - 30 && mouseY >= y + 50 && mouseY <= y + 80) {
                render.drawRect(selected_cube, x - 60, y + 50, x - 30, y + 80);
                hoveredText = Arrays.asList(TextFormatting.GREEN + "[>] Undiscovered Secret Discoveries", TextFormatting.GRAY + "Click to " + (undiscoveredSecret ? "hide" : "show"));
                render.drawRect(Textures.UIs.quest_book, x - 54, y + 57, 263, 324, 17, 16);
            } else {
                render.drawRect((undiscoveredSecret ? selected_cube_2 : unselected_cube), x - 60, y + 50, x - 30, y + 80);
                render.drawRect(Textures.UIs.quest_book, x - 54, y + 58, 263, 306, 17, 14);
            }

            // Page Text
            render.drawString(currentPage + " / " + pages, x + 80, y + 88, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);

            // Reload Data button
            if (posX >= -157 && posX <= -147 && posY >= 89 && posY <= 99) {
                hoveredText = Arrays.asList("Reload Button!", TextFormatting.GRAY + "Reloads all discovery data.");
                render.drawRect(Textures.UIs.quest_book, x + 147, y - 99, x + 158, y - 88, 218, 281, 240, 303);
            } else {
                render.drawRect(Textures.UIs.quest_book, x + 147, y - 99, x + 158, y - 88, 240, 281, 262, 303);
            }

            // Draw all Discoveries
            int currentY = 12;
            if (discoverySearch.size() > 0) {
                for (int i = ((currentPage - 1) * 13); i < 13 * currentPage; i++) {
                    if (discoverySearch.size() <= i) {
                        break;
                    }

                    DiscoveryInfo selected;
                    try {
                        selected = discoverySearch.get(i);
                    } catch (IndexOutOfBoundsException e) {
                        break;
                    }

                    List<String> lore = new ArrayList<>(selected.getLore());

                    if (posX >= -146 && posX <= -13 && posY >= 87 - currentY && posY <= 96 - currentY && !showAnimation) {
                        if (lastTick == 0 && !animationCompleted) {
                            lastTick = McIf.getSystemTime();
                        }

                        this.selected = i;

                        int animationTick;
                        if (!animationCompleted) {
                            animationTick = (int) (McIf.getSystemTime() - lastTick) / 2;
                            if (animationTick >= 133) {
                                animationCompleted = true;
                                animationTick = 133;
                            }
                        } else {
                            animationTick = 133;
                        }

                        render.drawRectF(background_1, x + 9, y - 96 + currentY, x + 13 + animationTick, y - 87 + currentY);
                        render.drawRectF(background_2, x + 9, y - 96 + currentY, x + 146, y - 87 + currentY);

                        overDiscovery = selected;
                        hoveredText = lore;
                        GlStateManager.disableLighting();
                    } else {
                        if (this.selected == i) {
                            animationCompleted = false;

                            if (!showAnimation) lastTick = 0;
                            overDiscovery = null;
                        }

                        render.drawRectF(background_2, x + 13, y - 96 + currentY, x + 146, y - 87 + currentY);
                    }

                    render.color(1, 1, 1, 1);

                    // Guild territory lore actions
                    if (selected.getGuildTerritoryProfile() != null) {
                        if (!lore.get(lore.size() - 1).contentEquals(""))
                            lore.add("");

                        lore.add(TextFormatting.GREEN + (TextFormatting.BOLD + "Left click to set compass beacon!"));
                        lore.add(TextFormatting.YELLOW + (TextFormatting.BOLD + "Right click to view on the map!"));
                    }

                    // Secret Discovery Actions
                    if (selected.getType() == DiscoveryType.SECRET) {
                        if (!lore.get(lore.size() - 1).contentEquals(""))
                            lore.add("");

                        if (QuestBookConfig.INSTANCE.spoilSecretDiscoveries.followsRule(selected.wasDiscovered())) {
                            lore.add(TextFormatting.GREEN + (TextFormatting.BOLD + "Left click to set compass beacon!"));
                            lore.add(TextFormatting.YELLOW + (TextFormatting.BOLD + "Right click to view on map!"));
                        }

                        lore.add(TextFormatting.GOLD + (TextFormatting.BOLD + "Middle click to open on the wiki!"));
                    }

                    // Removes blank space at the end of lores
                    if (lore.get(lore.size() - 1).contentEquals(""))
                        lore.remove(selected.getLore().size() - 1);


                    if (selected.wasDiscovered()) {
                        switch (selected.getType()) {
                            case TERRITORY:
                                render.drawRect(Textures.UIs.quest_book, x + 14, y - 95 + currentY, 264, 235, 11, 7);
                            break;
                            case WORLD:
                                render.drawRect(Textures.UIs.quest_book, x + 16, y - 95 + currentY, 276, 235, 7, 7);
                            break;
                            case SECRET:
                                render.drawRect(Textures.UIs.quest_book, x + 15, y - 95 + currentY, 255, 235, 8, 7);
                            break;
                        }
                    } else {
                        switch (selected.getType()) {
                            case TERRITORY:
                                render.drawRect(Textures.UIs.quest_book, x + 15, y - 95 + currentY, 241, 273, 8, 7);
                            break;
                            case WORLD:
                                render.drawRect(Textures.UIs.quest_book, x + 14, y - 95 + currentY, 250, 273, 11, 7);
                            break;
                            case SECRET:
                                render.drawRect(Textures.UIs.quest_book, x + 14, y - 95 + currentY, 229, 273, 11, 7);
                            break;
                        }
                    }

                    render.drawString(selected.getFriendlyName(), x + 26, y - 95 + currentY, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

                    currentY += 13;
                }
            } else {
                String textToDisplay;
                if (!(territory || world || secret || undiscoveredTerritory || undiscoveredWorld || undiscoveredSecret)) {
                    textToDisplay = "No filters enabled!\nTry refining your search.";
                } else if (QuestManager.getCurrentDiscoveries().size() == 0 || textField.getValue().equals("")) {
                    textToDisplay = "Loading Discoveries...\nIf nothing appears soon, try pressing the reload button.";
                } else {
                    textToDisplay = "No discoveries found!\nTry searching for something else.";
                }

                for (String line : textToDisplay.split("\n")) {
                    currentY += render.drawSplitString(line, 120, x + 26, y - 95 + currentY, 10, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE) * 10 + 2;
                }
            }
        }
        ScreenRenderer.endGL();
        renderHoveredText(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        MainWindow res = new MainWindow(McIf.mc());
        int posX = ((res.getGuiScaledWidth() / 2) - mouseX);
        int posY = ((res.getGuiScaledHeight() / 2) - mouseY);

        // Handle discovery click
        if (overDiscovery != null) {
            if (overDiscovery.getType() == DiscoveryType.SECRET) { // Secret discovery actions
                String name = McIf.getTextWithoutFormattingCodes(overDiscovery.getName());

                switch (mouseButton) {
                    case 0: // Left Click
                        if (QuestBookConfig.INSTANCE.spoilSecretDiscoveries.followsRule(overDiscovery.wasDiscovered())) {
                            locateSecretDiscovery(name, "compass");
                            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.BLOCK_ANVIL_PLACE, 1f));
                        }
                    break;
                    case 1: // Right Click
                        if (QuestBookConfig.INSTANCE.spoilSecretDiscoveries.followsRule(overDiscovery.wasDiscovered())) {
                            locateSecretDiscovery(name, "map");
                            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
                        }
                    break;
                    case 2: //Middle Click
                        String wikiUrl = "https://wynncraft.gamepedia.com/" + Utils.encodeForWikiTitle(name);
                        Utils.openUrl(wikiUrl);
                        McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
                    break;
                }
            } else if (overDiscovery.getGuildTerritoryProfile() != null) { // Guild territory actions
                TerritoryProfile guildTerritory = overDiscovery.getGuildTerritoryProfile();

                int x = (guildTerritory.getStartX() + guildTerritory.getEndX()) / 2;
                int z = (guildTerritory.getStartZ() + guildTerritory.getEndZ()) / 2;

                switch(mouseButton) {
                    case 0: // Left Click
                        CompassManager.setCompassLocation(new Location(x, 50, z));
                        McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.BLOCK_ANVIL_PLACE, 1f));
                    break;
                    case 1: // Right Click
                        Utils.setScreen(new MainWorldMapUI(x, z));
                        McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
                    break;
                    case 2: //Middle Click
                        Utils.setScreen(new MainWorldMapUI(x, z));
                        CompassManager.setCompassLocation(new Location(x, 50, z));
                        McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
                    break;
                }
            }
        }

        checkMenuButton(posX, posY);
        checkForwardAndBackButtons(posX, posY);

        if (posX >= 100 && posX <= 130 && posY >= -45 && posY <= -15) { // Discovered Territory
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            territory = !territory;
            updateSearch();
            return true;
        } else if (posX >= 100 && posX <= 130 && posY >= -80 && posY <= -50) { // Undiscovered Territory
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            undiscoveredTerritory = !undiscoveredTerritory;
            updateSearch();
            return true;
        } else if (posX >= 65 && posX <= 95 && posY >= -45 && posY <= -15) { // Discovered World
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            world = !world;
            updateSearch();
            return true;
        } else if (posX >= 65 && posX <= 95 && posY >= -80 && posY <= -50) { // Undiscovered World
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            undiscoveredWorld = !undiscoveredWorld;
            updateSearch();
            return true;
        } else if (posX >= 30 && posX <= 60 && posY >= -45 && posY <= -15) { // Discovered Secret
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            secret = !secret;
            updateSearch();
            return true;
        } else if (posX >= 30 && posX <= 60 && posY >= -80 && posY <= -50) { // Undiscovered Secret
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            undiscoveredSecret = !undiscoveredSecret;
            updateSearch();
            return true;
        } else if (posX >= -157 && posX <= -147 && posY >= 89 && posY <= 99) { // Update Button
            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f));
            QuestManager.updateAnalysis(EnumSet.of(AnalysePosition.DISCOVERIES, AnalysePosition.SECRET_DISCOVERIES), true, true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyPressed(char typedChar, int keyCode) throws IOException {
        overDiscovery = null;
        super.keyPressed(typedChar, keyCode);
    }

    @Override
    protected void searchUpdate(String currentText) {
        discoverySearch = new ArrayList<>(QuestManager.getCurrentDiscoveries());
        boolean isSearching = currentText != null && !currentText.isEmpty();

        // Apply Filters
        discoverySearch.removeIf(c -> {
            if (c.getType() == null) return true;
            switch (c.getType()) {
                case TERRITORY:
                    return !territory;
                case WORLD:
                    return !world;
                case SECRET:
                    return !secret;
                default:
                    return true;
            }
        });

        // Apply search terms
        if (isSearching) {
            discoverySearch.removeIf(c -> !doesSearchMatch(c.getName().toLowerCase(), currentText.toLowerCase()));
        }

        // Undiscovered data collection
        if (undiscoveredSecret || undiscoveredTerritory || undiscoveredWorld) {
            List<DiscoveryProfile> allDiscoveriesSearch = new ArrayList<>(WebManager.getDiscoveries());

            discoverySearch.addAll(allDiscoveriesSearch.stream()
                .filter(c -> {
                    // Shows/Hides discoveries if requirements met/not met
                    if (!QuestBookConfig.INSTANCE.showAllDiscoveries) {
                        if (c.getLevel() > PlayerInfo.get(CharacterData.class).getLevel()) {
                            return false;
                        }

                        boolean requirementsMet = true;
                        for (String requirement : c.getRequirements()) {
                            requirementsMet &= QuestManager.getCurrentDiscoveries().stream().anyMatch(foundDiscovery -> McIf.getTextWithoutFormattingCodes(foundDiscovery.getName()).equals(requirement));
                        }
                        if (!requirementsMet) {
                            return false;
                        }
                    }

                    // Apply search term
                    if (isSearching) {
                        if (!doesSearchMatch(c.getName().toLowerCase(), currentText.toLowerCase())) {
                            return false;
                        }
                    }

                    // Checks if already in list
                    if (QuestManager.getCurrentDiscoveries().stream().anyMatch(foundDiscovery -> {
                        boolean nameMatch = McIf.getTextWithoutFormattingCodes(foundDiscovery.getName()).equals(c.getName());
                        boolean levelMatch = foundDiscovery.getMinLevel() == c.getLevel();
                        boolean typeMatch = foundDiscovery.getType().name().toLowerCase(Locale.ROOT).equals(c.getType());

                        return nameMatch && levelMatch && typeMatch;
                    })) {
                        return false;
                    }

                    // Removes based on filters
                    if (c.getType() == null) return false;
                    switch (c.getType()) {
                        case "territory":
                            return undiscoveredTerritory;
                        case "world":
                            return undiscoveredWorld;
                        case "secret":
                            return undiscoveredSecret;
                        default:
                            return false;
                    }
                }).map(discoveryProfile -> {
                    DiscoveryType discoveryType = DiscoveryType.valueOf(discoveryProfile.getType().toUpperCase(Locale.ROOT));
                    return new DiscoveryInfo(discoveryProfile.getName(), discoveryType, discoveryProfile.getLevel(), false);
                }).collect(Collectors.toList())
            );
        }

        discoverySearch.sort(Comparator.comparingInt(DiscoveryInfo::getMinLevel));

        pages = discoverySearch.size() <= 13 ? 1 : (int) Math.ceil(discoverySearch.size() / 13d);
        currentPage = Math.min(currentPage, pages);
        refreshAccepts();
    }

    @Override
    public void open(boolean showAnimation) {
        super.open(showAnimation);

        if (QuestManager.getCurrentDiscoveries().isEmpty())
            QuestManager.updateAnalysis(EnumSet.of(AnalysePosition.DISCOVERIES, AnalysePosition.SECRET_DISCOVERIES), true, true);
    }

    @Override
    public List<String> getHoveredDescription() {
        return Arrays.asList(TextFormatting.GOLD + "[>] " + TextFormatting.BOLD + "Discoveries", TextFormatting.GRAY + "See and sort all", TextFormatting.GRAY + "of the discoveries.", "", TextFormatting.GREEN + "Left click to select");
    }

    /**
     * Uses the Wynncraft Wiki API to get the coordinates of a secret discovery from the Template:Infobox/Town
     */
    private void locateSecretDiscovery(String name, String action) {
        String queryUrl = WebManager.getApiUrl("WikiDiscoveryQuery");

        Request query = new Request(queryUrl + Utils.encodeForWikiTitle(name), "SecretWikiQuery");
        RequestHandler handler = new RequestHandler();

        handler.addAndDispatch(query.handleJsonObject(jsonOutput -> {
            if (jsonOutput.has("error")) { // Returns error if page does not exist
                McIf.sendMessage(new StringTextComponent(TextFormatting.RED + "Unable to find discovery coordinates. (Wiki page not found)"));
                return true;
            }

            String wikitext = jsonOutput.get("parse").getAsJsonObject().get("wikitext").getAsJsonObject().get("*").getAsString().replace(" ", "").replace("\n", "");

            String xlocation = wikitext.substring(wikitext.indexOf("xcoordinate="));
            String zlocation = wikitext.substring(wikitext.indexOf("zcoordinate="));

            int xend = Math.min(xlocation.indexOf("|"), xlocation.indexOf("}}"));
            int zend = Math.min(zlocation.indexOf("|"), zlocation.indexOf("}}"));

            int x;
            int z;

            try {
                x = Integer.parseInt(xlocation.substring(12, xend));
                z = Integer.parseInt(zlocation.substring(12, zend));
            } catch (NumberFormatException e) {
                McIf.sendMessage(new StringTextComponent(TextFormatting.RED + "Unable to find discovery coordinates. (Wiki template not located)"));
                return true;
            }

            if (x == 0 && z == 0) {
                McIf.sendMessage(new StringTextComponent(TextFormatting.RED + "Unable to find discovery coordinates. (Wiki coordinates not located)"));
                return true;
            }

            switch (action) {
                case "compass":
                    CompassManager.setCompassLocation(new Location(x, 50, z));
                break;
                case "map":
                    Utils.setScreen(new MainWorldMapUI(x, z));
                break;
                case "both":
                    Utils.setScreen(new MainWorldMapUI(x, z));
                    CompassManager.setCompassLocation(new Location(x, 50, z));
                break;
            }

            return true;
        }), true);
    }

}
