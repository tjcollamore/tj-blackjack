import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PracticeTestSessionBuilder
{
  private static final int HARD_COUNT =
      12;

  private static final int SOFT_COUNT =
      10;

  private static final int PAIR_COUNT =
      10;

  private static final int SURRENDER_COUNT =
      8;

  private static final int CONTROL_COUNT =
      10;


  public PracticeTestSession build(
      PracticeTestDifficulty difficulty)
  {
    return build(
        difficulty,
        new Random()
    );
  }


  /*
   * Seeded overload is useful for unit tests
   * and reproducible debugging.
   */
  public PracticeTestSession build(
      PracticeTestDifficulty difficulty,
      long seed)
  {
    return build(
        difficulty,
        new Random(
            seed
        )
    );
  }


  private PracticeTestSession build(
      PracticeTestDifficulty difficulty,
      Random random)
  {
    List<PracticeTestQuestion> selected =
        new ArrayList<>();


    addRandomQuestions(
        selected,
        PracticeTestQuestionBank
            .getChallengingQuestions(
                PracticeTestCategory.HARD
            ),
        HARD_COUNT,
        random
    );


    addRandomQuestions(
        selected,
        PracticeTestQuestionBank
            .getChallengingQuestions(
                PracticeTestCategory.SOFT
            ),
        SOFT_COUNT,
        random
    );


    addRandomQuestions(
        selected,
        PracticeTestQuestionBank
            .getChallengingQuestions(
                PracticeTestCategory.PAIR
            ),
        PAIR_COUNT,
        random
    );


    addRandomQuestions(
        selected,
        PracticeTestQuestionBank
            .getChallengingQuestions(
                PracticeTestCategory.SURRENDER
            ),
        SURRENDER_COUNT,
        random
    );


    addRandomQuestions(
        selected,
        PracticeTestQuestionBank
            .getControlQuestions(),
        CONTROL_COUNT,
        random
    );


    /*
     * Don't present questions grouped by category.
     */
    Collections.shuffle(
        selected,
        random
    );


    return new PracticeTestSession(
        difficulty,
        selected
    );
  }


  private void addRandomQuestions(
      List<PracticeTestQuestion> destination,
      List<PracticeTestQuestion> source,
      int count,
      Random random)
  {
    if (source.size()
        < count)
    {
      throw new IllegalStateException(
          "Not enough questions. Needed "
          + count
          + " but only found "
          + source.size()
      );
    }


    List<PracticeTestQuestion> shuffled =
        new ArrayList<>(
            source
        );


    Collections.shuffle(
        shuffled,
        random
    );


    for (int i = 0;
         i < count;
         i++)
    {
      destination.add(
          shuffled.get(i)
      );
    }
  }
}
