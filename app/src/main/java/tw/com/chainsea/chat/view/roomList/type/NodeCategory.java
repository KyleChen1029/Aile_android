package tw.com.chainsea.chat.view.roomList.type;

import androidx.recyclerview.widget.RecyclerView;
/**
 * Create by evan on 1/7/21
 *
 * @author Evan Wang
 * date 1/7/21
 */
public enum NodeCategory {
    GENERAL_NODE(101, 20),
    GENERAL_PARENT_NODE(102, 0),
    SUBSCRIBE_PARENT_NOD(103, 0),
    CHILD_NODE(104, 0),
    SUBSCRIBE_NODE(105, 20),
    BOSS_NODE(106, 10),
    SERVICE_CHILD_NODE(107, 0),
    SERVICE_NODE(108, 10),
    BUSINESS_NODE(109, 10);

    private int viewType;
    private int poolSize;

    public static NodeCategory of(int type) {
        for (NodeCategory n : values()) {
            if (n.getViewType() == type) {
                return n;
            }
        }
        return GENERAL_NODE;
    }

    public static void setMaxRecycledViews(RecyclerView.RecycledViewPool pool) {
        for (NodeCategory n : values()) {
            pool.setMaxRecycledViews(n.viewType, n.poolSize);
        }
    }

    NodeCategory(int viewType, int poolSize) {
        this.viewType = viewType;
        this.poolSize = poolSize;
    }

    public int getViewType() {
        return viewType;
    }

    public int getPoolSize() {
        return poolSize;
    }
}