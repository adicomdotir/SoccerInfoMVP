package ir.adicom.app.soccerinfomvp.teams;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import ir.adicom.app.soccerinfomvp.R;
import ir.adicom.app.soccerinfomvp.data.Team;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Display a grid of {@link Team}s. User can choose to view all, active or champion tasks.
 */
public class TeamsFragment extends Fragment implements TeamsContract.View {

    private TeamsContract.Presenter mPresenter;

    private TeamsAdapter mListAdapter;

    private View mNoTeamsView;

    private ImageView mNoTeamIcon;

    private TextView mNoTeamMainView;

    private TextView mNoTeamAddView;

    private LinearLayout mTeamsView;

    private TextView mFilteringLabelView;

    public TeamsFragment() {
        // Requires empty public constructor
    }

    public static TeamsFragment newInstance() {
        return new TeamsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListAdapter = new TeamsAdapter(new ArrayList<Team>(0), mItemListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(@NonNull TeamsContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.teams_frag, container, false);

        // Set up tasks view
        ListView listView = (ListView) root.findViewById(R.id.tasks_list);
        listView.setAdapter(mListAdapter);
        mFilteringLabelView = (TextView) root.findViewById(R.id.filteringLabel);
        mTeamsView = (LinearLayout) root.findViewById(R.id.tasksLL);

        // Set up  no tasks view
        mNoTeamsView = root.findViewById(R.id.noTeams);
        mNoTeamIcon = (ImageView) root.findViewById(R.id.noTeamsIcon);
        mNoTeamMainView = (TextView) root.findViewById(R.id.noTeamsMain);
        mNoTeamAddView = (TextView) root.findViewById(R.id.noTeamsAdd);
        mNoTeamAddView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTeam();
            }
        });

        // Set up floating action button
        FloatingActionButton fab =
                (FloatingActionButton) getActivity().findViewById(R.id.fab_add_task);

//        fab.setImageResource(R.drawable.ic_add);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPresenter.addNewTeam();
//            }
//        });

        // Set up progress indicator
