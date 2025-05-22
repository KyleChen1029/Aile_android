package tw.com.chainsea.chat.aiff.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import tw.com.chainsea.chat.aiff.database.dao.AiffInfoDao;
import tw.com.chainsea.chat.aiff.database.entity.AiffInfo;

@Database(entities = {AiffInfo.class}, version = 4, exportSchema = false)
public abstract class AiffDB extends RoomDatabase {

    public static final String DB_NAME = "AiffDB.db";
    private static volatile AiffDB instance;

    public static synchronized AiffDB getInstance(Context context){
        if(instance == null){
            instance = create(context);
        }
        return instance;
    }

    private static AiffDB create(final Context context){
        return Room.databaseBuilder(context, AiffDB.class, DB_NAME)
                .allowMainThreadQueries()
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .build();
    }

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE aiffInfo "
                    + "ADD COLUMN incomingAiff TEXT");
            database.execSQL("ALTER TABLE aiffInfo "
                    + "ADD COLUMN serviceNumberIds TEXT");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE aiffInfo "
                    + "ADD COLUMN useTimestamp INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE aiffInfo "
                    + "ADD COLUMN pinTimestamp INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE aiffInfo "
                    + "ADD COLUMN userType_APP TEXT");
            database.execSQL("ALTER TABLE aiffInfo "
                    + "ADD COLUMN serviceNumberNames TEXT");
            database.execSQL("ALTER TABLE aiffInfo "
                    + "ADD COLUMN status TEXT");
        }
    };

    public void openDb() {
        if (instance != null) {
            instance.getOpenHelper().getWritableDatabase();
        }
    }

    public abstract AiffInfoDao getAiffInfoDao();
}
