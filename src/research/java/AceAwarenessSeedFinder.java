public class AceAwarenessSeedFinder
{
  private static final int TARGET_COUNT =
      50;

  private static final long MAX_SEED =
      1_000_000L;


  public static void main(
      String[] args)
  {
    findSwitchSeeds();
    findRichSeeds();
  }


  private static void findSwitchSeeds()
  {
    System.out.println(
        "SWITCH SEEDS"
    );

    int found =
        0;


    for (long seed = 0;
         seed < MAX_SEED
             && found < TARGET_COUNT;
         seed++)
    {
      int[] balances =
          getCheckpointBalances(
              seed
          );


      int first =
          balances[0];

      int second =
          balances[1];

      int third =
          balances[2];


      boolean useful =
          first <= -1
          && first >= -3
          && second <= -2
          && second >= -4
          && third <= -2;


      if (useful)
      {
        System.out.println(
            seed
            + "  "
            + first
            + ", "
            + second
            + ", "
            + third
        );

        found++;
      }
    }


    System.out.println(
        "Found "
        + found
    );
  }


    private static void findRichSeeds()
    {
      System.out.println(
          "\nACE-RICH SEEDS"
      );

      int found =
          0;


      for (long seed = 0;
           seed < MAX_SEED
               && found < TARGET_COUNT;
           seed++)
      {
        int[] balances =
            getRichCheckpointBalances(
                seed
            );


        int first =
            balances[0];

        int second =
            balances[1];

        int third =
            balances[2];


        boolean useful =
            first >= 4
            && second >= 1
            && second <= 4
            && third >= 0
            && third <= 3
            && (
                second != first
                || third != second
            );


        if (useful)
        {
          System.out.println(
              seed
              + "  "
              + first
              + ", "
              + second
              + ", "
              + third
          );

          found++;
        }
      }


      System.out.println(
          "Found "
          + found
      );
    }
    
    private static int[] getRichCheckpointBalances(
        long seed)
    {
      AceAwarenessPracticeSession session =
          new AceAwarenessPracticeSession(
              seed
          );

      double[] targets =
          {
            1.00,
            1.25,
            1.50
          };

      int[] balances =
          new int[
              targets.length
          ];


      for (int i = 0;
           i < targets.length;
           i++)
      {
        playToDecksSeen(
            session,
            targets[i]
        );

        balances[i] =
            session
                .getAceCount()
                .getMentalBalance(
                    session.getDecksSeen()
                );
      }


      return balances;
    }
    
  private static int[] getCheckpointBalances(
      long seed)
  {
    AceAwarenessPracticeSession session =
        new AceAwarenessPracticeSession(
            seed
        );

    double[] targets =
        {
          0.35,
          0.75,
          1.00
        };

    int[] balances =
        new int[
            targets.length
        ];


    for (int i = 0;
         i < targets.length;
         i++)
    {
      playToDecksSeen(
          session,
          targets[i]
      );

      balances[i] =
          session
              .getAceCount()
              .getMentalBalance(
                  session.getDecksSeen()
              );
    }


    return balances;
  }


  private static void playToDecksSeen(
      AceAwarenessPracticeSession session,
      double target)
  {
    while (session.getDecksSeen()
           < target)
    {
      session.playNextHand();
    }
  }
}