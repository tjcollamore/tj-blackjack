import java.util.ArrayList;
import java.util.List;

public class PracticeTestQuestionBank
{
  private static final List<PracticeTestQuestion>
      QUESTIONS =
      List.of(

          // ====================================================
          // CHALLENGING — HARD HANDS
          // ====================================================

          q(
              "HARD_12_VS_2",
              PracticeTestCategory.HARD,
              true,
              Rank.TWO,
              Rank.TEN,
              Rank.TWO
          ),

          q(
              "HARD_12_VS_3",
              PracticeTestCategory.HARD,
              true,
              Rank.THREE,
              Rank.TEN,
              Rank.TWO
          ),

          q(
              "HARD_12_VS_4",
              PracticeTestCategory.HARD,
              true,
              Rank.FOUR,
              Rank.TEN,
              Rank.TWO
          ),

          q(
              "HARD_12_VS_6",
              PracticeTestCategory.HARD,
              true,
              Rank.SIX,
              Rank.TEN,
              Rank.TWO
          ),

          q(
              "HARD_13_VS_2",
              PracticeTestCategory.HARD,
              true,
              Rank.TWO,
              Rank.TEN,
              Rank.THREE
          ),

          q(
              "HARD_13_VS_6",
              PracticeTestCategory.HARD,
              true,
              Rank.SIX,
              Rank.TEN,
              Rank.THREE
          ),

          q(
              "HARD_15_VS_7",
              PracticeTestCategory.HARD,
              true,
              Rank.SEVEN,
              Rank.TEN,
              Rank.FIVE
          ),

          q(
              "HARD_16_VS_7",
              PracticeTestCategory.HARD,
              true,
              Rank.SEVEN,
              Rank.TEN,
              Rank.SIX
          ),

          q(
              "HARD_11_VS_ACE",
              PracticeTestCategory.HARD,
              true,
              Rank.ACE,
              Rank.SIX,
              Rank.FIVE
          ),

          q(
              "HARD_10_VS_10",
              PracticeTestCategory.HARD,
              true,
              Rank.TEN,
              Rank.SIX,
              Rank.FOUR
          ),

          q(
              "HARD_10_VS_ACE",
              PracticeTestCategory.HARD,
              true,
              Rank.ACE,
              Rank.SIX,
              Rank.FOUR
          ),

          q(
              "HARD_9_VS_2",
              PracticeTestCategory.HARD,
              true,
              Rank.TWO,
              Rank.FIVE,
              Rank.FOUR
          ),

          q(
              "HARD_9_VS_3",
              PracticeTestCategory.HARD,
              true,
              Rank.THREE,
              Rank.FIVE,
              Rank.FOUR
          ),

          q(
              "HARD_9_VS_6",
              PracticeTestCategory.HARD,
              true,
              Rank.SIX,
              Rank.FIVE,
              Rank.FOUR
          ),


          // ====================================================
          // CHALLENGING — SOFT HANDS
          // ====================================================

          q(
              "SOFT_A8_VS_6",
              PracticeTestCategory.SOFT,
              true,
              Rank.SIX,
              Rank.ACE,
              Rank.EIGHT
          ),

          q(
              "SOFT_A7_VS_2",
              PracticeTestCategory.SOFT,
              true,
              Rank.TWO,
              Rank.ACE,
              Rank.SEVEN
          ),

          q(
              "SOFT_A7_VS_3",
              PracticeTestCategory.SOFT,
              true,
              Rank.THREE,
              Rank.ACE,
              Rank.SEVEN
          ),

          q(
              "SOFT_A7_VS_4",
              PracticeTestCategory.SOFT,
              true,
              Rank.FOUR,
              Rank.ACE,
              Rank.SEVEN
          ),

          q(
              "SOFT_A7_VS_6",
              PracticeTestCategory.SOFT,
              true,
              Rank.SIX,
              Rank.ACE,
              Rank.SEVEN
          ),

          q(
              "SOFT_A7_VS_9",
              PracticeTestCategory.SOFT,
              true,
              Rank.NINE,
              Rank.ACE,
              Rank.SEVEN
          ),

          q(
              "SOFT_A7_VS_ACE",
              PracticeTestCategory.SOFT,
              true,
              Rank.ACE,
              Rank.ACE,
              Rank.SEVEN
          ),

          q(
              "SOFT_A6_VS_3",
              PracticeTestCategory.SOFT,
              true,
              Rank.THREE,
              Rank.ACE,
              Rank.SIX
          ),

          q(
              "SOFT_A6_VS_4",
              PracticeTestCategory.SOFT,
              true,
              Rank.FOUR,
              Rank.ACE,
              Rank.SIX
          ),

          q(
              "SOFT_A2_VS_4",
              PracticeTestCategory.SOFT,
              true,
              Rank.FOUR,
              Rank.ACE,
              Rank.TWO
          ),

          q(
              "SOFT_A3_VS_4",
              PracticeTestCategory.SOFT,
              true,
              Rank.FOUR,
              Rank.ACE,
              Rank.THREE
          ),

          q(
              "SOFT_A2_VS_5",
              PracticeTestCategory.SOFT,
              true,
              Rank.FIVE,
              Rank.ACE,
              Rank.TWO
          ),


          // ====================================================
          // CHALLENGING — PAIRS
          // ====================================================

          q(
              "PAIR_99_VS_7",
              PracticeTestCategory.PAIR,
              true,
              Rank.SEVEN,
              Rank.NINE,
              Rank.NINE
          ),

          q(
              "PAIR_99_VS_ACE",
              PracticeTestCategory.PAIR,
              true,
              Rank.ACE,
              Rank.NINE,
              Rank.NINE
          ),

          q(
              "PAIR_66_VS_7",
              PracticeTestCategory.PAIR,
              true,
              Rank.SEVEN,
              Rank.SIX,
              Rank.SIX
          ),

          q(
              "PAIR_44_VS_5",
              PracticeTestCategory.PAIR,
              true,
              Rank.FIVE,
              Rank.FOUR,
              Rank.FOUR
          ),

          q(
              "PAIR_44_VS_6",
              PracticeTestCategory.PAIR,
              true,
              Rank.SIX,
              Rank.FOUR,
              Rank.FOUR
          ),

          q(
              "PAIR_AA_VS_10",
              PracticeTestCategory.PAIR,
              true,
              Rank.TEN,
              Rank.ACE,
              Rank.ACE
          ),

          q(
              "PAIR_1010_VS_6",
              PracticeTestCategory.PAIR,
              true,
              Rank.SIX,
              Rank.TEN,
              Rank.TEN
          ),

          q(
              "PAIR_77_VS_7",
              PracticeTestCategory.PAIR,
              true,
              Rank.SEVEN,
              Rank.SEVEN,
              Rank.SEVEN
          ),

          q(
              "PAIR_77_VS_8",
              PracticeTestCategory.PAIR,
              true,
              Rank.EIGHT,
              Rank.SEVEN,
              Rank.SEVEN
          ),

          q(
              "PAIR_66_VS_2",
              PracticeTestCategory.PAIR,
              true,
              Rank.TWO,
              Rank.SIX,
              Rank.SIX
          ),

          q(
              "PAIR_55_VS_6",
              PracticeTestCategory.PAIR,
              true,
              Rank.SIX,
              Rank.FIVE,
              Rank.FIVE
          ),

          q(
              "PAIR_33_VS_2",
              PracticeTestCategory.PAIR,
              true,
              Rank.TWO,
              Rank.THREE,
              Rank.THREE
          ),


          // ====================================================
          // CHALLENGING — SURRENDER
          // ====================================================

          q(
              "HARD_15_VS_ACE",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.ACE,
              Rank.TEN,
              Rank.FIVE
          ),

          q(
              "HARD_16_VS_9",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.NINE,
              Rank.TEN,
              Rank.SIX
          ),

          q(
              "HARD_16_VS_ACE",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.ACE,
              Rank.TEN,
              Rank.SIX
          ),

          q(
              "HARD_17_VS_ACE",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.ACE,
              Rank.TEN,
              Rank.SEVEN
          ),

          q(
              "PAIR_88_VS_ACE",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.ACE,
              Rank.EIGHT,
              Rank.EIGHT
          ),

          q(
              "PAIR_88_VS_10",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.TEN,
              Rank.EIGHT,
              Rank.EIGHT
          ),

          q(
              "HARD_15_VS_10",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.TEN,
              Rank.TEN,
              Rank.FIVE
          ),

          q(
              "HARD_16_VS_10",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.TEN,
              Rank.TEN,
              Rank.SIX
          ),

          q(
              "HARD_14_VS_10",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.TEN,
              Rank.TEN,
              Rank.FOUR
          ),

          q(
              "HARD_16_VS_8",
              PracticeTestCategory.SURRENDER,
              true,
              Rank.EIGHT,
              Rank.TEN,
              Rank.SIX
          ),


          // ====================================================
          // CONTROL QUESTIONS
          // ====================================================

          q(
              "HARD_20_VS_10",
              PracticeTestCategory.HARD,
              false,
              Rank.TEN,
              Rank.TEN,
              Rank.QUEEN
          ),

          q(
              "HARD_18_VS_9",
              PracticeTestCategory.HARD,
              false,
              Rank.NINE,
              Rank.TEN,
              Rank.EIGHT
          ),

          q(
              "HARD_13_VS_5",
              PracticeTestCategory.HARD,
              false,
              Rank.FIVE,
              Rank.TEN,
              Rank.THREE
          ),

          q(
              "HARD_12_VS_7",
              PracticeTestCategory.HARD,
              false,
              Rank.SEVEN,
              Rank.TEN,
              Rank.TWO
          ),

          q(
              "HARD_11_VS_6",
              PracticeTestCategory.HARD,
              false,
              Rank.SIX,
              Rank.SIX,
              Rank.FIVE
          ),

          q(
              "HARD_10_VS_9",
              PracticeTestCategory.HARD,
              false,
              Rank.NINE,
              Rank.SIX,
              Rank.FOUR
          ),

          q(
              "HARD_8_VS_10",
              PracticeTestCategory.HARD,
              false,
              Rank.TEN,
              Rank.FIVE,
              Rank.THREE
          ),

          q(
              "SOFT_A9_VS_10",
              PracticeTestCategory.SOFT,
              false,
              Rank.TEN,
              Rank.ACE,
              Rank.NINE
          ),

          q(
              "SOFT_A7_VS_5",
              PracticeTestCategory.SOFT,
              false,
              Rank.FIVE,
              Rank.ACE,
              Rank.SEVEN
          ),

          q(
              "SOFT_A5_VS_8",
              PracticeTestCategory.SOFT,
              false,
              Rank.EIGHT,
              Rank.ACE,
              Rank.FIVE
          ),

          q(
              "PAIR_AA_VS_8",
              PracticeTestCategory.PAIR,
              false,
              Rank.EIGHT,
              Rank.ACE,
              Rank.ACE
          ),

          q(
              "PAIR_1010_VS_9",
              PracticeTestCategory.PAIR,
              false,
              Rank.NINE,
              Rank.TEN,
              Rank.TEN
          ),

          q(
              "PAIR_88_VS_6",
              PracticeTestCategory.PAIR,
              false,
              Rank.SIX,
              Rank.EIGHT,
              Rank.EIGHT
          ),

          q(
              "PAIR_55_VS_9",
              PracticeTestCategory.PAIR,
              false,
              Rank.NINE,
              Rank.FIVE,
              Rank.FIVE
          ),

          q(
              "PAIR_22_VS_8",
              PracticeTestCategory.PAIR,
              false,
              Rank.EIGHT,
              Rank.TWO,
              Rank.TWO
          ),

          q(
              "HARD_14_VS_ACE",
              PracticeTestCategory.SURRENDER,
              false,
              Rank.ACE,
              Rank.TEN,
              Rank.FOUR
          )
      );


