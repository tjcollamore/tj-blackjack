
public class ChoiceResult
{
    private String value;
    
    public ChoiceResult(String value)
    {
        this.value = value;
    }
    
    public static ChoiceResult isResultOf(String value)
    {
        return new ChoiceResult(value);
    }
    
    public boolean anyOf(String... values) {
        if (value == null) return false;
        for (String val : values) {
            if (value.equals(val)) {
                return true;
            }
        }
        return false;
    }
}