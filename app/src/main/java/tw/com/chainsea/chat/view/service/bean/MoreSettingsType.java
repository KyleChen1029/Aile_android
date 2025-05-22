package tw.com.chainsea.chat.view.service.bean;
/**
 * current by evan on 2020-07-28
 *
 * @author Evan Wang
 * date 2020-07-28
 */
public enum MoreSettingsType {
    SERVICE_INFO(0,  Type.VIEW),
    DEPICTION_INPUT(1,  Type.INPUT),
    ENABLE(2,  Type.CUTOVER),
    MEMBERS(3,  Type.LIST),
    BROADCAST_MESSAGE(4,  Type.ACTION),
    UPGRADE_PROFESSIONAL(5,  Type.CUTOVER),
    INSIDE_SERVICE_NUMBER(6,  Type.CUTOVER),
    OUTSIDE_SERVICE_NUMBER(7,  Type.CUTOVER),
    WELCOME_MESSAGE(8,  Type.ACTION),
    POST_BACK_CALLBACK(9,  Type.ACTION),
    TIMEOUT_SETTING(10,  Type.ACTION);

    private int index;
    private Type type;

    MoreSettingsType(int index, Type type) {
        this.index = index;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public Type getType() {
        return type;
    }

    enum Type {
        VIEW,
        INPUT,
        ACTION,
        CUTOVER,
        LIST
    }
}
