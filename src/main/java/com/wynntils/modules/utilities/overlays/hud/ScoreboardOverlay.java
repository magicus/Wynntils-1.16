package com.wynntils.modules.utilities.overlays.hud;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.modules.utilities.configs.OverlayConfig;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IngameGuiForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class ScoreboardOverlay extends Overlay {

    public ScoreboardOverlay() {
        super("Scoreboard", 125, 60, true, 1, 0.5f, 0, 0, OverlayGrowFrom.MIDDLE_RIGHT);
    }


    @Override
    public void render(RenderGameOverlayEvent.Pre event) {
        if (!Reference.onWorld || !OverlayConfig.Scoreboard.INSTANCE.enableScoreboard ||
                event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;

        // vanilla fontrenderer
        FontRenderer font = McIf.mc().font;

        ScoreObjective objective = McIf.world().getScoreboard().getObjectiveInDisplaySlot(1); // sidebar objective
        if (objective == null) return;

        // get the 15 highest scores
        Scoreboard scoreboard = objective.getScoreboard();
        List<Score> scores = Lists.newArrayList(Iterables.filter(scoreboard.getSortedScores(objective), s -> s.getPlayerName() != null && !s.getPlayerName().startsWith("#")));
        if (scores.size() > 15) scores = new ArrayList<Score>(scores.subList(0, 15));

        // remove objective, compass lines, then remove unnecessary blanks
        if (OverlayConfig.Objectives.INSTANCE.enableObjectives) removeObjectiveLines(scores);
        if (!OverlayConfig.Scoreboard.INSTANCE.showCompass) removeCompassLines(scores);
        trimBlankLines(scores);

        // nothing to display
        if (scores.isEmpty()) return;

        // calculate width based on widest line
        int width = OverlayConfig.Scoreboard.INSTANCE.showTitle ? font.width(objective.getDisplayName()) : 0;
        for (Score s : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(s.getPlayerName());
            String line = ScorePlayerTeam.formatPlayerName(team, s.getPlayerName());
            if (OverlayConfig.Scoreboard.INSTANCE.showNumbers) line += ": " + TextFormatting.RED + s.getScorePoints();
            width = Math.max(width, font.width(line));
        }

        int height = scores.size() * font.lineHeight;
        int yOffset = height/3;
        int xOffset = -3 - width;

        // Background box
        if (OverlayConfig.Scoreboard.INSTANCE.opacity > 0) {
            int titleOffset = OverlayConfig.Scoreboard.INSTANCE.showTitle ? font.lineHeight + 1 : 1;
            drawRect(OverlayConfig.Scoreboard.INSTANCE.backgroundColor, xOffset - 2, yOffset, -1, yOffset - height - titleOffset);
        }

        // draw lines
        int lineCount = 1;
        for (Score s : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(s.getPlayerName());
            String name = ScorePlayerTeam.formatPlayerName(team, s.getPlayerName());
            String score = TextFormatting.RED + "" + s.getScorePoints();

            // draw line, including score if enabled
            int y = yOffset - (lineCount * font.lineHeight);
            font.drawString(name, drawingOrigin().x + xOffset, drawingOrigin().y + y, CommonColors.WHITE.toInt());
            if (OverlayConfig.Scoreboard.INSTANCE.showNumbers) font.drawString(score, drawingOrigin().x - 1 - font.width(score), drawingOrigin().y + y, CommonColors.WHITE.toInt());

            lineCount++;
            if (lineCount > scores.size() && OverlayConfig.Scoreboard.INSTANCE.showTitle) { // end of scores, draw title if enabled
                String title = objective.getDisplayName();
                font.drawString(title, drawingOrigin().x + xOffset + width/2 - font.width(title)/2, drawingOrigin().y + y - font.lineHeight, CommonColors.WHITE.toInt());
            }
        }

    }

    private void removeObjectiveLines(List<Score> scores) {
        scores.removeIf(s -> McIf.getTextWithoutFormattingCodes(s.getPlayerName()).matches(ObjectivesOverlay.OBJECTIVE_PATTERN.pattern()));
        scores.removeIf(s -> McIf.getTextWithoutFormattingCodes(s.getPlayerName()).matches("- All done"));
        scores.removeIf(s -> McIf.getTextWithoutFormattingCodes(s.getPlayerName()).matches("(Daily )?Objectives?:"));
    }

    private void removeCompassLines(List<Score> scores) {
        scores.removeIf(s -> s.getPlayerName().matches(TextFormatting.LIGHT_PURPLE + "(Follow your compass|to reach that location)"));
    }

    private void trimBlankLines(List<Score> scores) {
        List<Score> toRemove = new ArrayList<>();
        for (int i = scores.size()-1; i >= 0; i--) {
            Score score = scores.get(i);
            if (!McIf.getTextWithoutFormattingCodes(score.getPlayerName()).isEmpty()) continue;
            if (i == 0 || McIf.getTextWithoutFormattingCodes(scores.get(i-1).getPlayerName()).isEmpty())
                toRemove.add(score);
        }

        scores.removeAll(toRemove);

        // remove title spacer if title is disabled
        if (!OverlayConfig.Scoreboard.INSTANCE.showTitle && !scores.isEmpty() && McIf.getTextWithoutFormattingCodes(scores.get(scores.size()-1).getPlayerName()).isEmpty())
            scores.remove(scores.size()-1);
    }

    public static void enableCustomScoreboard(boolean enabled) {
        IngameGuiForge.renderObjective = !enabled;
    }

}
