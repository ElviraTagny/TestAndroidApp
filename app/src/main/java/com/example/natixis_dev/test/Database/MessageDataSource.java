package com.example.natixis_dev.test.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by natixis-dev on 12/06/2017.
 */

public class MessageDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MessageDatabaseHelper dbHelper;
    private String[] allColumns = { MessageDatabaseHelper.COLUMN_ID,
            MessageDatabaseHelper.COLUMN_MESSAGE, MessageDatabaseHelper.COLUMN_DATE, MessageDatabaseHelper.COLUMN_SENDER };

    public MessageDataSource(Context context) {
        dbHelper = new MessageDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Message createMessage(Message message) {
        ContentValues values = new ContentValues();
        values.put(MessageDatabaseHelper.COLUMN_MESSAGE, message.getTextMessage());
        values.put(MessageDatabaseHelper.COLUMN_DATE, message.getDateMessage());
        values.put(MessageDatabaseHelper.COLUMN_SENDER, message.isSender() ? 1: 0);
        long insertId = database.insert(MessageDatabaseHelper.TABLE_MESSAGE, null,
                values);
        Cursor cursor = database.query(MessageDatabaseHelper.TABLE_MESSAGE,
                allColumns, MessageDatabaseHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Message newMessage = cursorToMessage(cursor);
        cursor.close();
        return newMessage;
    }

    public void deleteMessage(Message message) {
        long id = message.getId();
        System.out.println("Message deleted with id: " + id);
        database.delete(MessageDatabaseHelper.TABLE_MESSAGE, MessageDatabaseHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<Message>();

        Cursor cursor = database.query(MessageDatabaseHelper.TABLE_MESSAGE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message message = cursorToMessage(cursor);
            messages.add(message);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return messages;
    }

    public void deleteAllMessages(){
        List<Message> messages = getAllMessages();
        for(Message msg: messages){
            deleteMessage(msg);
        }
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(0));
        message.setTextMessage(cursor.getString(1));
        message.setDateMessage(cursor.getLong(2));
        message.setSender(cursor.getInt(3) == 0 ? false : true);
        return message;
    }
}
