package ir.adicom.app.soccerinfomvp.teams;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ir.adicom.app.soccerinfomvp.data.Team;
import ir.adicom.app.soccerinfomvp.data.source.TeamsDataSource;
import ir.adicom.app.soccerinfomvp.data.source.TeamsRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Listens to user actions from the UI ({@link TeamsFragment}), retrieves the data and updates the
 * UI as required.
 */
public class TeamsPresenter implements TeamsContract.Presenter {

    private final TeamsRepository mTeamsRepository;

    private final TeamsContract.View mTeamsView;

    private TeamsFilterType mCurrentFiltering = TeamsFilterType.ALL_TEAMS;

    private boolean mFirstLoad = true;

    public TeamsPresenter(@NonNull TeamsRepository teamsRepository, @NonNull TeamsContract.View teamsView) {
        mTeamsRepository = checkNotNull(teamsRepository, "teamsRepository cannot be null");
        mTeamsView = checkNotNull(teamsView, "teamsView cannot be null!");

        mTeamsView.setPresenter(this);
    }

    @Override
    public void start() {
        loadTeams(false);
    }

    @Override
    public void result(int requestCode, int resultCode) {
        // If a task was successfully added, show snackbar
//        if (AddEditTeamActivity.REQUEST_ADD_TASK == requestCode && Activity.RESULT_OK == resultCode) {
//            mTeamsView.showSuccessfullySavedMessage();
//        }
    }

    @Override
    public void loadTeams(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadTeams(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }

    /**
     * @param forceUpdate   Pass in true to refresh the data in the {@link TeamsDataSource}
     * @param showLoadingUI Pass in true to display a loading icon in the UI
     */
    private void loadTeams(boolean forceUpdate, final boolean showLoadingUI) {
        if (showLoadingUI) {
            mTeamsView.setLoadingIndicator(true);
        }
        if (forceUpdate) {
            mTeamsRepository.refreshTeams();
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        // EspressoIdlingResource.increment(); // App is busy until further notice

        mTeamsRepository.getTeams(new TeamsDataSource.LoadTeamsCallback() {
            @Override
            public void onTeamsLoaded(List<Team> teams) {
                List<Team> teamsToShow = new ArrayList<Team>();

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
//                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
//                    EspressoIdlingResource.decrement(); // Set app as idle.
//                }

                // We filter the teams based on the requestType
                for (Team task : teams) {
                    switch (mCurrentFiltering) {
                        case ALL_TEAMS:
                            teamsToShow.add(task);
                            break;
                        case NORMAL_TEAMS:
                            if (task.isNormal()) {
                                teamsToShow.add(task);
                            }
                            break;
                        case TOP_TEAMS:
                            if (task.isChampion()) {
                                teamsToShow.add(task);
                            }
                            break;
                        default:
                            teamsToShow.add(task);
                            break;
                    }
                }
                // The view may not be able to handle UI updates anymore
                if (!mTeamsView.isActive()) {
                    return;
                }
                if (showLoadingUI) {
                    mTeamsView.setLoadingIndicator(false);
                }

                processTeams(teamsToShow);
            }

            @Override
            public void onDataNotAvailable() {
                // The view may not be able to handle UI updates anymore
                if (!mTeamsView.isActive()) {
                    return;
                }
                mTeamsView.showLoadingTeamsError();
            }
        });
    }

    private void processTeams(List<Team> teams) {
        if (teams.isEmpty()) {
            // Show a message indicating there are no teams for that filter type.
            processEmptyTeams();
        } else {
            // Show the list of teams
            mTeamsView.showTeams(teams);
            // Set the filter label's text.
            showFilterLabel();
        }
    }

    private void showFilterLabel() {
        switch (mCurrentFiltering) {
            case NORMAL_TEAMS:
                mTeamsView.showActiveFilterLabel();
                break;
            case TOP_TEAMS:
                mTeamsView.showCompletedFilterLabel();
                break;
            default:
                mTeamsView.showAllFilterLabel();
                break;
        }
    }

    private void processEmptyTeams() {
        switch (mCurrentFiltering) {
            case NORMAL_TEAMS:
                mTeamsView.showNoActiveTeams();
                break;
            case TOP_TEAMS:
                mTeamsView.showNoCompletedTeams();
                break;
            default:
                mTeamsView.showNoTeams();
                break;
        }
    }

    @Override
    public void addNewTeam() {
        mTeamsView.showAddTeam();
    }

    @Override
    public void openTeamDetails(@NonNull Team requestedTeam) {
        checkNotNull(requestedTeam, "requestedTeam cannot be null!");
        mTeamsView.showTeamDetailsUi(requestedTeam.getId());
    }

    @Override
    public void championTeam(@NonNull Team championdTeam) {
        checkNotNull(championdTeam, "championdTeam cannot be null!");
        mTeamsRepository.championTeam(championdTeam);
        mTeamsView.showTeamMarkedComplete();
        loadTeams(false, false);
    }

    @Override
    public void normalTeam(@NonNull Team normalTeam) {
        checkNotNull(normalTeam, "normalTeam cannot be null!");
        mTeamsRepository.normalTeam(normalTeam);
        mTeamsView.showTeamMarkedActive();
        loadTeams(false, false);
    }

    @Override
    public void clearCompletedTeams() {
        mTeamsRepository.clearChampionTeams();
        mTeamsView.showCompletedTeamsCleared();
        loadTeams(false, false);
    }

    /**
     * Sets the current task filtering type.
     *
     * @param requestType Can be {@link TeamsFilterType#ALL_TEAMS},
     *                    {@link TeamsFilterType#TOP_TEAMS}, or
     *                    {@link TeamsFilterType#NORMAL_TEAMS}
     */
    @Override
    public void setFiltering(TeamsFilterType requestType) {
        mCurrentFiltering = requestType;
    }

    @Override
    public TeamsFilterType getFiltering() {
        return mCurrentFiltering;
    }

}
