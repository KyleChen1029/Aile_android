package tw.com.chainsea.chat.messagekit.lib;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import tw.com.chainsea.chat.util.DownloadUtil;
import tw.com.chainsea.custom.view.alert.AlertView;


public class FileUtil {
    private static String m_sCurrentPath = Global.ROOT_PATH;
    private static final String dirName = "chat";

    public static int getFileType(String a_FilePath) {
        File l_File = new File(a_FilePath);
        if (l_File.isDirectory()) {
            return Global.FileType_Dir;
        }
        String l_FileName = l_File.getName();
        String lo_fireName = l_FileName.toLowerCase();
        for (int i = 0; i < Global.FileTypes.length; i++) {
            int j = checkStringEnds(lo_fireName, Global.FileTypes[i]);
            if (j == -1) {
                continue;
            }
            return Global.TypeStart[i] + j;
        }
        return Global.FileType_Other;
    }


    public static int getFileType(File l_File) {
        if (l_File.isDirectory()) {
            return Global.FileType_Dir;
        }
        String l_FileName = l_File.getName();
        String lo_fireName = l_FileName.toLowerCase();
        for (int i = 0; i < Global.FileTypes.length; i++) {
            int j = checkStringEnds(lo_fireName, Global.FileTypes[i]);
            if (j == -1) {
                continue;
            }
            return Global.TypeStart[i] + j;
        }
        return Global.FileType_Other;
    }

//    public static int getNoDirType(String fileName) {
//        for (int i = 0; i < Global.FileTypes.length; i++) {
//            int j = checkStringEnds(fileName, Global.FileTypes[i]);
//            if (j == -1) {
//                continue;
//            }
//            return Global.TypeStart[i] + j;
//        }
//        return Global.FileType_Other;
//    }

//    public static void sortByName(List<String> a_FileNames) {
//        Collections.sort(a_FileNames);
//    }

    public static void sortByType(List<String> a_FileNames) {
        List<String> Res = new LinkedList<>();
        Collections.sort(a_FileNames);
        // Dir
        for (int i = 0; i < a_FileNames.size(); i++) {
            if (getFileType(a_FileNames.get(i)) == Global.FileType_Dir) {
                Res.add(a_FileNames.get(i));
            }
        }
        for (int j = 0; j < Global.FileTypes.length; j++) {
            for (int k = 0; k < Global.FileTypes[j].length; k++) {
                for (int i = 0; i < a_FileNames.size(); i++) {
                    if (a_FileNames.get(i).endsWith(Global.FileTypes[j][k])) {
                        Res.add(a_FileNames.get(i));
                    }
                }
            }
        }

        for (int i = 0; i < a_FileNames.size(); i++) {
            if (getFileType(a_FileNames.get(i)) == Global.FileType_Other) {
                Res.add(a_FileNames.get(i));
            }
        }

        a_FileNames.clear();

        a_FileNames.addAll(Res);
    }

//    public static boolean removeFile(String a_FileName) {
//        File l_File = new File(a_FileName);
//        if (l_File.exists()) {
//
//            if (l_File.isDirectory()) {
//                File[] l_Files = l_File.listFiles();
//                for (int i = 0; i < l_Files.length; i++) {
//                    removeFile(l_Files[i].getAbsolutePath());
//                }
//            }
//
//            return l_File.delete();
//        }
//
//        return false;
//    }

    public static void openFile(String a_FileName, Context ctx) throws Exception {
        File l_File = new File(a_FileName);
        if (l_File.isFile()) {
            Intent intent = null;
            if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexImage])) {
                intent = MToolBox.getImageFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexWebText])) {
                intent = MToolBox.getHtmlFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexPackage])) {
                intent = MToolBox.getApkFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexAudio])) {
                intent = MToolBox.getAudioFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexVideo])) {
                intent = MToolBox.getVideoFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexText])) {
                intent = MToolBox.getTextFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexPDF])) {
                intent = MToolBox.getPdfFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexWord])) {
                intent = MToolBox.getWordFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexExcel])) {
                intent = MToolBox.getExcelFileIntent(l_File, ctx);
            } else if (checkEndsWithInStringArray(a_FileName, Global.FileTypes[Global.IndexPPT])) {
                intent = MToolBox.getPPTFileIntent(l_File, ctx);
            } else {
                new AlertView.Builder()
                        .setStyle(AlertView.Style.Alert)
                        .setContext(ctx)
//                        .setTitle("Warning")
                        .setMessage("未發現打開此文件的應用")
                        .setDestructive("確定")
                        .build()
                        .setCancelable(true)
                        .show();
            }
            if (intent != null) {
                try {
//                    intent = Intent.createChooser(intent, a_Context.getString(R.string.file_open_failed));
                    ctx.startActivity(intent);
                } catch (Exception e) {
                    throw new RuntimeException("open file failed");
                }
            } else {
                throw new RuntimeException("open file failed & intent == Null");
            }
        }

