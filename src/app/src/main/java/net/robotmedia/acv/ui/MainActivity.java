/*******************************************************************************
 * Copyright 2009 Robot Media SL
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.robotmedia.acv.ui;

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.adapter.ViewPagerAdapter;
import net.robotmedia.acv.ui.settings.tablet.SettingsActivityTablet;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        RecentReadsFragment.OnFragmentInteractionListener,
        ShelfFragment.OnFragmentInteractionListener,
        RecentlAddedFragment.OnFragmentInteractionListener {

    protected SharedPreferences preferences;
    private ViewPager viewPager;
    private FrameLayout frameLayout;
    private TabLayout tabLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);
        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        frameLayout = (FrameLayout) findViewById(R.id.container);
        frameLayout.setVisibility(FrameLayout.GONE);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new RecentReadsFragment(), "RECENT READS");
        adapter.addFragment(new ShelfFragment(), "COMICS");
        adapter.addFragment(new RecentlAddedFragment(), "RECENTLY ADDED");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();

        switch(item.getItemId()){
            case R.id.nav_my_books:
                frameLayout.setVisibility(FrameLayout.GONE);
                viewPager.setVisibility(ViewPager.VISIBLE);
                tabLayout.setVisibility(TabLayout.VISIBLE);
                break;
            case R.id.nav_my_comics:
                frameLayout.setVisibility(FrameLayout.GONE);
                viewPager.setVisibility(ViewPager.VISIBLE);
                tabLayout.setVisibility(TabLayout.VISIBLE);
                break;
            case R.id.nav_favorites:
                frameLayout.setVisibility(FrameLayout.GONE);
                viewPager.setVisibility(ViewPager.VISIBLE);
                tabLayout.setVisibility(TabLayout.VISIBLE);
                break;
            case R.id.nav_collections:
                frameLayout.setVisibility(FrameLayout.GONE);
                viewPager.setVisibility(ViewPager.VISIBLE);
                tabLayout.setVisibility(TabLayout.VISIBLE);

                break;
            case R.id.nav_add_file:
                viewPager.setVisibility(ViewPager.GONE);
                tabLayout.setVisibility(TabLayout.GONE);
                frameLayout.setVisibility(FrameLayout.VISIBLE);

                fragment = new SDBrowserFragment();
                String comicsPath = preferences.getString(Constants.COMICS_PATH_KEY, Environment.getExternalStorageDirectory().getAbsolutePath());
                bundle.putString(comicsPath, Constants.COMIC_PATH_KEY);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_settings:
                viewPager.setVisibility(ViewPager.GONE);
                tabLayout.setVisibility(TabLayout.GONE);
                frameLayout.setVisibility(FrameLayout.VISIBLE);
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_settings:
                startSettingsActivity();
                return true;
            case R.id.item_share_app:
                shareApp();
                return true;
            case R.id.menu_close:
                finish();
                return true;
        }

        return false;
    }

    private void startSettingsActivity() {
        Intent myIntent = new Intent(this, SettingsActivityTablet.class);
        startActivityForResult(myIntent, Constants.SETTINGS_CODE);
    }


    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_title));
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message));
        Intent chooser = Intent.createChooser(intent, getString(R.string.item_share_app_title));
        startActivity(chooser);
    }
}
