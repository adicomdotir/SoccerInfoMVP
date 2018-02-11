package ir.adicom.app.soccerinfomvp.data.source;

import android.support.annotation.NonNull;

import java.util.List;

import ir.adicom.app.soccerinfomvp.data.Team;

/**
 * Main entry point for accessing teams data.
 * <p>
 * For simplicity, only getTeams() and getTeam() have callbacks. Consider adding callbacks to other
 * methods to inform the user of network/database errors or successful operations.
 * For example, when a new team is created, it's synchronously stored in cache but usually every
 * operation on database or network should be executed in a different thread.
 */
public interface TeamsDataSource {

    interface LoadTeamsCallback {

        void onTeamsLoaded(List<Team> teams);

        void onDataNotAvailable();
    }

    interface GetTeamCallback {

        void onTeamLoaded(Team team);

        void onDataNotAvailable();
    }

    void getTeams(@NonNull LoadTeamsCallback callback);

    void getTeam(@NonNull String teamId, @NonNull GetTeamCallback callback);

    void saveTeam(@NonNull Team team);

    void completeTeam(@NonNull Team team);

    void completeTeam(@NonNull String teamId);

    void activateTeam(@NonNull Team team);

    void activateTeam(@NonNull String teamId);

    void clearCompletedTeams();

    void refreshTeams();

    void deleteAllTeams();

    void deleteTeam(@NonNull String teamId);
}
