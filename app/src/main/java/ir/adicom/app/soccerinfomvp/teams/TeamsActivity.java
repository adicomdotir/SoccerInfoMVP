package ir.adicom.app.soccerinfomvp.teams;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import ir.adicom.app.soccerinfomvp.R;
import ir.adicom.app.soccerinfomvp.data.source.TeamsRepository;
import ir.adicom.app.soccerinfomvp.util.ActivityUtils;

public class TeamsActivity extends AppCompatActivity {

    private static final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";

    private DrawerLayout mDrawerLayout;

    private TeamsPresenter mTeamPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teams_activity);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);

        // Set up the navigation drawer.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        TeamsFragment teamsFragment =
                (TeamsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (teamsFragment == null) {
            // Create the fragment
            teamsFragment = TeamsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), teamsFragment, R.id.contentFrame);
        }

        TeamsRepository.getInstance(FakeTasksRemoteDataSource.getInstance(),
                TasksLocalDataSource.getInstance(context));
        // Create the presenter
        mTeamPresenter = new TeamsPresenter(
                Injection.provideTeamRepository(getApplicationContext()), teamsFragment);

        // Load previously saved state, if available.
        if (savedInstanceState != null) {
            TeamsFilterType currentFiltering =
                    (TeamsFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            mTeamPresenter.setFiltering(currentFiltering);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(CURRENT_FILTERING_KEY, mTeamPresenter.getFiltering());

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Open the navigation drawer when the home icon is selected from the toolbar.
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
//                            case R.id.list_navigation_menu_item:
//                                // Do nothing, we're already on that screen
//                                break;
//                            case R.id.statistics_navigation_menu_item:
//                                Intent intent =
//                                        new Intent(TeamsActivity.this, StatisticsActivity.class);
//                                startActivity(intent);
//                                break;
                            default:
                                break;
                        }
                        // Close the navigation drawer when an item is selected.
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

}
