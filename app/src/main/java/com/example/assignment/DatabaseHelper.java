package com.example.assignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "bills.db";   // the file name of the database
    private static final int DB_VERSION = 1;
    private static final String TABLE = "bills";        // the table name

    // Constructor: tells Android the database name and version
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Runs once, the first time the database is created: builds the table
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "month INTEGER, "
                + "units INTEGER, "
                + "rebate REAL, "
                + "total REAL, "
                + "final_cost REAL)");
    }

    // Runs when DB_VERSION goes up: here we simply delete the table and rebuild it
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    // Packs a BillRecord into the format the database understands
    private ContentValues toValues(BillRecord bill) {
        ContentValues values = new ContentValues();
        values.put("month", bill.month);
        values.put("units", bill.units);
        values.put("rebate", bill.rebate);
        values.put("total", bill.total);
        values.put("final_cost", bill.finalCost);
        return values;
    }

    // Adds a new bill as a new row
    public void insertBill(BillRecord bill) {
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE, null, toValues(bill));
    }

    // Changes an existing row. The row is found using the bill's id
    public void updateBill(BillRecord bill) {
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE, toValues(bill), "id = ?", new String[]{String.valueOf(bill.id)});
    }

    // Looks for a bill saved for this month. Returns its id, or -1 if none exists
    public int findIdByMonth(int month) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, new String[]{"id"},
                "month = ?", new String[]{String.valueOf(month)},
                null, null, null);

        int foundId = -1;
        if (cursor.moveToFirst()) {          // moveToFirst is true if there is a row
            foundId = cursor.getInt(0);
        }
        cursor.close();
        return foundId;
    }

    // Reads every saved bill, ordered January to December
    public List<BillRecord> getAllBills() {
        List<BillRecord> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, null, null, null, null, "month ASC");

        while (cursor.moveToNext()) {        // go through the rows one by one
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
            int units = cursor.getInt(cursor.getColumnIndexOrThrow("units"));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow("rebate"));
            double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("final_cost"));

            list.add(new BillRecord(id, month, units, rebate, total, finalCost));
        }
        cursor.close();
        return list;
    }
    // Finds one bill by its id. Returns null if it doesn't exist
    public BillRecord getBill(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);

        BillRecord bill = null;   // stays null if nothing is found
        if (cursor.moveToFirst()) {
            int month = cursor.getInt(cursor.getColumnIndexOrThrow("month"));
            int units = cursor.getInt(cursor.getColumnIndexOrThrow("units"));
            double rebate = cursor.getDouble(cursor.getColumnIndexOrThrow("rebate"));
            double total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"));
            double finalCost = cursor.getDouble(cursor.getColumnIndexOrThrow("final_cost"));
            bill = new BillRecord(id, month, units, rebate, total, finalCost);
        }
        cursor.close();
        return bill;
    }

    // Removes the row with this id from the table
    public void deleteBill(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, "id = ?", new String[]{String.valueOf(id)});
    }
}