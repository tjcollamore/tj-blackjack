public class ChipPile
{
  private final WagerChip chip;
  private final int count;

  public ChipPile(
      WagerChip chip,
      int count)
  {
    this.chip = chip;
    this.count = count;
  }

  public WagerChip getChip()
  {
    return chip;
  }

  public int getCount()
  {
    return count;
  }
}
