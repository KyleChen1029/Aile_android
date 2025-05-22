package tw.com.chainsea.chat.messagekit.lib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.android.common.video.IVideoSize;
import tw.com.chainsea.android.common.video.VideoSizeFromVideoFile;
import tw.com.chainsea.chat.R;
import tw.com.chainsea.chat.view.movie.MovieActivity;

public class MToolBox {
    // array clear

    // get image resource id
    public static int getFileImage(int a_FileType) {
        switch (a_FileType) {

            case Global.FileType_Dir:
                return R.drawable.dir;

            case Global.FileType_Png:
            case Global.FileType_Jpg:
            case Global.FileType_jpeg:
            case Global.FileType_bmp:
                return R.drawable.image;

            case Global.FileType_gif:
                return R.drawable.gif;

            case Global.FileType_mp3:
            case Global.FileType_wav:
            case Global.FileType_ogg:
            case Global.FileType_midi:
                return R.drawable.audio;

            case Global.FileType_mp4:
            case Global.FileType_rmvb:
            case Global.FileType_avi:
            case Global.FileType_3gp:
                return R.drawable.vedio;
            case Global.FileType_flv:
                return R.drawable.flv;

            case Global.FileType_jsp:
            case Global.FileType_htm:
            case Global.FileType_php:
                return R.drawable.web;

            case Global.FileType_js:
                return R.drawable.js;

            case Global.FileType_html:
                return R.drawable.html;

            case Global.FileType_c:
                return R.drawable.c;
            case Global.FileType_cpp:
                return R.drawable.cpp;

            case Global.FileType_txt:
            case Global.FileType_xml:
            case Global.FileType_py:
            case Global.FileType_json:
            case Global.FileType_log:
                return R.drawable.text;

            case Global.FileType_xls:
            case Global.FileType_xlsx:
                return R.drawable.excel;

            case Global.FileType_doc:
                return R.drawable.doc;
            case Global.FileType_docx:
                return R.drawable.docx;

            case Global.FileType_ppt:
            case Global.FileType_pptx:
                return R.drawable.ppt;
            case Global.FileType_pdf:
                return R.drawable.pdf;

            case Global.FileType_jar:
                return R.drawable.jar;
            case Global.FileType_zip:
                return R.drawable.zip;
            case Global.FileType_rar:
                return R.drawable.rar;
            case Global.FileType_gz:
                return R.drawable.gz;
            case Global.FileType_apk:
                return R.drawable.apk;

            default:
                return R.drawable.other_file;
        }
    }

    // get sd card path
    public static String getSdcardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * create open file intent
     */
    public static Intent getHtmlFileIntent(File file, Context a_Context) throws Exception {
		/*if (Build.VERSION.SDK_INT >= 24) {
			Uri photoURI = FileProvider.getUriForFile(, "tw.com.chainsea.chat.fileprovider", file);


		} else {

		}*/
        Uri uri = Uri.parse(file.toString()).buildUpon()
            .encodedAuthority("com.android.htmlfileprovider")
            .scheme("content").encodedPath(file.toString()).build();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(uri, "text/html");
        return intent;
    }

    public static Intent getImageFileIntent(File file, Context a_Context) throws Exception {
        Uri uri;
        {
            uri = FileProvider.getUriForFile(a_Context, a_Context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "image/*");
            return intent;
        }
    }

    public static Intent getPdfFileIntent(File file, Context a_Context) throws Exception {
        Uri uri;
        {
            uri = FileProvider.getUriForFile(a_Context, a_Context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "application/pdf");
            return intent;
        }
    }

    public static Intent getTextFileIntent(File file, Context a_Context) throws Exception {
        Uri uri;
        {
            uri = FileProvider.getUriForFile(a_Context, a_Context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "text/plain");
            return intent;
        }
    }

    public static Intent getAudioFileIntent(File file, Context a_Context) throws Exception {
        Uri uri;
        {
            uri = FileProvider.getUriForFile(a_Context, a_Context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("oneshot", 0);
            intent.putExtra("configchange", 0);
            intent.setDataAndType(uri, "audio/*");
            return intent;
        }
    }

    public static Intent getVideoFileIntent(File file, Context a_Context) throws Exception {
        IVideoSize iVideoSize = new VideoSizeFromVideoFile(file.getPath());
        return MovieActivity.getIntent(a_Context, file.getPath(), "", iVideoSize.width(), iVideoSize.height(), 0);
    }

    public static Intent getWordFileIntent(File file, Context a_Context) throws Exception {
        Uri uri;
        {
            uri = FileProvider.getUriForFile(a_Context, a_Context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "application/msword");
            return intent;
        }
    }

    public static Intent getExcelFileIntent(File file, Context a_Context) throws Exception {
        Uri uri;
        {
            uri = FileProvider.getUriForFile(a_Context, a_Context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.ms-excel");
            return intent;
        }
    }

    public static Intent getPPTFileIntent(File file, Context a_Context) throws Exception {
        Uri uri;
        {
            uri = FileProvider.getUriForFile(a_Context, a_Context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            return intent;
        }
    }

    public static Intent getApkFileIntent(File apkFile, Context a_Context) throws Exception {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        CELog.e("正在打开apk文件", "版本大于 N ，开始使用 fileProvider 进行安装");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri contentUri = FileProvider.getUriForFile(
            a_Context
            , a_Context.getPackageName() + ".fileprovider"
            , apkFile);
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        intent.setData(contentUri);
        return intent;
    }
}
