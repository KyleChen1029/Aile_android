package tw.com.chainsea.ce.sdk.reference;

/**
 * current by evan on 2020-08-20
 *
 * @author Evan Wang
 * @date 2020-08-20
 */
public abstract class AbsReference {

    protected static <T> String concatStrings(String placeholder, String separator, T... elements) {
        StringBuilder builder = new StringBuilder();
        if (elements.length <= 0) {
            return builder.toString();
        }
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                builder.append(separator);
            }
            builder.append(placeholder).append(elements[i]).append(placeholder);
        }
        return builder.toString();
    }
}
