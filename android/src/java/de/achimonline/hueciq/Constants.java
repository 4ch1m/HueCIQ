package de.achimonline.hueciq;

import java.util.HashMap;

public class Constants
{
    public static final boolean RELEASE_BUILD = true;
    public static final boolean LOG_ACTIVE = !RELEASE_BUILD;
    public static final int ACTION_LOG_SIZE = 500;

    public static HashMap<String, Integer> HUE_COLORS = new HashMap<String, Integer>();

    static
    {
        HUE_COLORS.put("red", 0);
        HUE_COLORS.put("blue", 47000);
        HUE_COLORS.put("green", 25500);
        HUE_COLORS.put("yellow", 12800);
        HUE_COLORS.put("orange", 3800);
        HUE_COLORS.put("purple", 50000);
    }
}
