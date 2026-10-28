public interface CountingSystem
{
  void reset();

  void observe(
      Card card
  );

  double getSignal(
      int remainingCards
  );

  String getName();
}
