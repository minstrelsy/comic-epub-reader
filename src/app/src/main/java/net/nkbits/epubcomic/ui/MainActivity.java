/*******************************************************************************
 * Copyright 2016 NKBits Development
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
package net.nkbits.epubcomic.ui;

import net.androidcomics.acv.R;
import net.nkbits.epubcomic.Constants;
import net.nkbits.epubcomic.adapter.ViewPagerAdapter;
import net.nkbits.epubcomic.db.DBHelper;
import net.nkbits.epubcomic.utils.FileUtils;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.*;
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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        RecentReadsFragment.OnFragmentInteractionListener,
        ShelfFragment.OnFragmentInteractionListener,
        RecentlyAddedFragment.OnFragmentInteractionListener,
        SDBrowserFragment.OnFragmentInteractionListener{

    protected SharedPreferences preferences;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Fragment currentFragment;
    private SDBrowserFragment sdBrowserFragment;
    private ShelfFragment shelfFragment;
    private RecentlyAddedFragment recentlyAddedFragment;
    private RecentReadsFragment recentReadsFragment;
    private MenuItem importAction;
    private MenuItem selectAllAction;
    private int drawerItemSelected;
    private DBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);
        Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(1).select();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        currentFragment = null;
        shelfFragment = new ShelfFragment();
        recentlyAddedFragment = new RecentlyAddedFragment();
        recentReadsFragment = new RecentReadsFragment();

        dbHelper = new DBHelper(this);
    }

    @Override
    public void onStop () {
        dbHelper.file.deleteFiles();
        super.onStop();
    }

    @Override
    public void onFileSelected(String path) {

    }

    @Override
    public void onFilesSelected(ArrayList<File> files) {
        insertFiles(files);
    }

    @Override
    public void finishThisFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();

        switch (drawerItemSelected) {
            case R.id.nav_add_file:{
                selectAllAction.setVisible(false);
                importAction.setVisible(false);
                break;
            }
        }

        fragmentManager.beginTransaction().remove(fragment).commit();
        viewPager.setVisibility(ViewPager.VISIBLE);
        tabLayout.setVisibility(TabLayout.VISIBLE);
    }

    @Override
    public void hideSelectAll(){
        selectAllAction.setVisible(false);
    }

    public void showSelectAll(){
        selectAllAction.setVisible(true);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(recentReadsFragment, "RECENT READS");
        adapter.addFragment(shelfFragment, "COMICS");
        adapter.addFragment(recentlyAddedFragment, "RECENTLY ADDED");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } if(drawerItemSelected == R.id.nav_add_file){
            sdBrowserFragment.fragmentOnBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        importAction = menu.findItem(R.id.action_import);
        selectAllAction = menu.findItem(R.id.action_select_all);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        Bundle bundle = new Bundle();
        FragmentManager fragmentManager = getFragmentManager();

        switch(item.getItemId()){
            case R.id.nav_my_books:
                viewPager.setVisibility(ViewPager.VISIBLE);
                tabLayout.setVisibility(TabLayout.VISIBLE);
                tabLayout.getTabAt(1).setText("BOOKS");
                break;
            case R.id.nav_my_comics:
                viewPager.setVisibility(ViewPager.VISIBLE);
                tabLayout.setVisibility(TabLayout.VISIBLE);
                tabLayout.getTabAt(1).setText("COMICS");
                break;
            case R.id.nav_favorites:
                viewPager.setVisibility(ViewPager.GONE);
                tabLayout.setVisibility(TabLayout.GONE);
                break;
            case R.id.nav_collections:
                viewPager.setVisibility(ViewPager.GONE);
                tabLayout.setVisibility(TabLayout.GONE);
                break;
            case R.id.nav_add_file:
                viewPager.setVisibility(ViewPager.GONE);
                tabLayout.setVisibility(TabLayout.GONE);

                sdBrowserFragment = new SDBrowserFragment();
                String comicsPath = preferences.getString(Constants.COMICS_PATH_KEY, Environment.getExternalStorageDirectory().getAbsolutePath());
                bundle.putString(comicsPath, Constants.COMIC_PATH_KEY);
                sdBrowserFragment.setArguments(bundle);
                fragment = sdBrowserFragment;
                importAction.setVisible(false);
                selectAllAction.setVisible(true);

                drawerItemSelected = R.id.nav_add_file;
                currentFragment = sdBrowserFragment;
                break;
            case R.id.nav_settings:
                viewPager.setVisibility(ViewPager.GONE);
                tabLayout.setVisibility(TabLayout.GONE);
                break;
        }

        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment).commit();
        }else if(currentFragment != null){
            fragmentManager.beginTransaction()
                    .remove(currentFragment).commit();
            currentFragment = null;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                selectAllAction.setVisible(false);
                importAction.setVisible(true);
                sdBrowserFragment.selectAll();
                return true;
            case R.id.action_import:
                sdBrowserFragment.importFiles();
                return true;
        }

        return false;
    }

    private void insertFiles(ArrayList<File> files){
        final HashMap<String, Integer> supportedExtensions = Constants.getSupportedExtensions();

        for(int i = 0; i < files.size(); i++){
            File file = files.get(i);

            if(file.isDirectory()){
                File[] validFiles = file.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String filename) {
                        String ext = FileUtils.getFileExtension(filename);
                        File file = new File(dir.getPath(), filename);

                        return filename.indexOf(".") != 0 && (supportedExtensions.containsKey(ext.toLowerCase()) || file.isDirectory());
                    }
                });

                insertFiles(new ArrayList<>(Arrays.asList(validFiles)));
                continue;
            }

            String ext = FileUtils.getFileExtension(file.getName());
            boolean isBook = Objects.equals(Constants.EPUB_EXTENSION, ext.toLowerCase()) || Objects.equals(Constants.PDF_EXTENSION, ext.toLowerCase());

            if(!dbHelper.file.existsFile(file.getPath()))
                dbHelper.file.insertFile(file.getPath(), isBook);
        }
    }
}
