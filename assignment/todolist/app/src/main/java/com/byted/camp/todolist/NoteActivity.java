package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.util.Date;

public class NoteActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private RadioButton radioBtn_high;
    private RadioButton radioBtn_normal;
    private RadioButton radioBtn_low;
    private EditText editText;
    private Button addBtn;
    private TodoDbHelper dbHelper;

    private int level_temp = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        addBtn = findViewById(R.id.btn_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });

        radioGroup = findViewById(R.id.radio_group);
        radioBtn_high = findViewById(R.id.radio_high);
        radioBtn_normal = findViewById(R.id.radio_normal);
        radioBtn_low = findViewById(R.id.radio_low);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == radioBtn_high.getId())
                {
                    level_temp = 3;
                }
                else if(checkedId == radioBtn_normal.getId())
                {
                    level_temp = 2;
                }
                else
                {
                    level_temp = 1;
                }

            }
        });

        dbHelper = new TodoDbHelper(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功
        Date date_now = new Date(System.currentTimeMillis());//获取系统当前时间

        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_DATE, date_now.toString());
        values.put(TodoContract.TodoEntry.COLUMN_STATE, String.valueOf(State.TODO));
        values.put(TodoContract.TodoEntry.COLUMN_CONTENT, content);
        values.put(TodoContract.TodoEntry.COLUMN_LEVEL, String.valueOf(level_temp));

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long newRowId = db.insert(TodoContract.TodoEntry.TABLE_NAME, null, values);
        if(newRowId > 0)
            return true;
        return false;
    }
}