  public static List<PracticeTestQuestion>
      getQuestions()
  {
    return QUESTIONS;
  }


  public static List<PracticeTestQuestion>
      getChallengingQuestions()
  {
    List<PracticeTestQuestion> result =
        new ArrayList<>();


    for (PracticeTestQuestion question :
         QUESTIONS)
    {
      if (question.isChallenging())
      {
        result.add(
            question
        );
      }
    }


    return result;
  }


  public static List<PracticeTestQuestion>
      getChallengingQuestions(
          PracticeTestCategory category)
  {
    List<PracticeTestQuestion> result =
        new ArrayList<>();


    for (PracticeTestQuestion question :
         QUESTIONS)
    {
      if (question.isChallenging()
          && question.getCategory()
              == category)
      {
        result.add(
            question
        );
      }
    }


    return result;
  }


  public static List<PracticeTestQuestion>
      getControlQuestions()
  {
    List<PracticeTestQuestion> result =
        new ArrayList<>();


    for (PracticeTestQuestion question :
         QUESTIONS)
    {
      if (!question.isChallenging())
      {
        result.add(
            question
        );
      }
    }


    return result;
  }


  private static PracticeTestQuestion q(
      String id,
      PracticeTestCategory category,
      boolean challenging,
      Rank dealerRank,
      Rank... playerRanks)
  {
    return new PracticeTestQuestion(
        id,
        playerRanks,
        dealerRank,
        category,
        challenging
    );
  }
}
