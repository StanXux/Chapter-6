package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new TodoDbHelper(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
                try {
                    notesAdapter.refresh(loadNotesFromDatabase());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
                try {
                    notesAdapter.refresh(loadNotesFromDatabase());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        recyclerView.setAdapter(notesAdapter);
        try {
            notesAdapter.refresh(loadNotesFromDatabase());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        dbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            try {
                notesAdapter.refresh(loadNotesFromDatabase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Note> loadNotesFromDatabase() throws ParseException {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                BaseColumns._ID,
                TodoContract.TodoEntry.COLUMN_DATE,
                TodoContract.TodoEntry.COLUMN_STATE,
                TodoContract.TodoEntry.COLUMN_CONTENT,
                TodoContract.TodoEntry.COLUMN_LEVEL
        };
        String sortOrder = TodoContract.TodoEntry.COLUMN_LEVEL + " DESC ";
        Cursor cursor = db.query(
                TodoContract.TodoEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (cursor.moveToNext()){
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry._ID));
            String date = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_DATE));
            String state = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_STATE));
            String content = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_CONTENT));
            int level = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_LEVEL));

            //将格林威治时间String转换为Date
            String date_details[] = date.split(" ");
            if (date_details[1] != null && date_details[1].length() > 0) {
                date_details[1] = date_details[1]
                        .replace("Jan","01")
                        .replace("Feb","02")
                        .replace("Mar","03")
                        .replace("Apr","04")
                        .replace("May","05")
                        .replace("Jun","06")
                        .replace("Jul","07")
                        .replace("Aug","08")
                        .replace("Sep","09")
                        .replace("Oct","10")
                        .replace("Nov","11")
                        .replace("Dec","12");
            }
            String date_sum = date_details[5] + "-" + date_details[1] + "-" + date_details[2]
                    + " " + date_details[3];
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date_temp = sdf.parse(date_sum);

            int state_temp = 0;
            if(state.equals("DONE"))
                state_temp = 1;

            Note temp = new Note(itemId);
            temp.setDate(date_temp);
            temp.setState(State.from(state_temp));
            temp.setContent(content);
            temp.setLevel(level);
            notes.add(temp);
        }
        cursor.close();

        if(notes.size() > 0) {
            return notes;
        }
        return null;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(note.id)};

        db.delete(TodoContract.TodoEntry.TABLE_NAME, selection, selectionArgs);
    }

    private void updateNode(Note note) {
        // 更新数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Date date_now = new Date(System.currentTimeMillis());//获取系统当前时间
        String str_state = "TODO";
        if(note.getState() == State.DONE)
            str_state = "DONE";
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_DATE, date_now.toString());
        values.put(TodoContract.TodoEntry.COLUMN_STATE, str_state);
        values.put(TodoContract.TodoEntry.COLUMN_CONTENT, note.getContent());
        values.put(TodoContract.TodoEntry.COLUMN_LEVEL, String.valueOf(note.getLevel()));

        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] selectionArgs = {String.valueOf(note.id)};

        db.update(TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
    }
}
