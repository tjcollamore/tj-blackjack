public class GameSettings
{
  private boolean allowSurrender = true;
  private boolean doubleAfterSplit = true;
  private boolean surrenderAfterSplit = false;
  private boolean coachingEnabled = false;
  private boolean handSignalsEnabled = false;

  public boolean handSignalsEnabled()
  {
    return handSignalsEnabled;
  }

  public void setHandSignalsEnabled(
      boolean enabled)
  {
    handSignalsEnabled = enabled;
  }
  
  public boolean coachingEnabled()
  {
    return coachingEnabled;
  }

  public void setCoachingEnabled(
      boolean coachingEnabled)
  {
    this.coachingEnabled = coachingEnabled;
  }
  
  public boolean allowSurrender()
  {
    return allowSurrender;
  }

  public void setAllowSurrender(
      boolean allowSurrender)
  {
    this.allowSurrender = allowSurrender;
  }

  public boolean doubleAfterSplit()
  {
    return doubleAfterSplit;
  }

  public void setDoubleAfterSplit(
      boolean doubleAfterSplit)
  {
    this.doubleAfterSplit = doubleAfterSplit;
  }

  public boolean surrenderAfterSplit()
  {
    return surrenderAfterSplit;
  }

  public void setSurrenderAfterSplit(
      boolean surrenderAfterSplit)
  {
    this.surrenderAfterSplit =
        surrenderAfterSplit;
  }
  
  private int decksInShoe = 8;

  public int getDecksInShoe()
  {
    return decksInShoe;
  }

  public void setDecksInShoe(int decksInShoe)
  {
    if (decksInShoe != 1
        && decksInShoe != 2
        && decksInShoe != 6
        && decksInShoe != 8)
    {
      throw new IllegalArgumentException(
          "Unsupported deck count: " + decksInShoe
      );
    }

    this.decksInShoe = decksInShoe;
  }
  

  
  public enum FeltColor
  {
    GREEN,
    BLUE,
    BURGUNDY
  }

  public enum BlackjackPayout
  {
    SIX_TO_FIVE(2.2),
    THREE_TO_TWO(2.5);

    private final double totalReturnMultiplier;

    BlackjackPayout(double totalReturnMultiplier)
    {
      this.totalReturnMultiplier =
          totalReturnMultiplier;
    }

    public double getTotalReturnMultiplier()
    {
      return totalReturnMultiplier;
    }
  }

  public BlackjackPayout getBlackjackPayout()
  {
    return blackjackPayout;
  }

  public void setBlackjackPayout(
      BlackjackPayout blackjackPayout)
  {
    this.blackjackPayout = blackjackPayout;
  }
  
  private FeltColor feltColor = FeltColor.GREEN;
  private boolean dealerHitsSoft17 = true;
  private BlackjackPayout blackjackPayout = BlackjackPayout.SIX_TO_FIVE;
  
  public FeltColor getFeltColor()
  {
    return feltColor;
  }

  public void setFeltColor(
      FeltColor feltColor)
  {
    this.feltColor = feltColor;
  }

  public void setDealerHitsSoft17(boolean dealerHitsSoft17)
  {
    this.dealerHitsSoft17 = dealerHitsSoft17;
  }

  public boolean dealerHitsSoft17()
  {
    return dealerHitsSoft17;
  }
}
