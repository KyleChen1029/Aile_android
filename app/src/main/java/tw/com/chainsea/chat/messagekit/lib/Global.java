package tw.com.chainsea.chat.messagekit.lib;

public class Global {
    public static final String ROOT_PATH = "/";

    public static final String[][] FileTypes = {
            // File image
            {".png", ".jpg", ".jpeg", ".gif", ".bmp"},
            // File Audio
            {".mp3", ".wav", ".ogg", ".midi", ".m4a", ".aac"},
            // File Video
            {".mp4", ".rmvb", ".avi", ".flv", ".3gp", ".mov"},
            // File Web Text
            {".jsp", ".html", ".htm", ".js", ".php"},
            // File Text
            {".txt", ".c", ".cpp", ".xml", ".py", ".json", ".log"},
            // File Excel
            {".xls", ".xlsx"},
            // File Word
            {".doc", ".docx"},
            // File PPT
            {"ppt", ".pptx"},
            // File PDF
            {".pdf"},
            // File Package
            {".jar", ".zip", ".rar", ".gz", ".apk"}};

    public static final int[] TypeStart = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};

    public static final int IndexImage = 0;
    public static final int IndexAudio = 1;
    public static final int IndexVideo = 2;
    public static final int IndexWebText = 3;
    public static final int IndexText = 4;
    public static final int IndexExcel = 5;
    public static final int IndexWord = 6;
    public static final int IndexPPT = 7;
    public static final int IndexPDF = 8;
    public static final int IndexPackage = 9;

    public static final int FileType_Other = 0;
    public static final int FileType_Dir = 1;

    /**
     * 1.File TodoOverviewType: image
     */
    public static final int FileType_Png = 100;
    public static final int FileType_Jpg = 101;
    public static final int FileType_jpeg = 102;
    public static final int FileType_gif = 103;
    public static final int FileType_bmp = 104;

    /**
     * 1.File TodoOverviewType: Audio
     */
    public static final int FileType_mp3 = 200;
    public static final int FileType_wav = 201;
    public static final int FileType_ogg = 202;
    public static final int FileType_midi = 203;
    public static final int FileType_m4a = 204;
    public static final int FileType_aac = 205;

    /**
     * 3.File TodoOverviewType: Video
     */
    public static final int FileType_mp4 = 300;
    public static final int FileType_rmvb = 301;
    public static final int FileType_avi = 302;
    public static final int FileType_flv = 303;
    public static final int FileType_3gp = 304;
    public static final int FileType_mov = 305;

    /**
     * 4.File TodoOverviewType: Web Text
     */
    public static final int FileType_jsp = 400;
    public static final int FileType_html = 401;
    public static final int FileType_htm = 405;
    public static final int FileType_js = 406;
    public static final int FileType_php = 407;

    /**
     * 5.File TodoOverviewType:Text
     */
    public static final int FileType_txt = 500;
    public static final int FileType_c = 501;
    public static final int FileType_cpp = 502;
    public static final int FileType_xml = 503;
    public static final int FileType_py = 504;
    public static final int FileType_json = 505;
    public static final int FileType_log = 506;

    /**
     * 6.File TodoOverviewType:Excel
     */
    public static final int FileType_xls = 600;
    public static final int FileType_xlsx = 601;

    /**
     * 7.File TodoOverviewType:Word
     */
    public static final int FileType_doc = 700;
    public static final int FileType_docx = 701;

    /**
     * 8.File TodoOverviewType:PPT
     */
    public static final int FileType_ppt = 800;
    public static final int FileType_pptx = 801;
    /**
     * 9.File TodoOverviewType:PDF
     */
    public static final int FileType_pdf = 900;

    /**
     * 10.File TodoOverviewType:Package
     */
    public static final int FileType_jar = 1000;
    public static final int FileType_zip = 1001;
    public static final int FileType_rar = 1002;
    public static final int FileType_gz = 1003;
    public static final int FileType_apk = 1004;
}
