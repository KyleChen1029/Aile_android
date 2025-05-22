package tw.com.chainsea.custom.view.layout.refresh.listener;

/**
 * current by evan on 2019-12-26
 */
public interface OnPullListener {
    void onMoveTarget(int offset);

    void onMoveRefreshView(int offset);

    void onRefresh();
}
