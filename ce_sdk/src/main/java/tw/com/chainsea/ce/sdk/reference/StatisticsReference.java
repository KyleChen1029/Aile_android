package tw.com.chainsea.ce.sdk.reference;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;
import java.util.Set;

import tw.com.chainsea.ce.sdk.bean.statistics.StatisticsEntity;
import tw.com.chainsea.ce.sdk.database.DBContract;
import tw.com.chainsea.ce.sdk.database.DBManager;

/**
 * current by evan on 2020-09-18
 *
 * @author Evan Wang
 * @date 2020-09-18
 */
public class StatisticsReference extends AbsReference {


    /**
     * 批量 更換
     */
    public static boolean replace(SQLiteDatabase db, String relId, List<StatisticsEntity> entities, Set<Long> endTimeSet) {
        String endTimeIn = String.format(DBContract.StatisticsEntry.COLUMN_END_TIME + " IN (%s)", concatStrings("", ",", endTimeSet.toArray()));
        String deleteSql = "DELETE FROM " + DBContract.StatisticsEntry.TABLE_NAME
                + " WHERE " + DBContract.StatisticsEntry.COLUMN_RELATION_ID + "=?"
                + " AND " + endTimeIn;
        (db == null ? DBManager.getInstance().openDatabase() : db).execSQL(deleteSql, new String[]{relId});

        boolean result = true;
        for (StatisticsEntity entity : entities) {
            ContentValues values = StatisticsEntity.getContentValues(entity, System.currentTimeMillis());
            long _id = (db == null ? DBManager.getInstance().openDatabase() : db).insert(DBContract.StatisticsEntry.TABLE_NAME, null, values);
            result = result && _id > 0;
        }
        return result;
    }
}
