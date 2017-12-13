package com.graftel.www.graftel;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Shorabh on 6/8/2016.
 */
public class MainActivity extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    static DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    static ActionBarDrawerToggle mDrawerToggle;
    TextView t1,t2;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    String call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        getUser();

        if(sp.getBoolean("FirstTime",true))
        {
            editor = sp.edit();
            editor.putBoolean("FirstTime",false);
            editor.commit();
            Toast.makeText(this, "Welcome! "+User.getContactPersonName(), Toast.LENGTH_SHORT).show();
        }
        invalidateOptionsMenu();

        t1 = (TextView)findViewById(R.id.username);
        t2 = (TextView)findViewById(R.id.email);

        /**
         *Setup the DrawerLayout and NavigationView
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff) ;

        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        mFragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();

        /**
         * Setup click events on the Navigation View Items.
         */
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                mDrawerLayout.closeDrawers();
                Intent i;
                switch(menuItem.getItemId())
                {
                    case R.id.profile:
                        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                        Bundle bundle= new Bundle();
                        bundle.putString("call","exist");
                        ProfileFragment profileFragment = new ProfileFragment();
                        profileFragment.setArguments(bundle);
                        fragmentTransaction.replace(R.id.containerView,profileFragment).addToBackStack(null).commit();
                        break;
                    case R.id.orders:
                        if(!TabFragment.viewPager.isShown())
                        {
                            FragmentTransaction xfragmentTransaction = mFragmentManager.beginTransaction();
                            xfragmentTransaction.replace(R.id.containerView,new TabFragment()).commit();
                        }
                        TabFragment.viewPager.setCurrentItem(0);
                        break;
                    case R.id.contact:
                        FragmentTransaction x5fragmentTransaction = mFragmentManager.beginTransaction();
                        x5fragmentTransaction.replace(R.id.containerView,new ContactUsFragment()).addToBackStack(null).commit();
                        break;
                    case R.id.message:
                        FragmentTransaction x3fragmentTransaction = mFragmentManager.beginTransaction();
                        Bundle args = new Bundle();
                        args.putString("Guest","No");
                        SendMessageFragment contactFragment = new SendMessageFragment();
                        contactFragment.setArguments(args);
                        x3fragmentTransaction.replace(R.id.containerView,contactFragment).addToBackStack(null).commit();
                        break;
                    case R.id.certi:
                        FragmentTransaction x4fragmentTransaction = mFragmentManager.beginTransaction();
                        x4fragmentTransaction.replace(R.id.containerView,new ViewCertiFragment()).addToBackStack(null).commit();
                        break;
                    case R.id.services:
                        int hasInternetPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET);
                        if (hasInternetPermission != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[] {Manifest.permission.INTERNET},1);
                            return false;
                        }
                        i = new Intent(getApplicationContext(),ServicesActivity.class);
                        startActivity(i);
                        break;
                    case R.id.settings:
                        FragmentTransaction x6fragmentTransaction = mFragmentManager.beginTransaction();
                        x6fragmentTransaction.replace(R.id.containerView,new LogoutFragment()).addToBackStack(null).commit();
                        break;
                }
                return false;
            }

        });

        /**
         * Setup Drawer Toggle of the Toolbar
         */
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout, toolbar,R.string.app_name,
                R.string.app_name);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        t1.setText(User.getContactPersonName());
        t2.setText(User.getLoginEmail());
    }

    private void getUser() {
        User.setUID(sp.getString("UID",null));
        User.setIsTempUser(sp.getString("IsTempUser",null));
        User.setLoginEmail(sp.getString("LoginEmail",null));
        User.setPassword(sp.getString("Password",null));
        User.setCompanyName(sp.getString("CompanyName",null));
        User.setContactPersonName(sp.getString("ContactPersonName",null));
        User.setShippingAddress(sp.getString("Phone",null));
        User.setPhone(sp.getString("Fax",null));
        User.setFax(sp.getString("ShippingAddress",null));
        User.setAddEmail1(sp.getString("AdditionalEmail1",null));
        User.setAddEmail2(sp.getString("AdditionalEmail2",null));
        User.setAddEmail3(sp.getString("AdditionalEmail3",null));
        User.setAddEmail4(sp.getString("AdditionalEmail4",null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_websearch).setVisible(true).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getSupportFragmentManager().beginTransaction().replace(R.id.containerView,new ViewCertiFragment()).addToBackStack(null).commit();
                return false;
            }
        });
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor = sp.edit();
        editor.putBoolean("FirstTime",true);
        editor.commit();
    }


    // Showing the status in Snackbar
    protected void showSnack(boolean isConnected) {
        String message;
        int color;
        if (isConnected) {
            message = "Connected to Internet";
            color = Color.WHITE;
        } else {
            message = "Not connected to internet";
            color = Color.RED;
        }

        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(color);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register connection status listener
        Graftel.getInstance().setConnectivityListener(this);
    }

    /**
     * Callback will be triggered when there is change in
     * network connection
     */
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        showSnack(isConnected);
    }


}