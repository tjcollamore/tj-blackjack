public class StrategyGuidanceFactory
{
  public static StrategyGuidanceProvider create(
      StrategyMode mode)
  {
    if (mode == StrategyMode.SOCIAL)
      return new SocialStrategyGuidance();

    return new BasicStrategyGuidance();
  }
}
