
public class StringWrapper {
    private String value;
    
    public StringWrapper(String value) {
        this.value = value;
    }
    
    public static StringWrapper of(String value) {
        return new StringWrapper(value);
    }
    
    public boolean isAny(String... values) {
        if (value == null) return false;
        for (String val : values) {
            if (value.equals(val)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isAnyIgnoreCase(String... values) {
        if (value == null) return false;
        for (String val : values) {
            if (value.equalsIgnoreCase(val)) {
                return true;
            }
        }
        return false;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
