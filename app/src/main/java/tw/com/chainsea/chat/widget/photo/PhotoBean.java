package tw.com.chainsea.chat.widget.photo;

import com.google.common.primitives.Longs;

import java.io.Serializable;

public class PhotoBean implements Serializable, Comparable<PhotoBean> {
    private static final long serialVersionUID = -2927420122757026007L;

    public int _id;
    public String name;
    public String path;
    public String folderName;
    public long size;
    public long date;
    public String type;

    public int position;
    public int selectPosition;

    @Override
    public int compareTo(PhotoBean o) {
        return Longs.compare(o.getDate(),this.date);
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
