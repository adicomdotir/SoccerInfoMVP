package ir.adicom.app.soccerinfomvp.data.source.remote;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import ir.adicom.app.soccerinfomvp.data.Team;
import ir.adicom.app.soccerinfomvp.data.source.TeamsDataSource;

/**
 * Implementation of the data source that adds a latency simulating network.
 */
public class TeamsRemoteDataSource implements TeamsDataSource {

    private static TeamsRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, Team> TASKS_SERVICE_DATA;

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
        addTeam("Build tower in Pisa", "Ground looks good, no foundation work required.");
        addTeam("Finish bridge in Tacoma", "Found awesome girders at half the cost!");
    }

    public static TeamsRemoteDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TeamsRemoteDataSource();
        }
        return INSTANCE;
    }

    // Prevent direct instantiation.
    private TeamsRemoteDataSource() {}

    private static void addTeam(String title, String description) {
        Team newTeam = new Team(title, description);
        TASKS_SERVICE_DATA.put(newTeam.getId(), newTeam);
    }

    /**
     * Note: {@link LoadTeamsCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getTeams(final @NonNull LoadTeamsCallback callback) {
        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onTeamsLoaded(Lists.newArrayList(TASKS_SERVICE_DATA.values()));
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    /**
     * Note: {@link GetTeamCallback#onDataNotAvailable()} is never fired. In a real remote data
     * source implementation, this would be fired if the server can't be contacted or the server
     * returns an error.
     */
    @Override
    public void getTeam(@NonNull String teamId, final @NonNull GetTeamCallback callback) {
        final Team team = TASKS_SERVICE_DATA.get(teamId);

        // Simulate network by delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onTeamLoaded(team);
            }
        }, SERVICE_LATENCY_IN_MILLIS);
    }

    @Override
    public void saveTeam(@NonNull Team team) {
        TASKS_SERVICE_DATA.put(team.getId(), team);
    }

    @Override
    public void completeTeam(@NonNull Team team) {
        Team completedTeam = new Team(team.getTitle(), team.getDescription(), team.getId(), true);
        TASKS_SERVICE_DATA.put(team.getId(), completedTeam);
    }

    @Override
    public void completeTeam(@NonNull String teamId) {
        // Not required for the remote data source because the {@link TeamsRepository} handles
        // converting from a {@code teamId} to a {@link team} using its cached data.
    }

    @Override
    public void activateTeam(@NonNull Team team) {
        Team activeTeam = new Team(team.getTitle(), team.getDescription(), team.getId());
        TASKS_SERVICE_DATA.put(team.getId(), activeTeam);
    }

    @Override
    public void activateTeam(@NonNull String teamId) {
        // Not required for the remote data source because the {@link TeamsRepository} handles
        // converting from a {@code teamId} to a {@link team} using its cached data.
    }

    @Override
    public void clearCompletedTeams() {
        Iterator<Map.Entry<String, Team>> it = TASKS_SERVICE_DATA.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Team> entry = it.next();
            if (entry.getValue().isCompleted()) {
                it.remove();
            }
        }
    }

    @Override
    public void refreshTeams() {
        // Not required because the {@link TeamsRepository} handles the logic of refreshing the
        // teams from all the available data sources.
    }

    @Override
    public void deleteAllTeams() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public void deleteTeam(@NonNull String teamId) {
        TASKS_SERVICE_DATA.remove(teamId);
    }
}
