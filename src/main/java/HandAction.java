public enum HandAction
{
  HIT("HIT"),
  STAND("STAND"),
  DOUBLE("DOUBLE"),
  SPLIT("SPLIT"),
  SURRENDER("⚑"); //🏳(U+1F3F3)

  private String displayText;

  HandAction(String displayText)
  {
    this.displayText = displayText;
  }

  public String getDisplayText()
  {
    return displayText;
  }

  public static HandAction fromDisplayText(String text)
  {
    for (HandAction action : values())
    {
      if (action.getDisplayText().equals(text))
        return action;
    }

    throw new IllegalArgumentException(
        "Unknown hand action: " + text);
  }
}

 
