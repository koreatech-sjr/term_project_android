package com.example.rock.todo;

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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RegisterSchedule.OnCompleteListener, AdjustSchedule.OnCompleteListener {
    private SQLiteDatabase db;
    DBHelper dbHelper;
    ArrayAdapter<String> mAdapter;
    ListView lstTask;
    ArrayList image_details;
    ListView lv1 = null;
    CustomListAdapter ca;
    Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //아래코드는 하단에 메시지 띄우기
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                show();
                lv1 = (ListView) findViewById(R.id.lstTask);
                lv1.setAdapter(ca);
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
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                Object o = lv1.getItemAtPosition(position);
                NewsItem newsData = (NewsItem) o;
                Toast.makeText(MainActivity.this, "Selected :" + " " + newsData, Toast.LENGTH_LONG).show();
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
                                for (int position : reverseSortedPositions) {
                                    System.out.println(position);
                                    ArrayList<String> uidList = dbHelper.getTaskUid();
                                    ca.remove(position);
                                    db.execSQL(String.format("DELETE FROM Task WHERE _id = %s", uidList.get(position)));
                                }

                                image_details = getListData();
                                lv1 = (ListView) findViewById(R.id.lstTask);
                                lv1.setAdapter(ca);
                            }
                        });
        lv1.setOnTouchListener(touchListener);
        lv1.setOnScrollListener(touchListener.makeScrollListener());
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


        int id = item.getItemId();
        switch(id){
            case R.id.black:
                image_details = getListSelectLabel("black");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                break;
            case R.id.red:
                image_details = getListSelectLabel("red");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                break;
            case R.id.yellow:
                image_details = getListSelectLabel("yellow");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                break;
            case R.id.green:
                image_details = getListSelectLabel("green");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                break;
            case R.id.blue:
                image_details = getListSelectLabel("blue");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                break;
            case R.id.purple:
                image_details = getListSelectLabel("purple");
                ca = new CustomListAdapter(this, image_details);
                lv1.setAdapter(ca);
                break;
        }

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
        lv1.setAdapter(ca);

    }
    @Override
    public void onAdjustInputedData(String title, String contents, String label, String year, String month, String day, String where) {
        Toast.makeText(getApplicationContext(), title+contents+label+year+month+day, Toast.LENGTH_LONG).show();

        db.execSQL("UPDATE Task SET TaskName = '"+title+"', TaskContents = '"+contents+"', TaskLabel = '"+label+"', TaskYear = '"+year+"', TaskMonth = '"+month+"', TaskDay = '"+day+"' WHERE TaskName = '"+where+"';");
        image_details = getListData();
        lv1 = (ListView) findViewById(R.id.lstTask);
        ca = new CustomListAdapter(this, image_details);
        lv1.setAdapter(ca);
    }

    void show()
    {
        RegisterSchedule newFragment = new RegisterSchedule();
        newFragment.show(getFragmentManager(), "dialog"); //"dialog"라는 태그를 갖는 프래그먼트를 보여준다.
    }
    void showAdjust(int position){
        AdjustSchedule newFragment = new AdjustSchedule( dbHelper.getTaskList().get(position),
                dbHelper.getTaskSubs().get(position),
                dbHelper.getTaskLabels().get(position),
                dbHelper.getTaskYear().get(position),
                dbHelper.getTaskMonth().get(position),
                dbHelper.getTaskDays().get(position) );
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
                results.add(newsData);
            }
        }

        // Add some more dummy data for testing
        return results;
    }
    protected void onResume() {
        super.onResume();
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
    }
}
