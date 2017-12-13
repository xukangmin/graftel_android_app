package com.graftel.www.graftel;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


/**
 * Created by Shorabh on 6/20/2016.
 */
public class TempUserActivity extends FragmentActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_act);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, profileFragment).commit();
        }
    }
}
