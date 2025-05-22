package tw.com.chainsea.android.common.multimedia;

/**
 * current by evan on 2020-06-04
 *
 * @author Evan Wang
 * date 2020-06-04
 */
public class VideoBean extends AMediaBean {
    public int _id;
    public String name;
    public String type;
    public long size;
    //public long date;
    //public String path;
    //public String folderName;

    //public int position;
    //public int selectPosition;

    public VideoBean(int _id, String name, String type, long size, long date, String path, String folderName) {
        this._id = _id;
        this.name = name;
        this.type = type;
        this.size = size;
        //this.date = date;
//        this.path = path;
//        this.folderName = folderName;
    }

    public long getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
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


}
