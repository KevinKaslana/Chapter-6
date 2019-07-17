package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.debug.DebugActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    TodoDbHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        mHelper.close();
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
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
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
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        mHelper = new TodoDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        List<Note> result = new LinkedList<>();
        Cursor cursor = null;
        try{
            cursor = db.query(TodoContract.Entry.TABLE_NAME,
                    new String[]{TodoContract.Entry.COLUMN_STATE,TodoContract.Entry.COLUMN_TIME,TodoContract.Entry._ID,TodoContract.Entry.COLUMN_TEXT},
                    null,null,null,null,
                    TodoContract.Entry.COLUMN_TIME + " DESC");
            while (cursor.moveToNext()) {
                String content = cursor.getString(cursor.getColumnIndex(TodoContract.Entry.COLUMN_TEXT));
                long time = cursor.getLong(cursor.getColumnIndex(TodoContract.Entry.COLUMN_TIME));
                int state = cursor.getInt(cursor.getColumnIndex(TodoContract.Entry.COLUMN_STATE));
                long _id = cursor.getLong(cursor.getColumnIndex(TodoContract.Entry._ID));
                Note note = new Note(_id);
                note.setContent(content);
                note.setDate(new Date(time));
                note.setState(State.from(state));
                result.add(note);
            }
        }finally {
            if (cursor == null){
                cursor.close();
            }
        }
        return result;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        mHelper = new TodoDbHelper(this);
        SQLiteDatabase db =  mHelper.getReadableDatabase();
        String selection = TodoContract.Entry.COLUMN_TEXT + " LIKE ?";
        String[] selectionArgs = {note.getContent()};
        int row = db.delete(TodoContract.Entry.TABLE_NAME, selection, selectionArgs);
        if (row > 0) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private void updateNode(Note note) {
        // TODO 更新数据
        mHelper = new TodoDbHelper(this);
        SQLiteDatabase db = mHelper.getReadableDatabase();
        int state = note.getState().intValue;
        ContentValues values = new ContentValues();
        values.put(TodoContract.Entry.COLUMN_STATE,state);
        String selection = TodoContract.Entry.COLUMN_TEXT + " LIKE ?";
        String[] selectionArgs = {note.getContent()};
        int row = db.update(TodoContract.Entry.TABLE_NAME,values,selection,selectionArgs);
        if (row > 0) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

}
