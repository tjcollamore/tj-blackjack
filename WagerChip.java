import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Arrays;

public enum WagerChip
{
  ORANGE(1000, "orange", "Resources/chip_orange.png"),
  PURPLE(500, "purple", "Resources/chip_purple.png"),
  BLACK(100, "black", "Resources/chip_black.png"),
  GREEN(25, "green", "Resources/chip_green.png"),
  RED(5, "red", "Resources/chip_red.png"),
  PINK(2.5, "pink", "Resources/chip_pink.png"),
  WHITE(1, "white", "Resources/chip_white.png");

  private double value;
  private String color;
  private String imagePath;

  private WagerChip(double value, String color, String imagePath)
  {
    this.value = value;
    this.color = color;
    this.imagePath = imagePath;
  }

  public double getValue()
  {
    return value;
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

    // Convert dollars to cents to avoid floating point errors
    int remainingCents = (int) Math.round(amount * 100);

    // Sort chips in descending order of value
    List<WagerChip> sorted = Arrays.stream(WagerChip.values())
        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
        .toList();

    for (WagerChip chip : sorted) {
        int chipCents = (int) Math.round(chip.getValue() * 100);
        int count = remainingCents / chipCents;
        if (count > 0) {
            result.put(chip, count);
            remainingCents -= chipCents * count;
        }
    }

    return result;
  }
}

