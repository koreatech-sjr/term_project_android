package com.example.rock.todo;

import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RegisterSchedule.OnCompleteListener {
    private SQLiteDatabase db;
    DBHelper dbHelper;
    ArrayAdapter<String> mAdapter;
    ListView lstTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DBHelper(this);
        lstTask = (ListView)findViewById(R.id.lstTask);
        loadTaskList();
        try {
            db = dbHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }

        ArrayList image_details = getListData();
        final ListView lv1 = (ListView) findViewById(R.id.lstTask);
        lv1.setAdapter(new CustomListAdapter(this, image_details));
    }

    private void loadTaskList() {
        ArrayList<String> taskList = dbHelper.getTaskList();
        if(mAdapter==null){
            mAdapter = new ArrayAdapter<String>(this,R.layout.row,R.id.task_title,taskList);
            lstTask.setAdapter(mAdapter);
        }else {
            mAdapter.clear();
            mAdapter.addAll(taskList);
            //mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_task:
                show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void deleteTask(View view){
        View parent = (View)view.getParent();
        TextView taskTextView = (TextView)parent.findViewById(R.id.task_title);
        String task = String.valueOf(taskTextView.getText());
        dbHelper.deleteTask(task);
        loadTaskList();
    }

    @Override
    public void onInputedData(String id, String pass, String month, String day) {
        Toast.makeText(this, id+"/"+pass+"/"+month+"/"+day,Toast.LENGTH_LONG).show();
        //dbHelper.insertNewTask(id,pass,month,day);

        db.execSQL("INSERT INTO Task VALUES (null, '" + id+ "', '" + pass + "', '" + month + "', '" + day + "');");
        loadTaskList();

    }
    void show()
    {
       RegisterSchedule newFragment = new RegisterSchedule();
       newFragment.show(getFragmentManager(), "dialog"); //"dialog"라는 태그를 갖는 프래그먼트를 보여준다.
    }
    private ArrayList getListData() {
        ArrayList<String> taskList = dbHelper.getTaskList();

        ArrayList<NewsItem> results = new ArrayList<NewsItem>();
        for(int i=0; i<taskList.size(); i++){
            NewsItem newsData = new NewsItem();
            newsData.setHeadline(taskList.get(i));
            newsData.setReporterName("Pankaj Gupta");
            newsData.setDate("May 26, 2013, 13:35");
            results.add(newsData);
        }


        // Add some more dummy data for testing
        return results;
    }
}
