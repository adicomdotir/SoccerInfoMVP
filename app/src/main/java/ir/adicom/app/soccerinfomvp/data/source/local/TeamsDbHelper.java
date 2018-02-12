package ir.adicom.app.soccerinfomvp.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TeamsDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "Teams.db";

    private static final String TEXT_TYPE = " TEXT";

    private static final String BOOLEAN_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TeamsPersistenceContract.TeamEntry.TABLE_NAME + " (" +
                    TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + " PRIMARY KEY," +
                    TeamsPersistenceContract.TeamEntry.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    TeamsPersistenceContract.TeamEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    TeamsPersistenceContract.TeamEntry.COLUMN_NAME_CHAMPION + BOOLEAN_TYPE +
            " )";

    public TeamsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
