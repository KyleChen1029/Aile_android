package tw.com.chainsea.chat.ui.adapter.entity;

/**
 * FileModel
 * Created by 90Chris on 2015/3/12.
 */
public class FileDisData {
    String filePath;
    boolean isChecked;

    public FileDisData(String filePath, boolean isChecked) {
        this.filePath = filePath;
        this.isChecked = isChecked;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
