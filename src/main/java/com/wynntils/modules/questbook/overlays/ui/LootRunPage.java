package com.wynntils.modules.questbook.overlays.ui;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.chat.overlays.ChatOverlay;
import com.wynntils.modules.map.MapModule;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.modules.map.instances.LootRunPath;
import com.wynntils.modules.map.instances.MapProfile;
import com.wynntils.modules.map.managers.LootRunManager;
import com.wynntils.modules.map.overlays.objects.MapIcon;
import com.wynntils.modules.map.overlays.ui.MainWorldMapUI;
import com.wynntils.modules.questbook.configs.QuestBookConfig;
import com.wynntils.modules.questbook.instances.IconContainer;
import com.wynntils.modules.questbook.instances.QuestBookPage;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.wynntils.transition.GlStateManager.*;

public class LootRunPage extends QuestBookPage {

    private final int MESSAGE_ID = 103002;

    private List<String> names;
    private String selectedName;
    private final static List<String> textLines = Arrays.asList("Here you can see all lootruns", "you have downloaded. You can", "also search for a specific", "quest just by typing its name.", "You can go to the next page", "by clicking on the two buttons", "or by scrolling your mouse.", "", "To add lootruns, access the", "folder for lootruns by", "running /lootrun folder");

    public LootRunPage() {
        super("Your Lootruns", true, IconContainer.lootrunIcon);
    }

    @Override
    public List<String> getHoveredDescription() {
        return Arrays.asList(TextFormatting.GOLD + "[>] " + TextFormatting.BOLD + "Lootruns", TextFormatting.GRAY + "See all lootruns", TextFormatting.GRAY + "you have", TextFormatting.GRAY + "saved in the game.", "", TextFormatting.GREEN + "Left click to select");
    }

    @Override
    public void init() {
        super.init();
        initBasicSearch();

        names = LootRunManager.getStoredLootruns();
        Collections.sort(names);
    }

    private void initBasicSearch() {
        textField.setMaxLength(50);
        initDefaultSearchBar();
    }

