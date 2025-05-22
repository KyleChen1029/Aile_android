package tw.com.chainsea.chat.keyboard.media.adatper;

import android.graphics.Canvas;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

/**
 * current by evan on 2020-06-01
 *
 * @author Evan Wang
 * @date 2020-06-01
 */
public class PicItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public PicItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        int count = layoutManager.getItemCount();
        int position = parent.getChildAdapterPosition(view);

        outRect.left = (int) (((float) (count - position)) / count * this.space);
        outRect.right = (int) (((float) this.space * (count + 1) / count) - outRect.left);
    }
}