//        return false;
    }

    public static String getCurPath() {
        return m_sCurrentPath;
    }

    public static boolean setCurPath(String a_sPath) {
        File t_File = new File(a_sPath);

        if (t_File.isDirectory()) {
            m_sCurrentPath = t_File.getPath();
            return true;
        }
        return false;
    }

    public static String getParentPath() {
        File t_File = new File(m_sCurrentPath);
        if (m_sCurrentPath.equals("/")) {
            return "/";
        }
        return t_File.getParent();
    }

    public static List<String> getCurDirFileNames() {
        File t_File = new File(m_sCurrentPath);
        File[] t_Files = t_File.listFiles();
        List<String> l_FileNames = new LinkedList<>();

        if (t_Files == null) {
            return null;
        }

        for (File t_File1 : t_Files) {
            l_FileNames.add(t_File1.getAbsolutePath());
        }
        sortByType(l_FileNames);

        return l_FileNames;
    }

    public static File getCacheFile(String fileName, String packageName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            String cacheDirPath = "/Android/data/" + packageName + "/cache/";
            File cacheFileDir = new File(DownloadUtil.INSTANCE.getDownloadFileDir() + cacheDirPath + File.separator + dirName);
            if (!cacheFileDir.exists()) {
               boolean isMk = cacheFileDir.mkdir();
            }
            return new File(cacheFileDir, fileName);
        } else {
            return null;
        }
    }

//    private static File getDiskCacheDir(String packageName, String uniqueName) {
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            String cacheDir = "/Android/data/" + packageName + "/cache/";
//            return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir + File.separator + uniqueName);
//        } else {
//            CELog.e("External storage was not mounted.");
//            return null;
//        }
//    }

//    public static byte[] file2byte(String filePath) {
//        byte[] buffer = null;
//        try {
//            File file = new File(filePath);
//            FileInputStream fis = new FileInputStream(file);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            byte[] b = new byte[1024];
//            int n;
//            while ((n = fis.read(b)) != -1) {
//                bos.write(b, 0, n);
//            }
//            fis.close();
//            bos.close();
//            buffer = bos.toByteArray();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return buffer;
//    }

//    public static long getFileSize(File file) throws Exception {
//        long size = 0;
//        if (file.exists()) {
//            FileInputStream fis = null;
//            fis = new FileInputStream(file);
//            size = fis.available();
//        } else {
//
//            Log.e("获取文件大小", "文件不存在!");
//        }
//        return size;
//    }

//    public static void renameFile(String a_FileName, String a_NewName) {
//        File l_File = new File(a_FileName);
//        File l_NewFile = new File(a_NewName);
//        l_File.renameTo(l_NewFile);
//    }

//    public static boolean copyFile(String a_FilePath, String a_DestPath, ProgressBar a_ProgressBar) {
//
//        File l_File = new File(a_FilePath);
//        if (l_File.getParent().equals(a_DestPath)) {
//            return false;
//        }
//        String l_FileName = l_File.getName();
//
//        File l_File2 = new File(a_DestPath + "/" + l_FileName);
//
//        long l_FileLen = l_File.length();
//
//        if (l_File.isDirectory()) {
//            File[] l_Files = l_File.listFiles();
//            l_File2.mkdirs();
//
//            for (int i = 0; i < l_Files.length; i++) {
//                boolean l_Rc = copyFile(l_Files[i].getAbsolutePath(),
//                        l_File2.getAbsolutePath(), a_ProgressBar);
//                if (!l_Rc) {
//                    return false;
//                }
//            }
//            return true;
//        }
//        if (l_File2.exists()) {
//            return true;
//        }
//        try {
//            FileInputStream fis = new FileInputStream(l_File);
//            FileOutputStream fos = new FileOutputStream(l_File2);
//
//            byte[] l_Buffer = new byte[1024 * 1024];
//
//            l_FileLen = l_FileLen / 1024;
//            int max = (int) (l_FileLen / 1024);
//
//            a_ProgressBar.setMax(max);
//
//            int len = 0;
//            int count = 0;
//
//            while (len != -1) {
//                count++;
//                len = fis.read(l_Buffer);
//                fos.write(l_Buffer);
//                a_ProgressBar.setProgress(count);
//            }
//            fis.close();
//            fos.close();
//
//        } catch (IOException e) {
//            return false;
//        }
//        return true;
//    }


    private static boolean checkEndsWithInStringArray(String a_CheckItem, String[] a_Array) {
        for (String l_Item : a_Array) {
            if (a_CheckItem.endsWith(l_Item)) {
                return true;
            }
        }
        return false;
    }

    private static int checkStringEnds(String a_CheckItem, String[] a_Array) {
        for (int i = 0; i < a_Array.length; i++) {
            if (a_CheckItem.endsWith(a_Array[i])) {
                return i;
            }
        }
        return -1;
    }

}
