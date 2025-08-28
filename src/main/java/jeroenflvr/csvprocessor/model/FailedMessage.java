package jeroenflvr.csvprocessor.model;
import java.io.Serializable;

public class FailedMessage implements Serializable {
    private String key;
    private String value;
    private String error;

    public FailedMessage() {}
    public FailedMessage(String key, String value, String error) {
        this.key = key;
        this.value = value;
        this.error = error;
    }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
