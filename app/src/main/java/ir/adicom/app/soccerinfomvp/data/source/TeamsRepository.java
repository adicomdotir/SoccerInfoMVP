package ir.adicom.app.soccerinfomvp.data.source;

import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ir.adicom.app.soccerinfomvp.data.Team;

/**
 * Concrete implementation to load teams from the data sources into a cache.
 * <p>
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 */
public class TeamsRepository implements TeamsDataSource {

    private static TeamsRepository INSTANCE = null;

    private final TeamsDataSource mTeamsRemoteDataSource;

    private final TeamsDataSource mTeamsLocalDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Team> mCachedTeams;

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    boolean mCacheIsDirty = false;

    // Prevent direct instantiation.
    private TeamsRepository(@NonNull TeamsDataSource teamsRemoteDataSource,
                            @NonNull TeamsDataSource teamsLocalDataSource) {
        mTeamsRemoteDataSource = checkNotNull(teamsRemoteDataSource);
        mTeamsLocalDataSource = checkNotNull(teamsLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param teamsRemoteDataSource the backend data source
     * @param teamsLocalDataSource  the device storage data source
     * @return the {@link TeamsRepository} instance
     */
    public static TeamsRepository getInstance(TeamsDataSource teamsRemoteDataSource,
                                              TeamsDataSource teamsLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new TeamsRepository(teamsRemoteDataSource, teamsLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(TeamsDataSource, TeamsDataSource)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets teams from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     * <p>
     * Note: {@link LoadTeamsCallback#onDataNotAvailable()} is fired if all data sources fail to
     * get the data.
     */
    @Override
    public void getTeams(@NonNull final LoadTeamsCallback callback) {
        checkNotNull(callback);

        // Respond immediately with cache if available and not dirty
        if (mCachedTeams != null && !mCacheIsDirty) {
            callback.onTeamsLoaded(new ArrayList<>(mCachedTeams.values()));
            return;
        }

        if (mCacheIsDirty) {
            // If the cache is dirty we need to fetch new data from the network.
            getTeamsFromRemoteDataSource(callback);
        } else {
            // Query the local storage if available. If not, query the network.
            mTeamsLocalDataSource.getTeams(new LoadTeamsCallback() {
                @Override
                public void onTeamsLoaded(List<Team> teams) {
                    refreshCache(teams);
                    callback.onTeamsLoaded(new ArrayList<>(mCachedTeams.values()));
                }

                @Override
                public void onDataNotAvailable() {
                    getTeamsFromRemoteDataSource(callback);
                }
            });
        }
    }

    @Override
    public void saveTeam(@NonNull Team team) {
        checkNotNull(team);
        mTeamsRemoteDataSource.saveTeam(team);
        mTeamsLocalDataSource.saveTeam(team);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTeams == null) {
            mCachedTeams = new LinkedHashMap<>();
        }
        mCachedTeams.put(team.getId(), team);
    }

    @Override
    public void championTeam(@NonNull Team team) {
        checkNotNull(team);
        mTeamsRemoteDataSource.championTeam(team);
        mTeamsLocalDataSource.championTeam(team);

        Team championdTeam = new Team(team.getTitle(), team.getDescription(), team.getId(), true);

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTeams == null) {
            mCachedTeams = new LinkedHashMap<>();
        }
        mCachedTeams.put(team.getId(), championdTeam);
    }

    @Override
    public void championTeam(@NonNull String teamId) {
        checkNotNull(teamId);
        championTeam(getTeamWithId(teamId));
    }

    @Override
    public void normalTeam(@NonNull Team team) {
        checkNotNull(team);
        mTeamsRemoteDataSource.normalTeam(team);
        mTeamsLocalDataSource.normalTeam(team);

        Team activeTeam = new Team(team.getTitle(), team.getDescription(), team.getId());

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTeams == null) {
            mCachedTeams = new LinkedHashMap<>();
        }
        mCachedTeams.put(team.getId(), activeTeam);
    }

    @Override
    public void normalTeam(@NonNull String teamId) {
        checkNotNull(teamId);
        normalTeam(getTeamWithId(teamId));
    }

    @Override
    public void clearChampionTeams() {
        mTeamsRemoteDataSource.clearChampionTeams();
        mTeamsLocalDataSource.clearChampionTeams();

        // Do in memory cache update to keep the app UI up to date
        if (mCachedTeams == null) {
            mCachedTeams = new LinkedHashMap<>();
        }
        Iterator<Map.Entry<String, Team>> it = mCachedTeams.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Team> entry = it.next();
            if (entry.getValue().isChampion()) {
                it.remove();
            }
        }
    }

    /**
     * Gets teams from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     * <p>
     * Note: {@link GetTeamCallback#onDataNotAvailable()} is fired if both data sources fail to
     * get the data.
     */
    @Override
    public void getTeam(@NonNull final String teamId, @NonNull final GetTeamCallback callback) {
        checkNotNull(teamId);
        checkNotNull(callback);

        Team cachedTeam = getTeamWithId(teamId);

        // Respond immediately with cache if available
        if (cachedTeam != null) {
            callback.onTeamLoaded(cachedTeam);
            return;
        }

        // Load from server/persisted if needed.

        // Is the team in the local data source? If not, query the network.
        mTeamsLocalDataSource.getTeam(teamId, new GetTeamCallback() {
            @Override
            public void onTeamLoaded(Team team) {
                // Do in memory cache update to keep the app UI up to date
                if (mCachedTeams == null) {
                    mCachedTeams = new LinkedHashMap<>();
                }
                mCachedTeams.put(team.getId(), team);
                callback.onTeamLoaded(team);
            }

            @Override
            public void onDataNotAvailable() {
                mTeamsRemoteDataSource.getTeam(teamId, new GetTeamCallback() {
                    @Override
                    public void onTeamLoaded(Team team) {
                        // Do in memory cache update to keep the app UI up to date
                        if (mCachedTeams == null) {
                            mCachedTeams = new LinkedHashMap<>();
                        }
                        mCachedTeams.put(team.getId(), team);
                        callback.onTeamLoaded(team);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void refreshTeams() {
        mCacheIsDirty = true;
    }

    @Override
    public void deleteAllTeams() {
        mTeamsRemoteDataSource.deleteAllTeams();
        mTeamsLocalDataSource.deleteAllTeams();

        if (mCachedTeams == null) {
            mCachedTeams = new LinkedHashMap<>();
        }
        mCachedTeams.clear();
    }

    @Override
    public void deleteTeam(@NonNull String teamId) {
        mTeamsRemoteDataSource.deleteTeam(checkNotNull(teamId));
        mTeamsLocalDataSource.deleteTeam(checkNotNull(teamId));

        mCachedTeams.remove(teamId);
    }

    private void getTeamsFromRemoteDataSource(@NonNull final LoadTeamsCallback callback) {
        mTeamsRemoteDataSource.getTeams(new LoadTeamsCallback() {
            @Override
            public void onTeamsLoaded(List<Team> teams) {
                refreshCache(teams);
                refreshLocalDataSource(teams);
                callback.onTeamsLoaded(new ArrayList<>(mCachedTeams.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshCache(List<Team> teams) {
        if (mCachedTeams == null) {
            mCachedTeams = new LinkedHashMap<>();
        }
        mCachedTeams.clear();
        for (Team team : teams) {
            mCachedTeams.put(team.getId(), team);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Team> teams) {
        mTeamsLocalDataSource.deleteAllTeams();
        for (Team team : teams) {
            mTeamsLocalDataSource.saveTeam(team);
        }
    }

    @Nullable
    private Team getTeamWithId(@NonNull String id) {
        checkNotNull(id);
        if (mCachedTeams == null || mCachedTeams.isEmpty()) {
            return null;
        } else {
            return mCachedTeams.get(id);
        }
    }
}
