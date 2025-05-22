package tw.com.chainsea.custom.view.alert;

/**
 * current by evan on 2020-02-05
 */
public interface OnLeftViewClickListener<A extends AlertView , T> {
    void onLeftClick(A a, T t);
}
