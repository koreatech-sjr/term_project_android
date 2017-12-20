package com.example.rock.todo;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static java.util.Collections.swap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RegisterSchedule.OnCompleteListener, AdjustSchedule.OnCompleteListener, EasyPermissions.PermissionCallbacks {
    private SQLiteDatabase db;
    DBHelper dbHelper;
    ArrayAdapter<String> mAdapter;
    ListView lstTask;
    ArrayList image_details;
    ListView lv1 = null;
    CustomListAdapter ca;
    Cursor cursor;

    //calendar
    GoogleAccountCredential mCredential;
    private Button mCallApiButton;
    ProgressDialog mProgress;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    //날짜
    long now;
    Integer nowDate;
    LinearLayout draw;
    DrawerColor co;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        co = new DrawerColor();
        now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String getDate = sdf.format(date);
        nowDate = Integer.parseInt(getDate);

        LinearLayout draw = (LinearLayout)findViewById(R.id.drawer);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //아래코드는 하단에 메시지 띄우기
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                show();
                lv1 = (ListView) findViewById(R.id.lstTask);
                lv1.setAdapter(sortedData());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //todo관련
        dbHelper = new DBHelper(this);
        lstTask = (ListView)findViewById(R.id.lstTask);
        loadTaskList();
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }

        image_details = getListData();
        lv1 = (ListView) findViewById(R.id.lstTask);
        ca = new CustomListAdapter(this, image_details);
        lv1.setAdapter(ca);
        lv1.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //
                // start dragging
                //
                CustomListAdapter.ViewHolder vh = (CustomListAdapter.ViewHolder) view.getTag();

                final int touchedX = (int) (vh.lastTouchedX + 0.5f);
                final int touchedY = (int) (vh.lastTouchedY + 0.5f);

                view.startDrag(null, new View.DragShadowBuilder(view) {
                    @Override
                    public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
                        super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
                        shadowTouchPoint.x = touchedX;
                        shadowTouchPoint.y = touchedY;
                    }

                    @Override
                    public void onDrawShadow(Canvas canvas) {
                        super.onDrawShadow(canvas);
                    }
                }, view, 0);

                view.setVisibility(View.INVISIBLE);

                return true;
            }
        });

        lv1.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (event.getAction() == DragEvent.ACTION_DROP) {
                    //
                    // finish dragging
                    //
                    View view = (View) event.getLocalState();
                    view.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        lv1.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                                       @Override
                                       public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                                           sortedData().getItem(position);
                                           showAdjust(position);
                                       }
                                   });
            SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(lv1,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                ca = sortedData();
                                for (int position : reverseSortedPositions) {

                                    ArrayList<String> taskList = dbHelper.getTaskList();
                                    ArrayList<String> taskSubs = dbHelper.getTaskSubs();
                                    ArrayList<String> taskLabels = dbHelper.getTaskLabels();
                                    ArrayList<String> taskYears = dbHelper.getTaskYear();
                                    ArrayList<String> taskMonths = dbHelper.getTaskMonth();
                                    ArrayList<String> taskDays = dbHelper.getTaskDays();
                                    ArrayList<String> uidList = dbHelper.getTaskUid();
                                    ArrayList<NewsItem> results = new ArrayList<NewsItem>();

                                    for(int i=0; i<taskList.size(); i++){
                                        String begin = nowDate+"";
                                        String end = taskYears.get(i)+taskMonths.get(i)+taskDays.get(i);
                                        NewsItem newsData = new NewsItem();
                                        newsData.setHeadline(taskList.get(i));
                                        newsData.setReporterName(taskSubs.get(i));
                                        newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
                                        newsData.setDates(Integer.parseInt(taskYears.get(i))*10000+Integer.parseInt(taskMonths.get(i))*100+Integer.parseInt(taskDays.get(i)));
                                        try {
                                            newsData.setDday(String.valueOf(diffOfDate(begin, end)));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        results.add(newsData);
                                    }

                                    for(int i=0; i<results.size()-1; i++) {
                                        int min = i;
                                        for(int j=i+1; j<results.size(); j++) {
                                            if(results.get(j).getDates() < results.get(min).getDates()) { //오름차순
                                                min = j;
                                            }
                                        }
                                        swap(results, min, i);
                                        swap(uidList, min, i);
                                    }

                                    ca.remove(position);

                                    db.execSQL(String.format("DELETE FROM Task WHERE _id = %s", uidList.get(position)));
                                }

                                lv1 = (ListView) findViewById(R.id.lstTask);
                                lv1.setAdapter(sortedData());
                            }
                        });
        lv1.setOnTouchListener(touchListener);
        lv1.setOnScrollListener(touchListener.makeScrollListener());
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        lv1.setAdapter(sortedData());
    }
    private void loadTaskList() {

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        ArrayList<String> taskList = dbHelper.getTaskList();
        ArrayList<String> taskSubs = dbHelper.getTaskSubs();
        ArrayList<String> taskLabels = dbHelper.getTaskLabels();
        ArrayList<String> taskYears = dbHelper.getTaskYear();
        ArrayList<String> taskMonths = dbHelper.getTaskMonth();
        ArrayList<String> taskDays = dbHelper.getTaskDays();

        int pos = 6;
        int id = item.getItemId();
        switch(id){
            case R.id.black:
                sortedData();
                image_details = getListSelectLabel("Black");
                ca = new CustomListAdapter(this, image_details);
                TextView ts = (TextView) findViewById(R.id.drawer_title);
                ts.setTextColor(Color.rgb(255,255,255));
                TextView ts2 = (TextView) findViewById(R.id.drawer_cont);
                ts2.setTextColor(Color.rgb(255,255,255));
                lv1.setAdapter(ca);
                pos=0;
                break;
            case R.id.red:
                sortedData();
                image_details = getListSelectLabel("Red");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                pos=1;
                break;
            case R.id.yellow:
                sortedData();
                image_details = getListSelectLabel("Yellow");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                pos=2;
                break;
            case R.id.green:
                sortedData();
                image_details = getListSelectLabel("Green");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                pos=3;
                break;
            case R.id.blue:
                sortedData();
                image_details = getListSelectLabel("Blue");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                pos=4;
                break;
            case R.id.purple:
                sortedData();
                image_details = getListSelectLabel("Purple");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                pos=5;
                break;
            case R.id.all:
                image_details = getListData();
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                pos=6;
                TextView ts3 = (TextView) findViewById(R.id.drawer_title);
                ts3.setTextColor(Color.rgb(0,0,0));
                TextView ts4 = (TextView) findViewById(R.id.drawer_cont);
                ts4.setTextColor(Color.rgb(0,0,0));
                break;
            case R.id.gc:
                getResultsFromApi();
                break;
        }

        draw = findViewById(R.id.drawer);
        draw.setBackgroundColor((co.getColor(pos)));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onInputedData(String title, String contents, String label, String year, String month, String day) {
        Toast.makeText(getApplicationContext(), title+contents+label+year+month+day, Toast.LENGTH_LONG).show();

        db.execSQL("INSERT INTO Task VALUES (null, '" + title + "', '" + contents + "', '" + label + "', '" + year + "', '" + month + "', '" + day + "');");
        image_details = getListData();
        lv1 = (ListView) findViewById(R.id.lstTask);
        ca = new CustomListAdapter(this, image_details);
        lv1.setAdapter(sortedData());
    }
    @Override
    public void onAdjustInputedData(String title, String contents, String label, String year, String month, String day, String where) {
        Toast.makeText(getApplicationContext(), title+contents+label+year+month+day, Toast.LENGTH_LONG).show();

        db.execSQL("UPDATE Task SET TaskName = '"+title+"', TaskContents = '"+contents+"', TaskLabel = '"+label+"', TaskYear = '"+year+"', TaskMonth = '"+month+"', TaskDay = '"+day+"' WHERE TaskName = '"+where+"';");
        image_details = getListData();
        lv1 = (ListView) findViewById(R.id.lstTask);
        ca = new CustomListAdapter(this, image_details);
        lv1.setAdapter(sortedData());
    }

    void show()
    {
        RegisterSchedule newFragment = new RegisterSchedule();
        newFragment.show(getFragmentManager(), "dialog"); //"dialog"라는 태그를 갖는 프래그먼트를 보여준다.
    }
    void showAdjust(int position){
        ArrayList<String> taskList = dbHelper.getTaskList();
        ArrayList<String> taskSubs = dbHelper.getTaskSubs();
        ArrayList<String> taskLabels = dbHelper.getTaskLabels();
        ArrayList<String> taskYears = dbHelper.getTaskYear();
        ArrayList<String> taskMonths = dbHelper.getTaskMonth();
        ArrayList<String> taskDays = dbHelper.getTaskDays();
        ArrayList<NewsItem> results = new ArrayList<NewsItem>();
        System.out.println(taskList);
        for(int i=0; i<taskList.size(); i++){
            NewsItem newsData = new NewsItem();
            newsData.setHeadline(taskList.get(i));
            newsData.setReporterName(taskSubs.get(i));
            newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
            newsData.setDates(Integer.parseInt(taskYears.get(i))*10000+Integer.parseInt(taskMonths.get(i))*100+Integer.parseInt(taskDays.get(i)));
            String begin = nowDate+"";
            String end = taskYears.get(i)+taskMonths.get(i)+taskDays.get(i);

            newsData.setHeadline(taskList.get(i));
            newsData.setReporterName(taskSubs.get(i));
            newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
            newsData.setDates(Integer.parseInt(taskYears.get(i))*10000+Integer.parseInt(taskMonths.get(i))*100+Integer.parseInt(taskDays.get(i)));
            try {
                newsData.setDday(String.valueOf(diffOfDate(begin, end)));
            } catch (Exception e) {
                e.printStackTrace();
            }


            results.add(newsData);
        }

        for(int i=0; i<results.size()-1; i++) {
            int min = i;
            for(int j=i+1; j<results.size(); j++) {
                if(results.get(j).getDates() < results.get(min).getDates()) { //오름차순
                    min = j;
                }
            }
            System.out.println("k"+taskList.get(min));
            swap(taskList, min, i);
            swap(taskSubs, min, i);
            swap(taskLabels, min, i);
            swap(taskYears, min, i);
            swap(taskMonths, min, i);
            swap(taskDays, min, i);
            System.out.println("after"+taskList.get(min));
        }
        System.out.println(taskList);
        System.out.println(taskList.get(position)+
                taskSubs.get(position)+
                taskLabels.get(position)+
                taskYears.get(position)+
                taskMonths.get(position)+
                taskDays.get(position));
        AdjustSchedule newFragment = new AdjustSchedule( taskList.get(position),
                taskSubs.get(position),
                taskLabels.get(position),
                taskYears.get(position),
                taskMonths.get(position),
                taskDays.get(position)
                );
        newFragment.show(getFragmentManager(), "dialog"); //"dialog"라는 태그를 갖는 프래그먼트를 보여준다.
    }
    private ArrayList getListData() {
        ArrayList<String> taskList = dbHelper.getTaskList();
        ArrayList<String> taskSubs = dbHelper.getTaskSubs();
        ArrayList<String> taskLabels = dbHelper.getTaskLabels();
        ArrayList<String> taskYears = dbHelper.getTaskYear();
        ArrayList<String> taskMonths = dbHelper.getTaskMonth();
        ArrayList<String> taskDays = dbHelper.getTaskDays();
        ArrayList<NewsItem> results = new ArrayList<NewsItem>();
        for(int i=0; i<taskList.size(); i++){
            NewsItem newsData = new NewsItem();
            newsData.setHeadline(taskList.get(i));
            newsData.setReporterName(taskSubs.get(i));
            newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));

            String begin = nowDate+"";
            String end = taskYears.get(i)+taskMonths.get(i)+taskDays.get(i);

            newsData.setHeadline(taskList.get(i));
            newsData.setReporterName(taskSubs.get(i));
            newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
            newsData.setDates(Integer.parseInt(taskYears.get(i))*10000+Integer.parseInt(taskMonths.get(i))*100+Integer.parseInt(taskDays.get(i)));
            try {
                newsData.setDday(String.valueOf(diffOfDate(begin, end)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            results.add(newsData);
        }

        // Add some more dummy data for testing
        return results;
    }
    private ArrayList getListSelectLabel(String label){
        ArrayList<String> taskList = dbHelper.getTaskList();
        ArrayList<String> taskSubs = dbHelper.getTaskSubs();
        ArrayList<String> taskLabels = dbHelper.getTaskLabels();
        ArrayList<String> taskYears = dbHelper.getTaskYear();
        ArrayList<String> taskMonths = dbHelper.getTaskMonth();
        ArrayList<String> taskDays = dbHelper.getTaskDays();
        ArrayList<NewsItem> results = new ArrayList<NewsItem>();
        System.out.println("DFDFDF : "+label);
        for(int i=0; i<taskList.size(); i++){
            System.out.println("IN : "+taskLabels.get(i).toString());
            if(taskLabels.get(i).toString().equals(label)){
                System.out.println("맞음");
                NewsItem newsData = new NewsItem();
                newsData.setHeadline(taskList.get(i));
                newsData.setReporterName(taskSubs.get(i));
                newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
                String begin = nowDate+"";
                String end = taskYears.get(i)+taskMonths.get(i)+taskDays.get(i);
                newsData.setHeadline(taskList.get(i));
                newsData.setReporterName(taskSubs.get(i));
                newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
                newsData.setDates(Integer.parseInt(taskYears.get(i))*10000+Integer.parseInt(taskMonths.get(i))*100+Integer.parseInt(taskDays.get(i)));
                try {
                    newsData.setDday(String.valueOf(diffOfDate(begin, end)));
                } catch (Exception e) {
                    e.printStackTrace();
                }


                results.add(newsData);
            }
        }

        // Add some more dummy data for testing
        return results;
    }
    protected void onResume() {
        super.onResume();
        image_details = getListData();
        lv1 = (ListView) findViewById(R.id.lstTask);
        ca = new CustomListAdapter(this, image_details);
        lv1.setAdapter(sortedData());
    }

    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            Toast.makeText(MainActivity.this, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {
            new MakeRequestTask(mCredential).execute();
        }
    }
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(MainActivity.this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }
                eventStrings.add(
                        String.format("%s (%s)", event.getSummary(), start));
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                Toast.makeText(MainActivity.this, "No results returned." , Toast.LENGTH_LONG).show();
            } else {
                //TODO: 추가하기
                System.out.println(output);
                String[] data;
                int size = output.size();
                for(int i=0; i<output.size(); i++) {
                    data = output.get(i).split(" ");
                    String title = data[0];
                    boolean flag = true;
                    for(int j=0; j<dbHelper.getTaskList().size(); j++){
                        if(title.equals(dbHelper.getTaskList().get(j).toString())){
                            System.out.println("k");
                            flag = false;
                            size--;
                        }
                    }
                    if(flag){
                        String date = data[1];

                        date = data[1].replace(")","");
                        String year = date.substring(1,5);
                        String month = date.substring(6,8);
                        String day = date.substring(9,11);


                        String contents = "";
                        String label = "";

                        month.replace("0","");
                        day.replace("0", "");

                        db.execSQL("INSERT INTO Task VALUES (null, '" + title + "', '" + contents + "', '" + label + "', '" + year + "', '" + month + "', '" + day + "');");
                    }
                }
                ArrayList<String> taskList = dbHelper.getTaskList();
                ArrayList<String> taskSubs = dbHelper.getTaskSubs();
                ArrayList<String> taskLabels = dbHelper.getTaskLabels();
                ArrayList<String> taskYears = dbHelper.getTaskYear();
                ArrayList<String> taskMonths = dbHelper.getTaskMonth();
                ArrayList<String> taskDays = dbHelper.getTaskDays();
                ArrayList<NewsItem> results = new ArrayList<NewsItem>();

                for(int i=0; i<taskList.size(); i++){
                    NewsItem newsData = new NewsItem();
                    newsData.setHeadline(taskList.get(i));
                    newsData.setReporterName(taskSubs.get(i));
                    newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
                    String begin = nowDate+"";
                    String end = taskYears.get(i)+taskMonths.get(i)+taskDays.get(i);
                    newsData.setHeadline(taskList.get(i));
                    newsData.setReporterName(taskSubs.get(i));
                    newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
                    newsData.setDates(Integer.parseInt(taskYears.get(i))*10000+Integer.parseInt(taskMonths.get(i))*100+Integer.parseInt(taskDays.get(i)));
                    try {
                        newsData.setDday(String.valueOf(diffOfDate(begin, end)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    results.add(newsData);
                }
                image_details = results;

                lv1.setAdapter(sortedData());
                Toast.makeText(MainActivity.this,"Google Calendar에서 총 "+size+"개의 일정을 가져옴.(중복제외)", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    Toast.makeText(MainActivity.this,"The following error occurred:\n"
                            + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this,"Request cancelled.", Toast.LENGTH_LONG).show();
            }
        }
    }
    public CustomListAdapter sortedData(){
        CustomListAdapter sca;
        ArrayList<String> taskList = dbHelper.getTaskList();
        ArrayList<String> taskSubs = dbHelper.getTaskSubs();
        ArrayList<String> taskLabels = dbHelper.getTaskLabels();
        ArrayList<String> taskYears = dbHelper.getTaskYear();
        ArrayList<String> taskMonths = dbHelper.getTaskMonth();
        ArrayList<String> taskDays = dbHelper.getTaskDays();
        ArrayList<String> uidList = dbHelper.getTaskUid();
        ArrayList<NewsItem> results = new ArrayList<NewsItem>();

        for(int i=0; i<taskList.size(); i++){
            String begin = nowDate+"";
            String end = taskYears.get(i)+taskMonths.get(i)+taskDays.get(i);
            NewsItem newsData = new NewsItem();
            newsData.setHeadline(taskList.get(i));
            newsData.setReporterName(taskSubs.get(i));
            newsData.setDate(taskYears.get(i)+". "+taskMonths.get(i)+". "+taskDays.get(i));
            newsData.setDates(Integer.parseInt(taskYears.get(i))*10000+Integer.parseInt(taskMonths.get(i))*100+Integer.parseInt(taskDays.get(i)));
            try {
                newsData.setDday(String.valueOf(diffOfDate(begin, end)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            results.add(newsData);
        }

        for(int i=0; i<results.size()-1; i++) {
            int min = i;
            for(int j=i+1; j<results.size(); j++) {
                if(results.get(j).getDates() < results.get(min).getDates()) { //오름차순
                    min = j;
                }
            }
            swap(results, min, i);
        }

        sca = new CustomListAdapter(MainActivity.this, results);
        return sca;
    }
    public static long diffOfDate(String begin, String end) throws Exception
    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

        Date beginDate = formatter.parse(begin);
        Date endDate = formatter.parse(end);

        long diff = endDate.getTime() - beginDate.getTime();
        long diffDays = diff / (24 * 60 * 60 * 1000);

        return diffDays;
    }
}
