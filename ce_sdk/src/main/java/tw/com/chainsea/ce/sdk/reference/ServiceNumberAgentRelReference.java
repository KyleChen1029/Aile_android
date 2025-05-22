package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import tw.com.chainsea.android.common.log.CELog;
import tw.com.chainsea.ce.sdk.bean.servicenumber.ServiceNumberEntity;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;
import tw.com.chainsea.ce.sdk.http.ce.model.Member;

/**
 * current by evan on 12/9/20
 *
 * @author Evan Wang
 * @date 12/9/20
 */
public class ServiceNumberAgentRelReference extends AbsReference {

    public static boolean saveAgentsRelByServiceNumber(SQLiteDatabase db, ServiceNumberEntity entity) {
        try {
            assert entity != null;
            String serviceNumberId = entity.getServiceNumberId();
            String broadcastRoomId = entity.getBroadcastRoomId();

            List<Member> entities = entity.getMemberItems();
            assert entities != null;

            String whereClause = DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID + "=?";
            String[] whereArgs = new String[]{entity.getServiceNumberId()};
            boolean status = (db == null ? DBManager.getInstance().openDatabase() : db).delete(DBContract.ServiceNumberAgentRel.TABLE_NAME, whereClause, whereArgs) > 0;
            CELog.d("delete service number agent rel by :: " + entity.getServiceNumberId() + " , status : " + status);

            boolean result = true;
            ContentValues values = new ContentValues();
            for (Member profile : entities) {
                values.put(DBContract.ServiceNumberAgentRel._ID, serviceNumberId + profile.getId());
                values.put(DBContract.ServiceNumberAgentRel.COLUMN_SERVICE_NUMBER_ID, serviceNumberId);
                values.put(DBContract.ServiceNumberAgentRel.COLUMN_BROADCAST_ROOM_ID, broadcastRoomId);
                values.put(DBContract.ServiceNumberAgentRel.COLUMN_AGENT_ID, profile.getId());
                values.put(DBContract.ServiceNumberAgentRel.COLUMN_AGENT_PRIVILEGE, profile.getPrivilege().getType());
                long _id = (db == null ? DBManager.getInstance().openDatabase() : db).replace(DBContract.ServiceNumberAgentRel.TABLE_NAME, null, values);
                result = result && _id > 0;
            }
            return result;
        } catch (Exception e) {
            CELog.e(e.getMessage());
            return false;
        }
    }
}
