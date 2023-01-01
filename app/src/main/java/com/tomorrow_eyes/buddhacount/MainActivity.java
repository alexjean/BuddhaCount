package com.tomorrow_eyes.buddhacount;

import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.tomorrow_eyes.buddhacount.databinding.ActivityMainBinding;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private MyViewModel viewModel;
    private OrientationEventListener mOrientationListener;
    private String DEBUG_TAG="Alex_Debug";
    //private int origin_brightness;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
        viewModel.readConfig(this);

        setSupportActionBar(binding.toolbar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(viewModel.getTitle(this));

//        origin_brightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, 0);

        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.v(DEBUG_TAG, "Orientation changed to " + orientation);
//                Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, 1);
            }
        };
        if (mOrientationListener.canDetectOrientation() == true) {
            Log.v(DEBUG_TAG, "Can detect orientation");
            mOrientationListener.enable();
        } else {
            Log.v(DEBUG_TAG, "Cannot detect orientation");
            mOrientationListener.disable();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.sound_switch).setChecked(viewModel.getWoodenKnocker());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if      (id == R.id.item_leave) finish();
        else if (id == R.id.action_settings) {
            Fragment navFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
            if (navFragment == null) return false;
            Fragment frag1 = navFragment.getChildFragmentManager().getFragments().get(0);
            if (frag1 instanceof RecordFragment) return false;
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.action_FirstFragment_to_recordFragment);
        }
        else if (id == R.id.sound_switch) {
            boolean b1 = !viewModel.getWoodenKnocker();
            viewModel.setWoodenKnocker(b1);
            viewModel.writeConfig(this);
            item.setChecked(b1);
        }
        else return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOrientationListener.disable();
        //Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS, origin_brightness);
    }
}