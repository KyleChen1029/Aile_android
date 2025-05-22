package tw.com.chainsea.ce.sdk.http.ce.model;

public @interface LastMessageType {
    @interface ContentType{
        String AT = "At"; //標註訊息
        String VIDEO = "Video"; //影片
        String LOCATION = "Location"; //地理位置
        String BUSINESS = "Business"; //物件
        String CALL = "Call"; //通話
        String FILE = "File"; //檔案
        String VOICE = "Voice"; //錄音
        String IMAGE = "Image"; //圖片
        String AD = "Ad"; //廣告
        String STICKER = "Sticker"; //表情貼圖
        String TEXT = "Text"; //文字訊息
        String Template = "Template"; //卡片訊息
    }

    @interface ObjectType{
        String USER = "User";
        String ALL = "All";
    }

    @interface Flag{
        int SELF = -1; //當前登入者發的
        int SEND = 0; //已發送
        int ARRIVE = 1; //已到
        int READ = 2; //已讀
        int RECYCLE = 3; //回收
    }

    @interface From{
        String FB = "facebook";
        String CE = "ce";
        String LINE = "line";
    }

    @interface SourceType{
        String USER = "User";
        String SYSTEM = "System"; //系統訊息，系統告知使用者的訊息
        String LOGIN = "Login"; //登入訊息，電腦版Client登入的通知
        String BROADCAST = "Broadcast"; //廣播功能
        String SATISFACTION = "Satisfaction"; //滿意度調查
        String CANNED_LANGUAGE = "CannedLanguage"; //罐頭語
    }
}
