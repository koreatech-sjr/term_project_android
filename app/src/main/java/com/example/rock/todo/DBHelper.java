package com.example.rock.todo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by rock on 2017. 12. 18..
 */

public class DBHelper extends SQLiteOpenHelper {
    private static String DB_NAME = "JUSTPC";
    private static final int DB_VER = 1;
    public  static  final String DB_TABLE =  "Task";
    public  static final String  DB_COLUMN = "TaskName";
    public  static final String  DB_COLUMN1 = "TaskContents";
    public  static final String  DB_COLUMN2 = "TaskLabel";
    public  static final String  DB_COLUMN3 = "TaskYear";
    public  static final String  DB_COLUMN4 = "TaskMonth";
    public  static final String  DB_COLUMN5 = "TaskDay";
    public DBHelper(Context context) {
        super(context,DB_NAME,null,DB_VER);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

        String str = "CREATE TABLE Task (_id INTEGER PRIMARY KEY AUTOINCREMENT, TaskName TEXT NOT NULL, TaskContents NOT NULL, TaskLabel, TaskYear NOT NULL, TaskMonth NOT NULL, TaskDay NOT NULL);";
        db.execSQL(str);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String str = String.format("DELETE TABLE IF EXISTS %s",DB_TABLE);
        db.execSQL(str);
        onCreate(db);
    }

    public void insertNewTask(String taskName, String taskContents, String taskLabel, String taskYear, String taskMonth, String taskDay){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DB_COLUMN,taskName);
        values.put(DB_COLUMN1,taskContents);
        values.put(DB_COLUMN2,taskLabel);
        values.put(DB_COLUMN3,taskYear);
        values.put(DB_COLUMN4,taskMonth);
        values.put(DB_COLUMN5,taskDay);
        db.insert(DB_TABLE,null,values);
    }

    public void deleteTask(String task){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(DB_TABLE,"TaskName = ?", new String[] {task});
    }

    public ArrayList<String> getTaskList(){
        ArrayList<String> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DB_TABLE,new String[]{DB_COLUMN},null,null,null,null,null);
        int index = cursor.getColumnIndex(DB_COLUMN);
        while(cursor.moveToNext()){
            taskList.add(cursor.getString(index).toString());
        }
        cursor.close();
        return taskList;
    }
    public ArrayList<String> getTaskSubs(){
        ArrayList<String> taskListSubs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(DB_TABLE,new String[]{DB_COLUMN1},null,null,null,null,null);
        int index = cursor.getColumnIndex(DB_COLUMN1);
        while(cursor.moveToNext()){
            taskListSubs.add(cursor.getString(index).toString());
        }
        cursor.close();
        return taskListSubs;
    }
}