    private void initDefaultSearchBar() {
        textField.x = width / 2 + 32;
        textField.y = height / 2 - 97;
        textField.width = 113;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        int x = width / 2;
        int y = height / 2;
        int posX = (x - mouseX);
        int posY = (y - mouseY);

        int mapX = x - 154;
        int mapY = y + 23;
        int mapWidth = 145;
        int mapHeight = 58;
        hoveredText = new ArrayList<>();

        float scale = QuestBookConfig.INSTANCE.scaleOfLootrun;

        ScreenRenderer.beginGL(0, 0);
        {
            if (LootRunManager.getActivePathName() != null) {
                //render info
                ScreenRenderer.scale(1.2f);
                render.drawString(LootRunManager.getActivePathName(), x/1.2f - 154/1.2f, y/1.2f - 35/1.2f, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                ScreenRenderer.resetScale();

                LootRunPath path = LootRunManager.getActivePath();
                Location start = path.getPoints().get(0);
                render.drawString("Chests: " + path.getChests().size(), x - 154, y - 20, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                render.drawString("Notes: " + path.getNotes().size(), x - 154, y - 10, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                render.drawString("Start point: " + start, x - 154, y, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                render.drawString("End point: " + path.getLastPoint(), x - 154, y + 10, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

                //render map of starting point
                MapProfile map = MapModule.getModule().getMainMap();

                if (map != null) {
                    float minX = map.getTextureXPosition(start.x) - scale * (mapWidth / 2f);  // <--- min texture x point
                    float minZ = map.getTextureZPosition(start.z) - scale * (mapHeight / 2f);  // <--- min texture z point

                    float maxX = map.getTextureXPosition(start.x) + scale * (mapWidth / 2f);  // <--- max texture x point
                    float maxZ = map.getTextureZPosition(start.z) + scale * (mapHeight / 2f);  // <--- max texture z point

                    minX /= (float) map.getImageWidth();
                    maxX /= (float) map.getImageWidth();

                    minZ /= (float) map.getImageHeight();
                    maxZ /= (float) map.getImageHeight();

                    try {
                        enableAlpha();
                        enableTexture2D();

                        //boundary around map
                        int boundarySize = 3;
                        render.drawRect(Textures.Map.paper_map_textures, mapX - boundarySize, mapY - boundarySize, mapX + mapWidth + boundarySize, mapY + mapHeight + boundarySize, 0, 0, 217, 217);
                        ScreenRenderer.enableScissorTest(mapX, mapY, mapWidth, mapHeight);

                        map.bind();
                        color(1.0f, 1.0f, 1.0f, 1.0f);

                        glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                        enableBlend();
                        enableTexture2D();
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder bufferbuilder = tessellator.getBuilder();
                        {
                            bufferbuilder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_TEX);

                            bufferbuilder.vertex(mapX, mapY + mapHeight, 0).uv(minX, maxZ).endVertex();
                            bufferbuilder.vertex(mapX + mapWidth, mapY + mapHeight, 0).uv(maxX, maxZ).endVertex();
                            bufferbuilder.vertex(mapX + mapWidth, mapY, 0).uv(maxX, minZ).endVertex();
                            bufferbuilder.vertex(mapX, mapY, 0).uv(minX, minZ).endVertex();

                            tessellator.end();
                        }

                        //render the line on the map
                        if (MapConfig.LootRun.INSTANCE.displayLootrunOnMap) {
                            List<MapIcon> icons = LootRunManager.getMapPathWaypoints();
                            for (MapIcon mapIcon : icons) {
                                mapIcon.renderAt(render, (float) (mapX + mapWidth / 2f - (start.getX() - mapIcon.getPosX())), (float) (mapY + mapHeight / 2f - (start.getZ() - mapIcon.getPosZ())), 1f, 1f);
                            }
                        }

                        boolean mapHovered = posX <= 154 && posX >= 154 - mapWidth && posY <= -23 && posY >= -23 - mapHeight;
                        if (mapHovered) {
                            hoveredText = Collections.singletonList(TextFormatting.YELLOW + "Click to open Map!");
                        }

                    } catch (Exception ignored) { }

                    //reset settings
                    disableAlpha();
                    disableBlend();
                    ScreenRenderer.disableScissorTest();
                    ScreenRenderer.clearMask();
                }
            } else {
                drawTextLines(textLines, x - 154, y - 30, 1);

            }

            // back to menu button
            drawMenuButton(x, y, posX, posY);

            //Page text
            render.drawString(currentPage + " / " + pages, x + 80, y + 88, CommonColors.BLACK, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);

            drawForwardAndBackButtons(x, y, posX, posY, currentPage, pages);

            // available lootruns
            int currentY = 12;

            if (names.size() > 0) {
                for (int i = ((currentPage - 1) * 13); i < 13 * currentPage; i++) {
                    if (names.size() <= i) {
                        break;
                    }

                    boolean hovered = posX >= -146 && posX <= -13 && posY >= 87 - currentY && posY <= 96 - currentY;
                    //is string length of selectedName > 120?
                    String currentName = names.get(i);
                    boolean toCrop = !getFriendlyName(currentName, 120).equals(currentName);

                    int animationTick = -1;
                    if (hovered && !showAnimation) {
                        if (lastTick == 0 && !animationCompleted) {
                            lastTick = McIf.getSystemTime();
                        }

                        selected = i;
                        selectedName = currentName;

                        if (!animationCompleted) {
                            animationTick = (int) (McIf.getSystemTime() - lastTick) / 2;
                            if (animationTick >= 133 && !toCrop) {
                                animationCompleted = true;
                                animationTick = 133;
                            }
                        } else {
                            //reset animation to wait for scroll
                            if (toCrop) {
                                animationCompleted = false;
                                lastTick = McIf.getSystemTime() - 133 * 2;
                            }

                            animationTick = 133;
                        }

                        int width = Math.min(animationTick, 133);
                        animationTick -= 133 + 200;
                        if (LootRunManager.getActivePathName() != null && LootRunManager.getActivePathName().equals(currentName)) {
                            render.drawRectF(background_3, x + 9, y - 96 + currentY, x + 13 + width, y - 87 + currentY);
                            render.drawRectF(background_4, x + 9, y - 96 + currentY, x + 146, y - 87 + currentY);
                        } else {
                            render.drawRectF(background_1, x + 9, y - 96 + currentY, x + 13 + width, y - 87 + currentY);
                            render.drawRectF(background_2, x + 9, y - 96 + currentY, x + 146, y - 87 + currentY);
                        }

                        disableLighting();

                        if (LootRunManager.getActivePathName() != null && LootRunManager.getActivePathName().equals(currentName)) {
                            hoveredText = Arrays.asList(TextFormatting.BOLD + names.get(i), TextFormatting.YELLOW + "Loaded", TextFormatting.GOLD + "Middle click to open lootrun in folder",  TextFormatting.GREEN + "Left click to unload this lootrun");
                        } else {
                            hoveredText = Arrays.asList(TextFormatting.BOLD + names.get(i), TextFormatting.GREEN + "Left click to load", TextFormatting.GOLD + "Middle click to open lootrun in folder", TextFormatting.RED + "Shift-Right click to delete");
                        }
                    } else {
                        if (selected == i) {
                            animationCompleted = false;

                            if (!showAnimation) lastTick = 0;
                        }

                        if (LootRunManager.getActivePathName() != null && LootRunManager.getActivePathName().equals(names.get(i))) {
                            render.drawRectF(background_4, x + 13, y - 96 + currentY, x + 146, y - 87 + currentY);
                        } else {
                            render.drawRectF(background_2, x + 13, y - 96 + currentY, x + 146, y - 87 + currentY);
                        }
                    }

                    String friendlyName = getFriendlyName(currentName, 120);
                    if (selected == i && toCrop && animationTick > 0) {
                        int maxScroll = font.width(friendlyName) - (120 - 10);
                        int scrollAmount = (animationTick / 20) % (maxScroll + 60);

                        if (maxScroll <= scrollAmount && scrollAmount <= maxScroll + 40) {
                            // Stay on max scroll for 20 * 40 animation ticks after reaching the end
                            scrollAmount = maxScroll;
                        } else if (maxScroll <= scrollAmount) {
                            // And stay on minimum scroll for 20 * 20 animation ticks after looping back to the start
                            scrollAmount = 0;
                        }

                        ScreenRenderer.enableScissorTestX(x + 26, 13 + 133 - 2 - 26);
                        {
                            render.drawString(selectedName, x + 26 - scrollAmount, y - 95 + currentY, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                        }
                        ScreenRenderer.disableScissorTest();
                    } else {
                        render.drawString(friendlyName, x + 26, y - 95 + currentY, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                    }

                    currentY += 13;
                }

                renderHoveredText(mouseX, mouseY);
            } else {
                String textToDisplay = "No Lootruns were found!\nTry changing your search.";

                for (String line : textToDisplay.split("\n")) {
                    currentY += render.drawSplitString(line, 120, x + 26, y - 95 + currentY, 10, CommonColors.BLACK, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE) * 10 + 2;
                }
            }
        }
        ScreenRenderer.endGL();
    }

    @Override
    protected void searchUpdate(String currentText) {
        names = LootRunManager.getStoredLootruns();

        if (currentText != null && !currentText.isEmpty()) {
            String lowerCase = currentText.toLowerCase();
            names.removeIf(c -> !doesSearchMatch(c.toLowerCase(Locale.ROOT), lowerCase));
        }

        Collections.sort(names);

        updateSelected();

        pages = names.size() <= 13 ? 1 : (int) Math.ceil(names.size() / 13d);
        currentPage = Math.min(currentPage, pages);
        refreshAccepts();
    }

    private void updateSelected() {
        if (selectedName == null) return;

        selected = names.indexOf(selectedName);

        if (selected == -1) {
            selectedName = null;
        }
    }

    public String getFriendlyName(String str, int width) {
        if (!(McIf.mc().font.width(str) > width)) return str;

        str += "...";

        while (McIf.mc().font.width(str) > width) {
            str = str.substring(0, str.length() - 4).trim() + "...";
        }

        return str;
    }

    @Override
    protected void drawSearchBar(int centerX, int centerY) {
        super.drawSearchBar(centerX, centerY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        MainWindow res = new MainWindow(McIf.mc());
        int posX = ((res.getGuiScaledWidth() / 2) - mouseX);
        int posY = ((res.getGuiScaledHeight() / 2) - mouseY);

        checkMenuButton(posX, posY);
        checkForwardAndBackButtons(posX, posY);

        int currentY = 12 + 13 * (selected % 13);

        boolean hovered = posX >= -146 && posX <= -13 && posY >= 87 - currentY && posY <= 96 - currentY;
        boolean isTracked = (LootRunManager.getActivePathName() != null && LootRunManager.getActivePathName().equals(selectedName));

        if (hovered && names.size() > selected) {
            if (mouseButton == 0) { //left click means either load or unload
                if (isTracked) {
                    if (LootRunManager.getActivePath() != null) {
                        LootRunManager.clear();
                    }
                } else {
                    boolean result = LootRunManager.loadFromFile(selectedName);

                    if (result) {
                        try {
                            Location start = LootRunManager.getActivePath().getPoints().get(0);
                            String startingPointMsg = "Loot run " + LootRunManager.getActivePathName() + " starts at [" + (int) start.getX() + ", " + (int) start.getZ() + "]";

                            McIf.mc().submit(() ->
                                    ChatOverlay.getChat().printChatMessageWithOptionalDeletion(new StringTextComponent(startingPointMsg), MESSAGE_ID)
                            );

                            McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.BLOCK_ANVIL_PLACE, 1f));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                return true;
            } else if (mouseButton == 1 && isShiftKeyDown() && !isTracked) { //shift right click means delete
                boolean result = LootRunManager.delete(selectedName);
                if (result) {
                    names.remove(selected);
                    updateSelected();
                    McIf.mc().getSoundManager().play(SimpleSound.forUI(SoundEvents.ENTITY_IRONGOLEM_HURT, 1f));
                }

                return true;
            } else if (mouseButton == 2) { //middle click means open up folder
                File lootrunPath = new File(LootRunManager.STORAGE_FOLDER, selectedName + ".json");
                String uri = lootrunPath.toURI().toString();
                Utils.openUrl(uri);
                return true;
            }
        }

        //open the map when clicked
        int mapWidth = 145;
        int mapHeight = 58;

        boolean mapHovered = posX <= 154 && posX >= 154 - mapWidth && posY <= -23 && posY >= -23 - mapHeight;
        if (mapHovered && LootRunManager.getActivePathName() != null) {
            if (Reference.onWorld) {
                if (WebManager.getApiUrls() == null) {
                    WebManager.tryReloadApiUrls(true);
                } else {
                    Location start = LootRunManager.getActivePath().getPoints().get(0);
                    Utils.setScreen(new MainWorldMapUI((int) start.x, (int) start.z));
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
