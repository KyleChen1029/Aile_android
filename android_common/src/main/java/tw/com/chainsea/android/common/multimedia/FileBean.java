package tw.com.chainsea.android.common.multimedia;

import java.io.Serializable;
/**
 * current by evan on 2020-06-04
 *
 * @author Evan Wang
 * date 2020-06-04
 */

public class FileBean extends AMediaBean implements Serializable {

    public int _id;
    private String name;

    private long size;
    private long addedDate;
    private long updateDate;

    public FileBean(int _id, String name, long size, long addedDate, long updateDate) {
        this._id = _id;
        this.name = name;
        this.size = size;
        this.addedDate = addedDate;
        this.updateDate = updateDate;
    }

    @Override
    public double getWeights() {
        return updateDate;
    }

    @Override
    public  String getFileName() {
        return this.name;
    }

    @Override
    public int getId() {
        return this._id;
    }


    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public long getAddedDate() {
        return addedDate;
    }

    public long getUpdateDate() {
        return updateDate;
    }
}
