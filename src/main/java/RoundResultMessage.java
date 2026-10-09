public class RoundResultMessage
{
  private String message;
  private MessageTone tone;

  public RoundResultMessage(
      String message,
      MessageTone tone)
  {
    this.message = message;
    this.tone = tone;
  }

  public String getMessage()
  {
    return message;
  }

  public MessageTone getTone()
  {
    return tone;
  }
}
