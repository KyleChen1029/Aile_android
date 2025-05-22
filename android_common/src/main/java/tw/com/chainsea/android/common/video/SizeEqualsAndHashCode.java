package tw.com.chainsea.android.common.video;

/**
 * current by evan on 2020-03-05
 */
public class SizeEqualsAndHashCode {
    private SizeEqualsAndHashCode() {
    }

    static boolean equals(IVideoSize a, Object o) {
        if (a == o) return true;
        if (o == null) return false;

        if (o instanceof IVideoSize) {
            IVideoSize b = (IVideoSize) o;
            if (a.width() != b.width()) return false;
            return a.height() == b.height();
        } else {
            return false;
        }
    }

    static int hashCode(IVideoSize size) {
        int result = size.width();
        result = 31 * result + size.height();
        return result;
    }


}