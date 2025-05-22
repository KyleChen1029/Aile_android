package tw.com.chainsea.chat.util;

import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
import tw.com.chainsea.ce.sdk.bean.room.ServiceNumberStatus;
import tw.com.chainsea.ce.sdk.database.sp.TokenPref;
import tw.com.chainsea.chat.App;
import tw.com.chainsea.chat.BuildConfig;

public class Unit {
    /**
     * 自己是商務號Owner&&未讀數0&&商務號online ==> 顯示N
     * 自己是商務號Owner&&未讀數0&&商務號offLine ==> 不顯示N也不顯示未讀數字
     * 未讀數非0 && 未讀數 < 99 ==> 顯示未讀數
     * 未讀數非0 && 未讀數 > 99 ==> 顯示99+
     */
    public static String getUnReadNumber(int unReadNumber){
        return getUnReadNumber(unReadNumber, 0, ServiceNumberStatus.UNDEF, false, false);
    }
    public static String getUnReadNumber(ChatRoomEntity t) {
        if (t == null) return null;
        else return getUnReadNumber(t.getUnReadNum(), t.getConsultSrcUnreadNumber(), t.getServiceNumberStatus(), t.isAtMe()
                , TokenPref.getInstance(App.getContext()).getUserId().equals(t.getOwnerId()));
    }
    public static String getUnReadNumber(int unreadNumber, int consultSrcUnreadNumber, ServiceNumberStatus serviceNumberStatus, boolean isAt, boolean isOwner) {
        if (unreadNumber == -1 && consultSrcUnreadNumber == 0) {
            return "1"; //手動設定未讀顯示1
        }
        int unReadNumber = Math.abs(unreadNumber);
        unReadNumber += Math.abs(consultSrcUnreadNumber);
        //無未讀數
        if(unReadNumber <= 0){
            if(ServiceNumberStatus.ON_LINE.equals(serviceNumberStatus)  //商務號進線
                    && isOwner //是商務號Owner
            ) {
                return "N";
            }else {
                return null;
            }
        }
        return getUnReadNumber(unReadNumber, isAt);
    }
    public static String getUnReadNumber(int unReadNumber, boolean isAt){
        if (unReadNumber > 99 && !BuildConfig.DEBUG) {
            return isAt ? "@ " + unReadNumber : String.valueOf(unReadNumber);
        } else {
            return (isAt ? "@ " : "") + unReadNumber;
        }
    }
}
