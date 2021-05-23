package com.wynntils.modules.map.instances;

import com.wynntils.McIf;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.core.utils.objects.Location;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;

import static com.wynntils.transition.GlStateManager.*;

public class LootRunNote {

    private Location location;
    private String note;

    private static final ScreenRenderer renderer = new ScreenRenderer();

    public LootRunNote(Location location, String note) {
        this.location = location;
        this.note = note;
    }

    public Location getLocation() {
        return location;
    }

    public String getNote() {
        return note;
    }

    public String getLocationString() {
        return "(" + (int) location.x + ", " + (int) location.y + ", " + (int) location.z + ")";
    }

    public String getShortLocationString() {
        return (int) location.x + "," + (int) location.y + "," + (int) location.z;
    }

    public void drawNote(CustomColor color) {
        EntityRendererManager render = McIf.mc().getEntityRenderDispatcher();
        FontRenderer fr = render.getFont();

        if (McIf.player().getDistanceSq(location.x, location.y, location.z) > 4096f)
            return; // only draw nametag when close

        String[] lines = StringUtils.wrapTextBySize(note, 200);
        int offsetY = -(fr.lineHeight * lines.length) / 2;

        for (String line : lines) {
            drawNametag(line, color, (float) (location.x - render.viewerPosX), (float) (location.y - render.viewerPosY + 2), (float) (location.z - render.viewerPosZ), offsetY, render.playerViewY, render.playerViewX, render.options.thirdPersonView == 2);
            offsetY += fr.lineHeight;
        }
    }

    private static void drawNametag(String input, CustomColor color, float x, float y, float z, int verticalShift, float viewerYaw, float viewerPitch, boolean isThirdPersonFrontal) {
        pushMatrix();
        {
            ScreenRenderer.beginGL(0, 0); // we set to 0 because we don't want the ScreenRender to handle this thing
            {
                // positions
                translate(x, y, z); // translates to the correct postion
                glNormal3f(0.0F, 1.0F, 0.0F);
                rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
                rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
                scale(-0.025F, -0.025F, 0.025F);
                disableLighting();

                int middlePos = (int) renderer.width(input) / 2;

                // draws the label
                renderer.drawString(input, -middlePos, verticalShift, color, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

                // renders twice to replace the areas that are overlaped by tile entities
                enableDepth();
                renderer.drawString(input, -middlePos, verticalShift, color, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);

                // returns back to normal
                enableDepth();
                enableLighting();
                disableBlend();
                color(1.0f, 1.0f, 1.0f, 1.0f);
            }
            ScreenRenderer.endGL();
        }
        popMatrix();
    }

}
