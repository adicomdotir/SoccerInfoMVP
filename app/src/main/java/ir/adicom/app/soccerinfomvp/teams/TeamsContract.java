package ir.adicom.app.soccerinfomvp.teams;

import android.support.annotation.NonNull;

import java.util.List;

import ir.adicom.app.soccerinfomvp.BasePresenter;
import ir.adicom.app.soccerinfomvp.BaseView;
import ir.adicom.app.soccerinfomvp.data.Team;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface TeamsContract {

    interface View extends BaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showTeams(List<Team> teams);

        void showAddTeam();

        void showTeamDetailsUi(String teamId);

        void showTeamMarkedComplete();

        void showTeamMarkedActive();

        void showCompletedTeamsCleared();

        void showLoadingTeamsError();

        void showNoTeams();

        void showActiveFilterLabel();

        void showCompletedFilterLabel();

        void showAllFilterLabel();

        void showNoActiveTeams();

        void showNoCompletedTeams();

        void showSuccessfullySavedMessage();

        boolean isActive();

        void showFilteringPopUpMenu();
    }

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void loadTeams(boolean forceUpdate);

        void addNewTeam();

        void openTeamDetails(@NonNull Team requestedTeam);

        void championTeam(@NonNull Team championTeam);

        void normalTeam(@NonNull Team normalTeam);

        void clearCompletedTeams();

        void setFiltering(TeamsFilterType requestType);

        TeamsFilterType getFiltering();
    }
}
