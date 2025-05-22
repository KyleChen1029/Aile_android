package tw.com.chainsea.android.common.multimedia;

import android.graphics.Bitmap;

import com.google.common.collect.ComparisonChain;

import java.io.Serializable;

/**
 * current by evan on 2020-06-04
 *
 * @author Evan Wang
 * date 2020-06-04
 */
public abstract class AMediaBean implements IMediaBean, Serializable, Comparable<AMediaBean> {
    private String path;
    private Bitmap thumbnailBitmap;

    private long date;
    private String folderName;

    private int position;
    private int selectPosition;

    public abstract double getWeights();

    public abstract String getFileName();

    public abstract int getId();

    @Override
    public int compareTo(AMediaBean o) {
        return ComparisonChain.start()
                .compare(o.getWeights(), this.getWeights())
                .result();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getThumbnailBitmap() {
        return thumbnailBitmap;
    }

    public void setThumbnailBitmap(Bitmap thumbnailBitmap) {
        this.thumbnailBitmap = thumbnailBitmap;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getSelectPosition() {
        return selectPosition;
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
    }
}
