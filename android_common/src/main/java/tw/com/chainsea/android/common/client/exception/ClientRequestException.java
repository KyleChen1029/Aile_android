package tw.com.chainsea.android.common.client.exception;


/**
 * ClientRequestException 連線例外類
 *
 * @author Evan Wang
 * @version 0.0.1
 * @since 0.0.1
 */
public class ClientRequestException extends RuntimeException {
    private static final long serialVersionUID = -2402989187626658065L;

    public ClientRequestException() {
    }

    public ClientRequestException(String s) {
        super(s);
    }

    public ClientRequestException(Throwable throwable) {
        super(throwable);
    }

    public ClientRequestException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
