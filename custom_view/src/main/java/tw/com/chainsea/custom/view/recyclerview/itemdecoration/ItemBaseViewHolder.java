package tw.com.chainsea.custom.view.recyclerview.itemdecoration;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

/**
 * current by evan on 2020-04-01
 *
 * @author Evan Wang
 * date 2020-04-01
 */
public abstract class ItemBaseViewHolder<T> extends RecyclerView.ViewHolder {
    private View rightMenu;
    private View leftMenu;
    private View vContentItem;

    public ItemBaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    protected void setMenuViews(View leftMenu, View rightMenu) {
        this.rightMenu = rightMenu;
        this.leftMenu = leftMenu;
        if (this.rightMenu != null && this.leftMenu != null) {
            this.rightMenu.setAlpha(0.0f);
            this.leftMenu.setAlpha(0.0f);
        }
    }

    protected void setContentItemView(View vContentItem) {
        this.vContentItem = vContentItem;
    }

    public View getRightMenu() {
        if (this.rightMenu == null) {
            throw new RuntimeException("the Right Menu Cannot Be Null");
        }
        return this.rightMenu;
    }

    public View getLeftMenu() {
        if (this.leftMenu == null) {
            throw new RuntimeException("the Left Menu Cannot Be Null");
        }
        return this.leftMenu;
    }

    public View getContentItem() {
        if (this.vContentItem == null) {
//            throw new RuntimeException("the Content Item View Cannot Be Null");
            return null;
        }
        return vContentItem;
    }


    public void onBind(T t, int section, int position) {
    }
}
