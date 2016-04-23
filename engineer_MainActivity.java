package com.cazaayan.tenzfree.Engineer.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cazaayan.tenzfree.Activity.AboutTenzfreeActivity;
import com.cazaayan.tenzfree.Activity.RegistrationActivity;
import com.cazaayan.tenzfree.Adapter.DrawerListAdapter_Engineer;
import com.cazaayan.tenzfree.Engineer.Adapter.engineer_ViewPagerAdapter;
import com.cazaayan.tenzfree.Engineer.Fragments.Fragment_Collection;
import com.cazaayan.tenzfree.Engineer.Fragments.Fragment_ServiceRequests;
import com.cazaayan.tenzfree.Engineer.Services.AsyncGetAllTask_Engineer;
import com.cazaayan.tenzfree.R;
import com.cazaayan.tenzfree.Services.AsyncGCMData;
import com.cazaayan.tenzfree.Services.AsyncRetrieveLogo;
import com.cazaayan.tenzfree.Services.RestClient;
import com.cazaayan.tenzfree.Utilities.ApplicationConstants;
import com.cazaayan.tenzfree.Utilities.ApplicationLoader;
import com.cazaayan.tenzfree.Utilities.ApplicationServices;
import com.cazaayan.tenzfree.Utilities.Utils;
import com.cazaayan.tenzfree.model.Login;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.readystatesoftware.viewbadger.BadgeView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


