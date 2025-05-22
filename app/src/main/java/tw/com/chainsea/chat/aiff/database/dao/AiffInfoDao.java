package tw.com.chainsea.chat.aiff.database.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import tw.com.chainsea.chat.aiff.database.entity.AiffInfo;
import tw.com.chainsea.chat.aiff.database.entity.AiffInfoUpdate;

@Dao
public abstract class AiffInfoDao extends BaseDao<AiffInfo> {

    @Query("SELECT * FROM AiffInfo WHERE `index` = :index")
    public abstract AiffInfo getAiffInfo(int index);

    @Query("SELECT * FROM AiffInfo WHERE `displayLocation` = :displayLocation AND `title` = :title")
    public abstract AiffInfo getAiffInfo(String displayLocation, String title);

    @Query("SELECT * FROM AiffInfo ORDER BY useTimestamp DESC")
    public abstract List<AiffInfo> getAiffInfoListByUseTime();

    @Query("SELECT * FROM AiffInfo ORDER BY pinTimestamp DESC")
    public abstract List<AiffInfo> getAiffInfoListByPinTime();

    @Query("SELECT * FROM AiffInfo ORDER BY `index` ASC")
    public abstract List<AiffInfo> getAiffInfoListByIndex();

    @Query("SELECT * FROM AiffInfo WHERE `id` = :id")
    public abstract AiffInfo getAiffInfo(String id);

    @Query("SELECT * FROM AiffInfo")
    public abstract List<AiffInfo> getAll();

    @Update
    abstract void updateAiffInfo(AiffInfo info);

    @Update(entity = AiffInfo.class)
    public abstract void updateAiffInfo(AiffInfoUpdate info);

    @Query("DELETE FROM AiffInfo")
    public abstract void delAll();

    //單筆判斷「新增或更新」
    @Transaction
    public void upsert(AiffInfo obj) {
        long id = insert(obj);
        if (id == -1) {
            updateAiffInfo(obj);
        }
    }

    //批次判斷「新增或更新」
    @Transaction
    public void upsert(List<AiffInfo> objList) {
        List<Long> insertResult = insert(objList);
        List<AiffInfo> updateList = new ArrayList<>();

        for (int i = 0; i < insertResult.size(); i++) {
            if (insertResult.get(i) == -1) {
                updateList.add(objList.get(i));
            }
        }

        if (!updateList.isEmpty()) {
            for (int i = 0; i < updateList.size(); i++) {
                updateAiffInfo(updateList.get(i));
            }
        }
    }
}
