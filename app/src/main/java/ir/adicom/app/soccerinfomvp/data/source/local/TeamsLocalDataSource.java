package ir.adicom.app.soccerinfomvp.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ir.adicom.app.soccerinfomvp.data.Team;
import ir.adicom.app.soccerinfomvp.data.source.TeamsDataSource;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Concrete implementation of a data source as a db.
 */
public class TeamsLocalDataSource implements TeamsDataSource {

    private static TeamsLocalDataSource INSTANCE;

    private TeamsDbHelper mDbHelper;

    // Prevent direct instantiation.
    private TeamsLocalDataSource(@NonNull Context context) {
        checkNotNull(context);
        mDbHelper = new TeamsDbHelper(context);
    }

    public static TeamsLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TeamsLocalDataSource(context);
        }
        return INSTANCE;
    }

    /**
     * Note: {@link LoadTeamsCallback#onDataNotAvailable()} is fired if the database doesn't exist
     * or the table is empty.
     */
    @Override
    public void getTeams(@NonNull LoadTeamsCallback callback) {
        List<Team> teams = new ArrayList<Team>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID,
                TeamsPersistenceContract.TeamEntry.COLUMN_NAME_TITLE,
                TeamsPersistenceContract.TeamEntry.COLUMN_NAME_DESCRIPTION,
                TeamsPersistenceContract.TeamEntry.COLUMN_NAME_COMPLETED
        };

        Cursor c = db.query(
                TeamsPersistenceContract.TeamEntry.TABLE_NAME, projection, null, null, null, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                String itemId = c.getString(c.getColumnIndexOrThrow(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID));
                String title = c.getString(c.getColumnIndexOrThrow(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_TITLE));
                String description =
                        c.getString(c.getColumnIndexOrThrow(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_DESCRIPTION));
                boolean completed =
                        c.getInt(c.getColumnIndexOrThrow(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_COMPLETED)) == 1;
                Team team = new Team(title, description, itemId, completed);
                teams.add(team);
            }
        }
        if (c != null) {
            c.close();
        }

        db.close();

        if (teams.isEmpty()) {
            // This will be called if the table is new or just empty.
            callback.onDataNotAvailable();
        } else {
            callback.onTeamsLoaded(teams);
        }

    }

    /**
     * Note: {@link GetTeamCallback#onDataNotAvailable()} is fired if the {@link Team} isn't
     * found.
     */
    @Override
    public void getTeam(@NonNull String teamId, @NonNull GetTeamCallback callback) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID,
                TeamsPersistenceContract.TeamEntry.COLUMN_NAME_TITLE,
                TeamsPersistenceContract.TeamEntry.COLUMN_NAME_DESCRIPTION,
                TeamsPersistenceContract.TeamEntry.COLUMN_NAME_COMPLETED
        };

        String selection = TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { teamId };

        Cursor c = db.query(
                TeamsPersistenceContract.TeamEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        Team team = null;

        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            String itemId = c.getString(c.getColumnIndexOrThrow(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID));
            String title = c.getString(c.getColumnIndexOrThrow(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_TITLE));
            String description =
                    c.getString(c.getColumnIndexOrThrow(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_DESCRIPTION));
            boolean completed =
                    c.getInt(c.getColumnIndexOrThrow(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_COMPLETED)) == 1;
            team = new Team(title, description, itemId, completed);
        }
        if (c != null) {
            c.close();
        }

        db.close();

        if (team != null) {
            callback.onTeamLoaded(team);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void saveTeam(@NonNull Team team) {
        checkNotNull(team);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID, team.getId());
        values.put(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_TITLE, team.getTitle());
        values.put(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_DESCRIPTION, team.getDescription());
        values.put(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_COMPLETED, team.isCompleted());

        db.insert(TeamsPersistenceContract.TeamEntry.TABLE_NAME, null, values);

        db.close();
    }

    @Override
    public void completeTeam(@NonNull Team team) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_COMPLETED, true);

        String selection = TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { team.getId() };

        db.update(TeamsPersistenceContract.TeamEntry.TABLE_NAME, values, selection, selectionArgs);

        db.close();
    }

    @Override
    public void completeTeam(@NonNull String teamId) {
        // Not required for the local data source because the {@link TeamsRepository} handles
        // converting from a {@code teamId} to a {@link team} using its cached data.
    }

    @Override
    public void activateTeam(@NonNull Team team) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TeamsPersistenceContract.TeamEntry.COLUMN_NAME_COMPLETED, false);

        String selection = TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { team.getId() };

        db.update(TeamsPersistenceContract.TeamEntry.TABLE_NAME, values, selection, selectionArgs);

        db.close();
    }

    @Override
    public void activateTeam(@NonNull String teamId) {
        // Not required for the local data source because the {@link TeamsRepository} handles
        // converting from a {@code teamId} to a {@link team} using its cached data.
    }

    @Override
    public void clearCompletedTeams() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = TeamsPersistenceContract.TeamEntry.COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = { "1" };

        db.delete(TeamsPersistenceContract.TeamEntry.TABLE_NAME, selection, selectionArgs);

        db.close();
    }

    @Override
    public void refreshTeams() {
        // Not required because the {@link TeamsRepository} handles the logic of refreshing the
        // teams from all the available data sources.
    }

    @Override
    public void deleteAllTeams() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.delete(TeamsPersistenceContract.TeamEntry.TABLE_NAME, null, null);

        db.close();
    }

    @Override
    public void deleteTeam(@NonNull String teamId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String selection = TeamsPersistenceContract.TeamEntry.COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = { teamId };

        db.delete(TeamsPersistenceContract.TeamEntry.TABLE_NAME, selection, selectionArgs);

        db.close();
    }
}
