package Lexers;

import java.awt.*;

public class ColorType {
    private final String colorIdentifier;
    private final Color color;

    public static final String[] ANSI = {
            "ansi0",
            "ansi1",
            "ansi2",
            "ansi3",
            "ansi4",
            "ansi5",
            "ansi6",
            "ansi7",
            "ansi8",
            "ansi9",
            "ansi10",
            "ansi11",
            "ansi12",
            "ansi13",
            "ansi14",
            "ansi15"
    };
    public static final String BACKGROUND = "backgroundcolor";
    public static final String FOREGROUND = "foregroundcolor";

    public ColorType(String colorIdentifier, Color color) {
        this.colorIdentifier = colorIdentifier;
        this.color = color;
    }

    public String getColorIdentifier() {
        return colorIdentifier;
    }

    public Color getColor() {
        return color;
    }
}
