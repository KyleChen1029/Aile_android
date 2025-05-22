package tw.com.chainsea.custom.view.layout.refresh.view;

/**
 * current by evan on 2019-12-26
 */
public interface IRefreshView {
    void stop();

    void doRefresh();

    void onPull(int offset, int total, int overPull);
}
