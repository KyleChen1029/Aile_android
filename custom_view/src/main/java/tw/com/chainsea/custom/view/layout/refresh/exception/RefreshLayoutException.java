package tw.com.chainsea.custom.view.layout.refresh.exception;

/**
 * current by evan on 2019-12-26
 *
 */
public class RefreshLayoutException  extends RuntimeException {

    private static final long serialVersionUID = 3240155297977889794L;

    public RefreshLayoutException() {
    }

    public RefreshLayoutException(String s) {
        super(s);
    }

    public RefreshLayoutException(Throwable throwable) {
        super(throwable);
    }

    public RefreshLayoutException(String s, Throwable throwable) {
        super(s, throwable);
    }
}
