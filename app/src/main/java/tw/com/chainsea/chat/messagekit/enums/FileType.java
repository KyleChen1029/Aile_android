package tw.com.chainsea.chat.messagekit.enums;

import androidx.annotation.DrawableRes;

import com.google.common.collect.Lists;

import java.util.List;

import tw.com.chainsea.chat.R;

/**
 * current by evan on 2020-01-07
 */
public enum FileType {
    AUDIO(R.drawable.file_message_icon_file, "", Lists.newArrayList("mp3", "wav", "ogg", "midi")),
    VIDEO(R.drawable.file_message_icon_file, "", Lists.newArrayList("avi", "mpeg", "ogv", "webm", "3gp", "3g2", "mp4")),
    WEB(R.drawable.file_message_icon_file, "", Lists.newArrayList("jsp", "html", "htm", "js", "php")),
    TEXT(R.drawable.file_message_icon_file, "", Lists.newArrayList("txt", "c", "cpp", "xml", "py", "json", "log")),
    EXCEL(R.drawable.ic_file_icon_excel_61dp, "", Lists.newArrayList("xls", "xlsx")),
    WORD(R.drawable.ic_file_icon_word_61dp, "", Lists.newArrayList("doc", "docx")),
    PPT(R.drawable.ic_icon_powerpoint, "", Lists.newArrayList("ppt", "pptx")),
    PDF(R.drawable.ic_file_icon_pdf_61dp, "", Lists.newArrayList("pdf")),
    FILE(R.drawable.file_message_icon_file, "", Lists.newArrayList("jar", "zip", "rar", "gz", "apk")),
    IMAGE(R.drawable.file_message_icon_file, "", Lists.newArrayList("jpg", "jpeg", "png", "gif", "bmp", "tiff")),
    NONE(R.drawable.file_message_icon_file, "", Lists.newArrayList());

    @DrawableRes
    private int drawable;
    private String name;
    private List<String> types;

    FileType(int drawable, String name, List<String> types) {
        this.drawable = drawable;
        this.name = name;
        this.types = types;
    }

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public static FileType of(String type) {
        if (type.startsWith(".")) {
            type = type.replaceAll("\\p{Punct}", "");
        }
        for (FileType ft : values()) {
            for (String t : ft.types) {
                if (t.toUpperCase().equals(type.toUpperCase())) {
                    return ft;
                }
            }
        }
        return NONE;
    }
}