package tw.com.chainsea.chat.messagekit.main.viewholder;

public @interface Constant {
    @interface Orientation{
        String VERTICAL = "Vertical";
        String HORIZONTAL = "Horizontal";
    }
    @interface ActionType{
        String POSTBACK = "Postback";
        String URL = "url";
        String AIFF = "aiff";
        String LOCATION = "location";
        String MESSAGE = "message";
        String PAGE = "page";
        String LINK = "link";
    }
}
