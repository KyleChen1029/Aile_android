package tw.com.chainsea.android.common.multimedia;

import java.util.Objects;

/**
 * current by evan on 2020-06-04
 *
 * @author Evan Wang
 * date 2020-06-04
 */
public class ImageBean extends AMediaBean {
    private static final long serialVersionUID = 9199135968787507316L;

    private int _id;
    private String name;
    private String type;
    private long size;

    public ImageBean(int _id, String name, String type, long size) {
        this._id = _id;
        this.name = name;
        this.type = type;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;
        ImageBean bean = (ImageBean) o;
        return _id == bean._id && Objects.equals(getPath(), bean.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _id, getPath());
    }

    @Override
    public double getWeights() {
        return getDate();
    }

    @Override
    public String getFileName() {
        return this.name;
    }

    @Override
    public int getId() {
        return this._id;
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
