import java.util.EnumSet;

public class PlayerDecisionContext
{
  private final Hand playerHand;
  private final Card dealerUpCard;
  private final GameSettings settings;
  private final EnumSet<HandAction> allowedActions;
  private final EnumSet<HandAction> availableActions;
  private final boolean cameFromSplit;
  private final int deckCount;
  private final double wagerAmount;
  
  public PlayerDecisionContext(
      Hand playerHand,
      Card dealerUpCard,
      GameSettings settings,
      EnumSet<HandAction> allowedActions,
      EnumSet<HandAction> availableActions,
      boolean cameFromSplit,
      int deckCount,
      double wagerAmount)
  {
    this.playerHand = playerHand;
    this.dealerUpCard = dealerUpCard;
    this.settings = settings;
    this.allowedActions = EnumSet.copyOf(allowedActions);
    this.availableActions = EnumSet.copyOf(availableActions);
    this.cameFromSplit = cameFromSplit;
    this.deckCount = deckCount;
    this.wagerAmount = wagerAmount;
  }
  
  public int getDeckCount()
  {
    return deckCount;
  }
  
  public boolean cameFromSplit()
  {
    return cameFromSplit;
  }
  
  public Hand getPlayerHand()
  {
    return playerHand;
  }

  public Card getDealerUpCard()
  {
    return dealerUpCard;
  }

  public GameSettings getSettings()
  {
    return settings;
  }
  
  public double getWagerAmount()
  {
    return wagerAmount;
  }

  public boolean isAllowed(HandAction action)
  {
    return allowedActions.contains(action);
  }
  
  public boolean isAvailable(HandAction action)
  {
    return availableActions.contains(action);
  }

  public EnumSet<HandAction> getAllowedActions()
  {
    return EnumSet.copyOf(allowedActions);
  }

  public EnumSet<HandAction> getAvailableActions()
  {
    return EnumSet.copyOf(availableActions);
  }
}