//        final ScrollChildSwipeRefreshLayout swipeRefreshLayout =
//                (ScrollChildSwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
//        swipeRefreshLayout.setColorSchemeColors(
//                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
//                ContextCompat.getColor(getActivity(), R.color.colorAccent),
//                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
//        );
//        // Set the scrolling view in the custom SwipeRefreshLayout.
//        swipeRefreshLayout.setScrollUpChild(listView);
//
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                mPresenter.loadTeams(false);
//            }
//        });

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear:
                mPresenter.clearChampionTeams();
                break;
            case R.id.menu_filter:
                showFilteringPopUpMenu();
                break;
            case R.id.menu_refresh:
                mPresenter.loadTeams(true);
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tasks_fragment_menu, menu);
    }

    @Override
    public void showFilteringPopUpMenu() {
        PopupMenu popup = new PopupMenu(getContext(), getActivity().findViewById(R.id.menu_filter));
        popup.getMenuInflater().inflate(R.menu.filter_tasks, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.active:
                        mPresenter.setFiltering(TeamsFilterType.NORMAL_TEAMS);
                        break;
                    case R.id.complete:
                        mPresenter.setFiltering(TeamsFilterType.TOP_TEAMS);
                        break;
                    default:
                        mPresenter.setFiltering(TeamsFilterType.ALL_TEAMS);
                        break;
                }
                mPresenter.loadTeams(false);
                return true;
            }
        });

        popup.show();
    }

    /**
     * Listener for clicks on tasks in the ListView.
     */
    TeamItemListener mItemListener = new TeamItemListener() {
        @Override
        public void onTeamClick(Team clickedTeam) {
            mPresenter.openTeamDetails(clickedTeam);
        }

        @Override
        public void onCompleteTeamClick(Team championTeam) {
            mPresenter.championTeam(championTeam);
        }

        @Override
        public void onNormalTeamClick(Team normalTeam) {
            mPresenter.normalTeam(normalTeam);
        }
    };

    @Override
    public void setLoadingIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showTeams(List<Team> tasks) {
        mListAdapter.replaceData(tasks);

        mTeamsView.setVisibility(View.VISIBLE);
        mNoTeamsView.setVisibility(View.GONE);
    }

    @Override
    public void showNoActiveTeams() {
        showNoTeamsViews(
                getResources().getString(R.string.no_tasks_active),
                R.drawable.ic_check_circle_24dp,
                false
        );
    }

    @Override
    public void showNoTeams() {
        showNoTeamsViews(
                getResources().getString(R.string.no_tasks_all),
                R.drawable.ic_assignment_turned_in_24dp,
                false
        );
    }

    @Override
    public void showNoChampionTeams() {
        showNoTeamsViews(
                getResources().getString(R.string.no_tasks_completed),
                R.drawable.ic_verified_user_24dp,
                false
        );
    }

    @Override
    public void showSuccessfullySavedMessage() {
        showMessage(getString(R.string.successfully_saved_task_message));
    }

    private void showNoTeamsViews(String mainText, int iconRes, boolean showAddView) {
        mTeamsView.setVisibility(View.GONE);
        mNoTeamsView.setVisibility(View.VISIBLE);

        mNoTeamMainView.setText(mainText);
        mNoTeamIcon.setImageDrawable(getResources().getDrawable(iconRes));
        mNoTeamAddView.setVisibility(showAddView ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showActiveFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_active));
    }

    @Override
    public void showChampionFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_completed));
    }

    @Override
    public void showAllFilterLabel() {
        mFilteringLabelView.setText(getResources().getString(R.string.label_all));
    }

    @Override
    public void showAddTeam() {
//        Intent intent = new Intent(getContext(), AddEditTeamActivity.class);
//        startActivityForResult(intent, AddEditTeamActivity.REQUEST_ADD_TASK);
    }

    @Override
    public void showTeamDetailsUi(String taskId) {
        // in it's own Activity, since it makes more sense that way and it gives us the flexibility
        // to show some Intent stubbing.
//        Intent intent = new Intent(getContext(), TeamDetailActivity.class);
//        intent.putExtra(TeamDetailActivity.EXTRA_TASK_ID, taskId);
//        startActivity(intent);
    }

    @Override
    public void showTeamMarkedComplete() {
        showMessage(getString(R.string.task_marked_complete));
    }

    @Override
    public void showTeamMarkedActive() {
        showMessage(getString(R.string.task_marked_active));
    }

    @Override
    public void showChampionTeamsCleared() {
        showMessage(getString(R.string.completed_tasks_cleared));
    }

    @Override
    public void showLoadingTeamsError() {
        showMessage(getString(R.string.loading_tasks_error));
    }

    private void showMessage(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private static class TeamsAdapter extends BaseAdapter {

        private List<Team> mTeams;
        private TeamItemListener mItemListener;

        public TeamsAdapter(List<Team> tasks, TeamItemListener itemListener) {
            setList(tasks);
            mItemListener = itemListener;
        }

        public void replaceData(List<Team> tasks) {
            setList(tasks);
            notifyDataSetChanged();
        }

        private void setList(List<Team> tasks) {
            mTeams = checkNotNull(tasks);
        }

        @Override
        public int getCount() {
            return mTeams.size();
        }

        @Override
        public Team getItem(int i) {
            return mTeams.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View rowView = view;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                rowView = inflater.inflate(R.layout.team_item, viewGroup, false);
            }

            final Team task = getItem(i);

            TextView titleTV = (TextView) rowView.findViewById(R.id.title);
            titleTV.setText(task.getTitleForList());

            CheckBox completeCB = (CheckBox) rowView.findViewById(R.id.complete);

            // Active/champion task UI
            completeCB.setChecked(task.isChampion());
            if (task.isChampion()) {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.list_champion_touch_feedback));
            } else {
                rowView.setBackgroundDrawable(viewGroup.getContext()
                        .getResources().getDrawable(R.drawable.touch_feedback));
            }

            completeCB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!task.isChampion()) {
                        mItemListener.onCompleteTeamClick(task);
                    } else {
                        mItemListener.onNormalTeamClick(task);
                    }
                }
            });

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mItemListener.onTeamClick(task);
                }
            });

            return rowView;
        }
    }

    public interface TeamItemListener {

        void onTeamClick(Team clickedTeam);

        void onCompleteTeamClick(Team championTeam);

        void onNormalTeamClick(Team normalTeam);
    }

}
