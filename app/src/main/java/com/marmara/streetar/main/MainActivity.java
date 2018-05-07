package com.marmara.streetar.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.marmara.streetar.R;
import com.marmara.streetar.main.pages.FavoritesFragment;
import com.marmara.streetar.main.pages.HomeFragment;
import com.marmara.streetar.main.pages.SettingsFragment;

import static com.marmara.streetar.main.MainPresenter.location;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainView{

    Fragment fragment;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        fragment = new HomeFragment();
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.camera_container_layout, fragment);
            ft.commit();
        }
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        MainPresenter mainPresenter = new MainPresenter(this);
        //noinspection SimplifiableIfStatement
        if (id == R.id.all) {
            Toast.makeText(this,"All place types are loading...",Toast.LENGTH_SHORT).show();
            MainPresenter.arPoints = mainPresenter.getPlaces("",MainPresenter.location);
            return true;
        }if (id == R.id.restaurant) {
            Toast.makeText(this,"Restaurants are loading...",Toast.LENGTH_SHORT).show();
            MainPresenter.arPoints = mainPresenter.getPlaces("restaurant",MainPresenter.location);
            return true;
        }if (id == R.id.bank){
            Toast.makeText(this,"Banks are loading...",Toast.LENGTH_SHORT).show();
            MainPresenter.arPoints = mainPresenter.getPlaces("bank",MainPresenter.location);if (id == R.id.cafe) {
                return true;
            }
        }if (id == R.id.school) {
            Toast.makeText(this,"Schools are loading...",Toast.LENGTH_SHORT).show();
            MainPresenter.arPoints = mainPresenter.getPlaces("school",MainPresenter.location);
            return true;
        }if (id == R.id.hospital) {
            Toast.makeText(this,"Hospitals are loading...",Toast.LENGTH_SHORT).show();
            MainPresenter.arPoints = mainPresenter.getPlaces("hospital",MainPresenter.location);
            return true;
        }if (id == R.id.cafe) {
            Toast.makeText(this,"Cafes are loading...",Toast.LENGTH_SHORT).show();
            MainPresenter.arPoints = mainPresenter.getPlaces("cafe",MainPresenter.location);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_home:
                fragment = new HomeFragment();
                break;
            case R.id.nav_favorites:
                fragment = new FavoritesFragment();
                break;
            case R.id.nav_settings:
                fragment = new SettingsFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.addToBackStack("fragment");
            ft.replace(R.id.camera_container_layout, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
