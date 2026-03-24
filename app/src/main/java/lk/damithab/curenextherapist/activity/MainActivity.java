package lk.damithab.curenextherapist.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import lk.damithab.curenextherapist.R;
import lk.damithab.curenextherapist.databinding.ActivityMainBinding;
import lk.damithab.curenextherapist.fragment.AccountFragment;
import lk.damithab.curenextherapist.fragment.HomeFragment;
import lk.damithab.curenextherapist.fragment.ScheduleFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NavigationBarView.OnItemSelectedListener {

    private BottomNavigationView bottomNavigationView;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        bottomNavigationView = binding.bottomNavigationView;

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        Menu bottomNavMenu = bottomNavigationView.getMenu();

        for (int i = 0; i < bottomNavMenu.size(); i++) {
            bottomNavMenu.getItem(i).setChecked(false);
        }

        if (itemId == R.id.nav_home) {
            loadFragment(new HomeFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);

        } else if (itemId == R.id.nav_schedule) {
            loadFragment(new ScheduleFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_schedule).setChecked(true);

        } else if (itemId == R.id.nav_account) {
            loadFragment(new AccountFragment());
            bottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);

        }

        return false;
    }

    public void loadFragment(Fragment fragment) {

        getSupportFragmentManager().beginTransaction().replace(R.id.navContainerView, fragment).commit();

    }
}