package com.byted.camp.todolist.db;

import android.provider.BaseColumns;

/**
 * Created on 2019/1/22.
 *
 * @author xuyingyi@bytedance.com (Yingyi Xu)
 */
public final class TodoContract {

    // TODO 定义表结构和 SQL 语句常量

    private TodoContract() {}

    public static class Entry implements BaseColumns {
        public static final String TABLE_NAME = "todo";
        public static final String COLUMN_TEXT = "content";
        public static final String COLUMN_STATE = "state";
        public static final String COLUMN_TIME = "time";
    }

    public static final String SQL_CREATE =
            "CREATE TABLE " + Entry.TABLE_NAME + "(" +
            Entry._ID + " INTEGER PRIMARY KEY," +
            Entry.COLUMN_TEXT + " TEXT," +
            Entry.COLUMN_TIME + " INTEGER," +
            Entry.COLUMN_STATE + " INTEGER)";

    public static final String SQL_DROP =
            "DROP TABLE IF EXISTS " + Entry.TABLE_NAME;

}
