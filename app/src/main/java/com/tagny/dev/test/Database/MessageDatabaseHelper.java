package com.tagny.dev.test.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by tagny on 12/06/2017.
 */

public class MessageDatabaseHelper extends SQLiteOpenHelper {

        public static final String TABLE_MESSAGE = "messages";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_MESSAGE = "textmessage";
        public static final String COLUMN_IMAGE_PATH = "imagepath";
        public static final String COLUMN_DATE = "datemessage";
        public static final String COLUMN_SENDER = "sender";

        private static final String DATABASE_NAME = "messages.db";
        private static final int DATABASE_VERSION = 2;

        // Database creation sql statement
        private static final String DATABASE_CREATE = "create table "
                + TABLE_MESSAGE
                + "( " + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_MESSAGE + " text, "
                + COLUMN_IMAGE_PATH + " text, "
                + COLUMN_DATE + " integer not null, "
                + COLUMN_SENDER + " integer default 0"
                + ");";

        public MessageDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
            onCreate(db);
        }

}
