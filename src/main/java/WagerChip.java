public enum WagerChip
{
  WHITE(100, "/chip_white.png"),
  PINK(250, "/chip_pink.png"),        //$2.50
  RED(500, "/chip_red.png"),
  GREEN(2500, "/chip_green.png"),
  BLACK(10000, "/chip_black.png"),
  PURPLE(50000, "/chip_purple.png"),  //$500
  ORANGE(100000, "/chip_orange.png");

  private final int centValue;
  private final String imagePath;

  WagerChip(
      int centValue,
      String imagePath)
  {
    this.centValue =
        centValue;

    this.imagePath =
        imagePath;
  }

  public int getCentValue()
  {
    return centValue;
  }

  public String getImagePath()
  {
    return imagePath;
  }
}
