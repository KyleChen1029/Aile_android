package tw.com.chainsea.android.common.json;

public class JsonReaderException extends RuntimeException {
    private static final long serialVersionUID = 769016127652166568L;

    public JsonReaderException() {
    }

    public JsonReaderException(String s) {
        super(s);
    }

    public JsonReaderException(Throwable throwable) {
        super(throwable);
    }

    public JsonReaderException(String s, Throwable throwable) {
        super(s, throwable);
    }

}