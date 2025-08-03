
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public enum WagerChip {
    ORANGE(100000, "orange", "resources/chip_orange.png"),
    PURPLE(50000, "purple", "resources/chip_purple.png"),
    BLACK(10000, "black", "resources/chip_black.png"),
    GREEN(2500, "green", "resources/chip_green.png"),
    RED(500, "red", "resources/chip_red.png"),
    PINK(250, "pink", "resources/chip_pink.png"),
    WHITE(100, "white", "resources/chip_white.png");

    private final int centValue;
    private final String color;
    private final String imagePath;

    WagerChip(int centValue, String color, String imagePath) {
        this.centValue = centValue;
        this.color = color;
        this.imagePath = imagePath;
    }

    public double getValue()
    {
        return centValue / 100.0;
    }

    public int getCentValue()
    {
        return centValue;
    }

    public String getColor()
    {
        return color;
    }

    public String getImagePath()
    {
        return imagePath;
    }

    public static Map<WagerChip, Integer> getChipsByValue(double amount)
    {
        Map<WagerChip, Integer> result = new LinkedHashMap<>();
        int cents = (int) (amount * 100);
        // Process chips in order from highest to lowest value
        for (WagerChip chip : values())
        {
            if (chip.getCentValue() < cents)
            {
                if (chip == PINK && cents % 100 == 0)
                    continue;
                
                int count = cents / chip.getCentValue();
                result.put(chip, count);
                cents -= count * chip.getCentValue();
            }
        }
        return result;
    }
}
