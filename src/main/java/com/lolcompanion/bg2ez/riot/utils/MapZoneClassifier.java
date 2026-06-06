package com.lolcompanion.bg2ez.riot.utils;

import org.springframework.stereotype.Component;

@Component
public class MapZoneClassifier {
    // Map is 0-15000 on both axes
    // Key coordinates
    private static final int DRAGON_X = 9866, DRAGON_Y = 4414;
    private static final int BARON_X = 5007, BARON_Y = 10471;
    private static final int DRAGON_RANGE = 2000;
    private static final int BARON_RANGE = 2000;

    public String classify(int x, int y) {
        // Objective zones — check first, highest priority
        if (isNear(x, y, DRAGON_X, DRAGON_Y, DRAGON_RANGE)) return "DRAGON_PIT";
        if (isNear(x, y, BARON_X, BARON_Y, BARON_RANGE)) return "BARON_PIT";

        // Top lane: high Y, low X
        // Based on turrets: outer(981,10441), inner(1512,6699)
        if (x < 3500 && y > 6000) return "TOP_LANE";

        // Bot lane: low Y, high X
        // Based on turrets: outer(10504,1029), inner(6919,1483)
        if (x > 6000 && y < 3500) return "BOT_LANE";

        // Mid lane: diagonal corridor
        // Blue mid outer(5846,6396), Red mid outer(8955,8510)
        if (isDiagonalMidLane(x, y)) return "MID_LANE";

        // Base zones
        if (x < 3000 && y < 3000) return "BLUE_BASE";
        if (x > 12000 && y > 12000) return "RED_BASE";

        // Jungle is everything else
        return "JUNGLE";
    }

    private boolean isDiagonalMidLane(int x, int y) {
        int midRangeMin = 3500;
        int midRangeMax = 11500;
        if (x < midRangeMin || x > midRangeMax) return false;
        if (y < midRangeMin || y > midRangeMax) return false;
        return Math.abs(x - y) < 2000;
    }

    private boolean isNear(int x, int y, int targetX, int targetY, int range) {
        int dx = x - targetX;
        int dy = y - targetY;
        return (dx * dx + dy * dy) < (range * range);
    }

    public String roleToExpectedZone(String teamPosition) {
        return switch (teamPosition) {
            case "TOP" -> "TOP_LANE";
            case "JUNGLE" -> "JUNGLE";
            case "MIDDLE" -> "MID_LANE";
            case "BOTTOM" -> "BOT_LANE";
            case "UTILITY" -> "BOT_LANE";
            default -> "UNKNOWN";
        };
    }

    public boolean isSideLane(String zone) {
        return "TOP_LANE".equals(zone) || "BOT_LANE".equals(zone);
    }

    public boolean isObjective(String zone) {
        return "DRAGON_PIT".equals(zone) || "BARON_PIT".equals(zone);
    }
}