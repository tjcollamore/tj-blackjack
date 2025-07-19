import javafx.scene.control.TextArea;

public class TextAreaOutputter implements IOutputter
{
  private TextArea textArea;

  public TextAreaOutputter(TextArea textArea)
  {
    this.textArea = textArea;
  }
  
  public void output(String text)
  {
    textArea.appendText(text + "\n");
  }
}