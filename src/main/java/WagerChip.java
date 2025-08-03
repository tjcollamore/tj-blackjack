
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public enum WagerChip {
    ORANGE(1000, "orange", "/chip_orange.png"),
    PURPLE(500, "purple", "/chip_purple.png"),
    BLACK(100, "black", "/chip_black.png"),
    GREEN(25, "green", "/chip_green.png"),
    RED(5, "red", "/chip_red.png"),
    PINK(2.5, "pink", "/chip_pink.png"),
    WHITE(1, "white", "/chip_white.png");

    private final double value;
    private final String color;
    private final String imagePath;

    WagerChip(double value, String color, String imagePath) {
        this.value = value;
        this.color = color;
        this.imagePath = imagePath;
    }

    public double getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }

    public String getImagePath() {
        return imagePath;
    }

    public static Map<WagerChip, Integer> getChipsByValue(double amount) {
        Map<WagerChip, Integer> result = new LinkedHashMap<>();
        
        // Convert to cents to avoid floating point precision issues
        int cents = (int) Math.round(amount * 100);
        
        // Process chips in order from highest to lowest value
        for (WagerChip chip : values()) {
            int chipCents = (int) Math.round(chip.getValue() * 100);
            if (cents >= chipCents) {
                int count = cents / chipCents;
                result.put(chip, count);
                cents -= count * chipCents;
            }
        }
        
        return result;
    }
}
