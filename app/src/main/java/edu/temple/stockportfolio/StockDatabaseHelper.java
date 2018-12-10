package edu.temple.stockportfolio;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StockDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "stocks";
    private static final int DB_VERSION = 1;


    public StockDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE STOCK (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                " COMPANY_NAME TEXT, " +
                " TICKER_NAME TEXT," +
                " OPENING_PRICE REAL," +
                " CURRENT_PRICE REAL," +
                " CHART_URL TEXT);");

//        test content
//        ContentValues stockPair = new ContentValues();
//        stockPair.put("COMPANY_NAME", "Google");
//        stockPair.put("TICKER_NAME", "GOOG");
//        stockPair.put("OPENING_PRICE", 50.00);
//        stockPair.put("OPENING_PRICE", 67.50);
//        stockPair.put("CHART_URL", "https://macc.io/lab/cis3515/?symbol=goog");
//        sqLiteDatabase.insert("STOCK", null, stockPair);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