public class engineer_MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    Toolbar toolbar;
    TabLayout tabLayout;

    //for drawer
    DrawerLayout drawerLayout;
    ListView mDrawerList;
    RelativeLayout mDrawerHeader;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private TextView mDrawerUserEmailID;
    private TextView mDrawerClientID;
    private ImageView userProfileImage;

    //for location fetching
    ArrayList<String> mLocationData = new ArrayList<String>();
    private Timer timer;
    private long UPDATE_INTERVAL;

    //for notification
    private GoogleCloudMessaging gcm;
    private String sRegid;
    private Context context;

    //for notification counter
    private BadgeView badgeNotification;
    private Menu menu;
    private MenuItem notification_menu;
    private ImageView imgMessagesIcon;


    private android.support.v4.app.FragmentManager fragManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engineer_main);
        initToolBar();
        initUI();
        setDrawer();
        setUIListeners();
    }


    private void initToolBar() {
        toolbar = (Toolbar) findViewById(R.id.tab_toolbar_engineer);
        toolbar.setTitle(getResources().getString(R.string.title_activity_Engineer));
        toolbar.setTitleTextColor(Color.BLACK);
        toolbar.setNavigationIcon(R.drawable.menu);
        setSupportActionBar(toolbar);
    }

    private void setDrawer() {

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerHeader = (RelativeLayout) findViewById(R.id.Drawer_Header);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        mDrawerUserEmailID = (TextView)findViewById(R.id.UserEmailIDTxtDrawer);
        mDrawerClientID = (TextView)findViewById(R.id.UserIDTxtDrawer);
        userProfileImage = (ImageView) findViewById(R.id.userProfileImage);

        setProfileImage();
        mDrawerUserEmailID.setText(ApplicationLoader.getPreferences().getEmail());
        if(ApplicationLoader.getPreferences().getClientID().length()>0) {
            mDrawerClientID.setText("ID: " + ApplicationLoader.getPreferences().getClientID());
        }
        else {
            mDrawerClientID.setVisibility(View.GONE);
        }

        String[] mDrawerItemArray = {"Rate Tenzfree", "Tell a Friend","Feedback", "Support", "About"};
        mDrawerList.setAdapter(new DrawerListAdapter_Engineer(mDrawerItemArray, this));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void setProfileImage() {
        try {

            String TenzfreeCkientId = ApplicationLoader.getPreferences().getGeneratedId();
            AsyncRetrieveLogo taskGetLogo = new AsyncRetrieveLogo(engineer_MainActivity.this, TenzfreeCkientId);
            taskGetLogo.execute();

        } catch (Exception e) {
            Log.e("Tag",e.getMessage());
            Utils.extractLogToFile();
        }
    }

    private void initUI() {
        Utils.sendgoogleanalytis("Employee Dashboard Page", getApplicationContext());
        context = getApplicationContext();

        timer = new Timer();
        mLocationData = Utils.populateDefaultLocation(engineer_MainActivity.this);
        UpdateWithNewLocation(mLocationData);

        viewPager = (ViewPager) findViewById(R.id.viewpager_engineer);
        setupViewPager(viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabsLayout_engineer);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.primary_500));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //tabLayout.setPadding(2, 0, 2, 0);
    }

    private void UpdateWithNewLocation(final ArrayList<String> mLocationData) {

        UPDATE_INTERVAL = 300000; //TimerTask will run every 300000ms(5m)
        final String mCountry;
        final String mState;
        final String mCity;
        final String mStreetAddress;
        final String mPincode;
        final String mLatitude;
        final String mLongitude;

        try {
            mCountry = mLocationData.get(0);
            mState = mLocationData.get(1);
            mCity = mLocationData.get(2);
            mPincode = mLocationData.get(3);
            mStreetAddress = mLocationData.get(4);
            mLatitude = mLocationData.get(5);
            mLongitude = mLocationData.get(6);

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (mLocationData.size() > 0) {
                        sendLocationRequest(mCountry, mState, mCity, mStreetAddress, mPincode, mLatitude, mLongitude);
                    }

                }
            }, 0, UPDATE_INTERVAL);
        } catch (Exception e) {
            Log.e("Tag",e.getMessage());
            Utils.extractLogToFile();
        }
    }

    private void sendLocationRequest(String mCountry, String mState, String mCity, String mStreetAddress, String mPincode, String mLatitude, String mLongitude) {

        String sResponse = "";
        HashMap<String, String> formValue = new HashMap<String, String>();
        formValue.put("TenzfreeClientID", ApplicationLoader.getPreferences().getGeneratedId());
        formValue.put("EmpID", Login.getInstance().getEmpID());
        formValue.put("Session", ApplicationLoader.getPreferences().getSessionID());
        formValue.put("CurrentLocation", mStreetAddress);
        formValue.put("Country", mCountry);
        formValue.put("State", mState);
        formValue.put("City", mCity);
        formValue.put("Pincode", mPincode);
        formValue.put("latitude", mLatitude);
        formValue.put("longitude", mLongitude);

        sResponse = RestClient.postData(ApplicationServices.FIND_LOCATION, formValue);
    }

    private void setUIListeners() {
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        new Fragment_ServiceRequests();
                        break;
                    case 1:
                        new Fragment_Collection();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * method for tab creation
     *
     * @param viewPager
     */
    private void setupViewPager(ViewPager viewPager) {
        engineer_ViewPagerAdapter adapter = new engineer_ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new Fragment_ServiceRequests(), "Service Requests");
        adapter.addFrag(new Fragment_Collection(), "Collections");
        viewPager.setAdapter(adapter);
        viewPager.getAdapter().notifyDataSetChanged();
    }


    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (drawerLayout.isDrawerVisible(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main_engineer, menu);

        notification_menu = menu.findItem(R.id.notification_engineer);

        notification_menu.setActionView(R.layout.notification_messages_indicator);

        if (notification_menu.getActionView().findViewById(R.id.imgMessagesIcon) != null) {
            imgMessagesIcon = ((ImageView) notification_menu.getActionView().findViewById(R.id.imgMessagesIcon));

            imgMessagesIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //  ((SlidingMenuBase)view.getContext()).onOptionsItemSelected(msgItem);
                    if (!Utils.isInternetConnected()) {
                        Utils.showInternetConnectionDialog(engineer_MainActivity.this);
                    } else {
                        new AsyncGetAllTask_Engineer(engineer_MainActivity.this).execute();
                        Utils.enterWindowAnimation(engineer_MainActivity.this);
                    }
                }
            });
            updateMessagesBadge(0);
        }
        return true;
    }

    /**
     * for Notification counter updation
     *
     * @param badgeCount
     * @return
     */
    private void updateMessagesBadge(int badgeCount) {
        if (notification_menu != null) {
            ImageView imgMessagesIcon = ((ImageView) notification_menu.getActionView().findViewById(R.id.imgMessagesIcon));
            if (badgeNotification == null && badgeCount > 0) {
                badgeNotification = new BadgeView(this, imgMessagesIcon);
                badgeNotification.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                badgeNotification.setBadgeMargin(0);
                badgeNotification.setTextSize(12);
                badgeNotification.setText(String.valueOf(badgeCount));
                badgeNotification.show();
            } else if (badgeNotification != null && badgeCount > 0) {
                badgeNotification.setText(String.valueOf(badgeCount));
                badgeNotification.show();
            } else if (badgeNotification != null && badgeCount == 0) {
                badgeNotification.hide();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case android.R.id.home:
                return true;
            case R.id.notification_engineer:
                if (!Utils.isInternetConnected()) {
                    Utils.showInternetConnectionDialog(engineer_MainActivity.this);
                } else {
                    new AsyncGetAllTask_Engineer(engineer_MainActivity.this).execute();
                    Utils.enterWindowAnimation(engineer_MainActivity.this);
                }
                return true;

            case R.id.action_logout:
                ApplicationLoader.getPreferences().setLogIn(false);
                i = new Intent(engineer_MainActivity.this, RegistrationActivity.class);
                startActivity(i);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            navigateTo(position);
        }
    }

    private void navigateTo(int position) {
        //drawerLayout.closeDrawer(mDrawerList);
        drawerLayout.closeDrawer(Gravity.LEFT);
        switch (position) {
            case 0://Rate Us
                Utils.RateUs(engineer_MainActivity.this);
                break;

            case 1://share
                Utils.shareApp(engineer_MainActivity.this);
                break;

            case 2://send feedback
                Utils.sendFeedbackMail(engineer_MainActivity.this);
                Utils.sendgoogleanalytis("App Feedback Page", getApplicationContext());
                break;

            case 3://Support
                Utils.makeCall(engineer_MainActivity.this,"8080300200");
                break;

            case 4://about
                Intent i = new Intent(engineer_MainActivity.this,AboutTenzfreeActivity.class);
                startActivity(i);
                Utils.sendgoogleanalytis("App About Page", getApplicationContext());
                break;
        }

    }


    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        moveTaskToBack(true);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String status = ApplicationLoader.getPreferences().getGCMStatus();
        if ((status == null) || (status.equalsIgnoreCase("false"))) {
            AsyncGCMData task = new AsyncGCMData(engineer_MainActivity.this, "Employee", ApplicationLoader.getPreferences().getEngineerId(), sRegid);
            task.execute(sRegid);
        }
    }

    //Notification Settings

    @Override
    protected void onStart() {

        if (checkPlayServices("ONSTART")) {
            gcm = GoogleCloudMessaging.getInstance(this);
            sRegid = getRegistrationId(context);

            if (sRegid == null || sRegid.trim().length() == 0) {

                new AsyncGetIDFromGCM().execute();

            } else {

                String sRegIDString = ApplicationLoader.getPreferences().getGCMId();
                String[] splited = sRegIDString.split("\\|");

                String sRegID = splited[0];
                String sType = splited[1];
                String mLastLogin = ApplicationLoader.getPreferences().getLastLogin();
                String mCurrentLogin = ApplicationLoader.getPreferences().getGeneratedId();
                if (!sType.equalsIgnoreCase("Employee") || !mLastLogin.equalsIgnoreCase(mCurrentLogin)) {
                    AsyncGCMData task = new AsyncGCMData(engineer_MainActivity.this, "Employee", ApplicationLoader.getPreferences().getEngineerId(), sRegID);
                    task.execute(sRegID);
                }
            }
        }
        super.onStart();
    }

    private String getRegistrationId(Context context) {

        String sRegistrationId = ApplicationLoader.getPreferences().getGCMId();
        try {
            if (sRegistrationId.trim().length() == 0 || sRegistrationId == null) {
                Log.i(ApplicationConstants.TAG, "Registration not found.");
                return "";
            }
        } catch (Exception e) {
            Log.e("Tag",e.getMessage());
            Utils.extractLogToFile();
        }
        return sRegistrationId;

    }

    private boolean checkPlayServices(String event) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                if (event.equals("ONSTART")) {
                    GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                            ApplicationConstants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
                }
            } else {
                Log.i(ApplicationConstants.TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    public class AsyncGetIDFromGCM extends AsyncTask<String, Void, String> {


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            //  byte[] result = null;
            String sRegistrationId = "";
            for (int i = 0; i < 3; i++) {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                try {
                    sRegistrationId = gcm.register(ApplicationConstants.SENDER_ID);

                    if (sRegistrationId.trim().length() > 0) {
                        ApplicationLoader.getPreferences().setGCMId(sRegistrationId + "|" + "Employee");
                        i = 3;
                    } else {
                        i = 0;
                    }
                } catch (IOException e) {
                    Log.e("Tag",e.getMessage());
                    Utils.extractLogToFile();
                }
            }
            return sRegistrationId;
        }

        @Override
        protected void onPostExecute(String result) {
                    // TODO Auto-generated method stub
            if (result.trim().length() > 0) {
                String status = ApplicationLoader.getPreferences().getGCMStatus();
                if ((status == null) || (status.equalsIgnoreCase("false"))) {
                    AsyncGCMData task = new AsyncGCMData(engineer_MainActivity.this, "Employee", ApplicationLoader.getPreferences().getEngineerId(), result);
                    task.execute(result);
                }
            }
        }
    }

}
