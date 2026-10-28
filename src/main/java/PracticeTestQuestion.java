public class PracticeTestQuestion
{
  private final String id;
  private final Rank[] playerRanks;
  private final Rank dealerRank;
  private final PracticeTestCategory category;
  private final boolean challenging;


  public PracticeTestQuestion(
      String id,
      Rank[] playerRanks,
      Rank dealerRank,
      PracticeTestCategory category,
      boolean challenging)
  {
    this.id =
        id;

    this.playerRanks =
        playerRanks.clone();

    this.dealerRank =
        dealerRank;

    this.category =
        category;

    this.challenging =
        challenging;
  }


  public String getId()
  {
    return id;
  }


  public Rank[] getPlayerRanks()
  {
    return playerRanks.clone();
  }


  public Rank getDealerRank()
  {
    return dealerRank;
  }


  public PracticeTestCategory getCategory()
  {
    return category;
  }


  public boolean isChallenging()
  {
    return challenging;
  }


  public String getPlayerHandText()
  {
    StringBuilder builder =
        new StringBuilder();


    for (int i = 0;
         i < playerRanks.length;
         i++)
    {
      if (i > 0)
      {
        builder.append(
            ","
        );
      }


      builder.append(
          playerRanks[i].getShortName()
      );
    }


    return builder.toString();
  }


  public String getDealerText()
  {
    return dealerRank.getShortName();
  }
}
