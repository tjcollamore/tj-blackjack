public class ConsoleOutputter implements IOutputter
{
  public void output(String text)
  {
    System.out.println(text);
  }
}