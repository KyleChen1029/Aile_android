//package tw.com.chainsea.ce.sdk.bean.todo;
//
//import android.content.ContentValues;
//import android.database.Cursor;
//
//import com.google.common.collect.ComparisonChain;
//import com.google.gson.annotations.SerializedName;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.Serializable;
//import java.math.BigInteger;
//import java.util.Calendar;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import tw.com.chainsea.android.common.json.JsonHelper;
//import tw.com.chainsea.ce.sdk.bean.Entity;
//import tw.com.chainsea.ce.sdk.bean.common.EnableType;
//import tw.com.chainsea.ce.sdk.bean.ProcessStatus;
//import tw.com.aile.sdk.bean.message.MessageEntity;
//import tw.com.chainsea.ce.sdk.bean.msg.Tools;
//import tw.com.chainsea.ce.sdk.bean.room.ChatRoomEntity;
//import tw.com.chainsea.ce.sdk.database.DBContract;
//
///**
// * current by evan on 2020-07-14
// *
// * @author Evan Wang
// * @date 2020-07-14
// */
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder(builderMethodName = "Build", toBuilder = true)
//public class TodoEntity implements Serializable, Comparable<TodoEntity>, Entity {
//    private static final long serialVersionUID = 8780661789029375195L;
//
//    @SerializedName(value = "id", alternate = {"todoId"})
//    String id; //Y	记事ID
//    String title; //Y	记事标题
//    @Builder.Default
//    TodoStatus status = TodoStatus.PROGRESS; //Y	记事状态
//    String userId; //Y	记事创建者
//    long createTime; //Y	记事创建者
//    long updateTime; //Y	记事最后更新时间
//    boolean openClock; //Y	是否开启了提醒
//    long remindTime; //N	提醒时间，开了提醒可用
//    String publicType; //Y	记事公开类型，默认为private
//    String roomId; //N	记事所关联的聊天室ID
//    String messageId;
//
//    // local
//    @Builder.Default
//    Type type = Type.MAIN;
//    ChatRoomEntity roomEntity;
//    MessageEntity messageEntity;
//
//    @Builder.Default
//    boolean isOutOfBounds = false;
//
//    @Override
//    public String getClassName() {
//        return this.getClass().getSimpleName();
//    }
//
//
//    // local
//    Calendar current;
//    // local judgment
//    @Builder.Default
//    ProcessStatus processStatus = ProcessStatus.UNDEF;
//    int deleteClickCount = 0;
//
//    public enum Type {
//        MAIN,
//        SETTING
//    }
//
//    public TodoEntity builderSetting() {
//        return TodoEntity.Build()
//                .id(this.id)
//                .title(this.title)
//                .remindTime(this.remindTime)
//                .openClock(this.openClock)
//                .userId(this.userId)
//                .createTime(this.createTime)
//                .updateTime(this.updateTime)
//                .status(this.status)
//                .roomId(this.roomId)
//                .messageId(this.messageId)
//                .type(Type.SETTING)
//                .processStatus(this.processStatus)
//                .build();
//    }
//
//    public int uniqueId() {
//        String uuid = this.id.replaceAll("-", "");
//        BigInteger big = new BigInteger(uuid, 32);
//        int alarmId = big.intValue();
//        return Math.abs(alarmId);
//    }
//
//    public String toJson() {
//        return JsonHelper.getInstance().toJson(this);
//    }
//
//    public static ContentValues getUpdateContentValues(TodoEntity entity) {
//        ContentValues values = new ContentValues();
////        values.put(DBContract.TodoEntry._ID, entity.getId());
//        values.put(DBContract.TodoEntry.COLUMN_TITLE, entity.getTitle());
//        values.put(DBContract.TodoEntry.COLUMN_USER_ID, entity.getUserId());
//        values.put(DBContract.TodoEntry.COLUMN_TODO_STATUS, entity.getStatus().name());
//        values.put(DBContract.TodoEntry.COLUMN_REMIND_TIME, entity.getRemindTime());
//        values.put(DBContract.TodoEntry.COLUMN_CREATE_TIME, entity.getCreateTime());
//        values.put(DBContract.TodoEntry.COLUMN_UPDATE_TIME, entity.getUpdateTime());
//        values.put(DBContract.TodoEntry.COLUMN_OPEN_CLOCK, EnableType.of(entity.isOpenClock()).name());
//
//        values.put(DBContract.TodoEntry.COLUMN_ROOM_ID, entity.getRoomId());
//        values.put(DBContract.TodoEntry.COLUMN_MESSAGE_ID, entity.getMessageId());
//        values.put(DBContract.TodoEntry.COLUMN_PROCESS_STATUS, entity.getProcessStatus().name());
//        values.put(DBContract.TodoEntry.COLUMN_PUBLIC_TYPE, entity.getPublicType());
//        return values;
//
//    }
//
//
//    public static ContentValues getContentValues(TodoEntity entity) {
//        ContentValues values = new ContentValues();
//        values.put(DBContract.TodoEntry._ID, entity.getId());
//        values.put(DBContract.TodoEntry.COLUMN_TITLE, entity.getTitle());
//        values.put(DBContract.TodoEntry.COLUMN_USER_ID, entity.getUserId());
//        values.put(DBContract.TodoEntry.COLUMN_TODO_STATUS, entity.getStatus().name());
//        values.put(DBContract.TodoEntry.COLUMN_REMIND_TIME, entity.getRemindTime());
//        values.put(DBContract.TodoEntry.COLUMN_CREATE_TIME, entity.getCreateTime());
//        values.put(DBContract.TodoEntry.COLUMN_UPDATE_TIME, entity.getUpdateTime());
//        values.put(DBContract.TodoEntry.COLUMN_OPEN_CLOCK, EnableType.of(entity.isOpenClock()).name());
//
//        values.put(DBContract.TodoEntry.COLUMN_ROOM_ID, entity.getRoomId());
//        values.put(DBContract.TodoEntry.COLUMN_MESSAGE_ID, entity.getMessageId());
//        values.put(DBContract.TodoEntry.COLUMN_PROCESS_STATUS, entity.getProcessStatus().name());
//        values.put(DBContract.TodoEntry.COLUMN_PUBLIC_TYPE, entity.getPublicType());
//        return values;
//
//    }
//
//    public static TodoEntity.TodoEntityBuilder formatByCursor(Cursor cursor) {
//        return TodoEntity.Build()
//                .id(Tools.getDbString(cursor, DBContract.TodoEntry._ID))
//                .title(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_TITLE))
//                .remindTime(Tools.getDbLong(cursor, DBContract.TodoEntry.COLUMN_REMIND_TIME))
//                .openClock(EnableType.valueOf(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_OPEN_CLOCK)).isStatus())
//                .userId(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_USER_ID))
//                .createTime(Tools.getDbLong(cursor, DBContract.TodoEntry.COLUMN_CREATE_TIME))
//                .updateTime(Tools.getDbLong(cursor, DBContract.TodoEntry.COLUMN_UPDATE_TIME))
//                .status(TodoStatus.valueOf(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_TODO_STATUS)))
//                .roomId(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_ROOM_ID))
//                .messageId(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_MESSAGE_ID))
//                .publicType(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_PUBLIC_TYPE))
//                .processStatus(ProcessStatus.valueOf(Tools.getDbString(cursor, DBContract.TodoEntry.COLUMN_PROCESS_STATUS)))
//                .type(Type.MAIN);
//    }
//
//    public static TodoEntity.TodoEntityBuilder formatByCursor(Cursor cursor, ChatRoomEntity entity) {
//        TodoEntity.TodoEntityBuilder builder = formatByCursor(cursor);
//        if (entity != null) {
//            builder.roomEntity(entity);
//        }
//        return builder;
//    }
//
//    public static TodoEntity.TodoEntityBuilder formatByCursor(Cursor cursor, MessageEntity messageEntity, ChatRoomEntity roomEentity) {
//        TodoEntity.TodoEntityBuilder builder = formatByCursor(cursor, roomEentity);
//        if (messageEntity != null) {
//            builder.messageEntity(messageEntity);
//        }
//        return builder;
//    }
//
//
//    public JSONObject toCreateJsonObj() throws JSONException {
//        JSONObject object = new JSONObject()
//                .put("id", this.id)
//                .put("title", this.title)
//                .put("remindTime", this.remindTime)
//                .put("openClock", this.openClock)
//                .put("roomId", this.roomId)
//                .put("messageId", this.messageId)
//                .put("status", this.status.getStatus());
//        return object;
//    }
//
//    public JSONObject toUpdateJsonObj() throws JSONException {
//        JSONObject object = new JSONObject()
//                .put("id", this.id)
//                .put("title", this.title)
//                .put("remindTime", this.remindTime)
//                .put("openClock", this.openClock)
//                .put("roomId", this.roomId)
//                .put("status", this.status.getStatus());
//        return object;
//    }
//
//
//    public JSONObject toCompleteJsonObj() throws JSONException {
//        JSONObject object = new JSONObject()
//                .put("id", this.id)
//                .put("status", this.status.getStatus());
//        return object;
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((id == null) ? 0 : id.hashCode());
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        TodoEntity other = (TodoEntity) obj;
//        if (this.id == null || other.getId() == null) {
//            return false;
//        }
//
//        if (this.id.equals(other.getId()) && this.type.equals(other.getType())) {
//            return true;
//        }
//
////        if (this.id == null) {
////            if (other.getId() != null)
////                return false;
////        } else if (!this.id.equals(other.getId()))
////            return false;
//        return false;
//    }
//
//
//    public double getWeights() {
//        return this.remindTime <= 0 ? 1 : 0;
//    }
//
//    public double getWeights2() {
//        return this.remindTime <= 0 ? this.createTime : this.remindTime;
//    }
//
//    public double getWeights3() {
//        return TodoStatus.DONE.equals(status) ? 1 : 0;
//    }
//
//
//    public double getWeights4() {
//        return this.remindTime >= System.currentTimeMillis() ? this.updateTime : this.remindTime <= 0 ? this.updateTime : this.remindTime;
//    }
//
//    public double getWeights5() {
//        return this.remindTime > 0 && this.remindTime < System.currentTimeMillis() ? 0 : 1;
//    }
//
//    public long getWeights6() {
//        return this.updateTime;
//    }
//
//    @Override
//    public int compareTo(TodoEntity o) {
//        return ComparisonChain.start()
//                .compare(this.getWeights(), o.getWeights())
//                .compare(this.getWeights2(), o.getWeights2())
//                .result();
//    }
//}
