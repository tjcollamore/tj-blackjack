public class AceOnlyCount
    implements CountingSystem
{
  private int acesSeen;
  private int visibleCardsSeen;


  @Override
  public void reset()
  {
    acesSeen =
        0;

    visibleCardsSeen =
        0;
  }


  @Override
  public void observe(
      Card card)
  {
    visibleCardsSeen++;


    if (card.getRank()
        == Rank.ACE)
    {
      acesSeen++;
    }
  }


  @Override
  public double getSignal(
      int remainingCards)
  {
    double decksRemaining =
        remainingCards / 52.0;


    if (decksRemaining <= 0.0)
      return 0.0;


    return getRunningSurplus()
        / decksRemaining;
  }

    public int getMentalBalance(
        double decksSeen)
    {
      int fullDecksSeen =
          (int) decksSeen;

      return 4 * fullDecksSeen
          - acesSeen;
    }
    
  public double getRunningSurplus()
  {
    double expectedAcesSeen =
        visibleCardsSeen
        / 13.0;


    return expectedAcesSeen
        - acesSeen;
  }

    public double getAceBalance()
    {
      return getRunningSurplus();
    }
    
  public int getAcesSeen()
  {
    return acesSeen;
  }


  public int getVisibleCardsSeen()
  {
    return visibleCardsSeen;
  }


  @Override
  public String getName()
  {
    return "ACE_ONLY";
  }
}
