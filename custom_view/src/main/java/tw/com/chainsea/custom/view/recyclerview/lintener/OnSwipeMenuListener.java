package tw.com.chainsea.custom.view.recyclerview.lintener;

/**
 * current by evan on 2020-06-02
 *
 * @author Evan Wang
 * @date 2020-06-02
 */
public interface OnSwipeMenuListener<CR, TYPE extends Enum> {
    void onSwipeMenuClick(CR cr, TYPE where, int position);
}
