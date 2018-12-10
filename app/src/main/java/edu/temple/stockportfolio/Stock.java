package edu.temple.stockportfolio;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;



public class Stock {
    private String mCompanyName;
    private String mTickerName;
    private double mCurrentPrice;
    private double mOpeningPrice;
    private int mStockID;  // this correlates to its ID in the database
    private String mChartURL = "https://macc.io/lab/cis3515/?symbol=%s";
    private String mStockInfoURL = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=%s";
    private Cursor mCursor;
    private StockDatabaseHelper mDBHelper;
    private SQLiteDatabase mDB;

    public Stock (Context context, int stockID) {
        mStockID = stockID;
        mDBHelper = new StockDatabaseHelper(context, null, null, 1);
        mDB = mDBHelper.getReadableDatabase();
        mCursor = mDB.query("STOCK",
                new String[]{"COMPANY_NAME", "CURRENT_PRICE", "OPENING_PRICE"},
                "_id = ?",
                new String[] {Integer.toString(stockID)},
                null,
                null,
                null);
        if (mCursor.moveToNext()) {
            mCompanyName = mCursor.getString(0);
            mCurrentPrice = mCursor.getDouble(1);
            mOpeningPrice = mCursor.getDouble(2);
        }
    }

    public Stock (final Context context, String tickerName) {
        mTickerName = tickerName;
        mChartURL = String.format(mChartURL, mTickerName);
        mStockInfoURL = String.format(mStockInfoURL, mTickerName);
        volleyJsonRequest(context);
//        JSONObject response = volleyJsonRequest(context);
//        parseStockInfo(response);
    }

    private void parseStockInfo(JSONObject jsonFile) {
        if (jsonFile != null) {
            try {
                mCompanyName = jsonFile.getJSONObject("Name").toString();
                mCurrentPrice = Double.parseDouble(jsonFile.getJSONObject("LastPrice").toString());
                mOpeningPrice = Double.parseDouble(jsonFile.getJSONObject("Open").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void volleyJsonRequest(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        JSONObject jsonResponse = null;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, mStockInfoURL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        parseStockInfo(response);
                        Log.e("IT WORKDED", response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR", "VOLLEY TIMED OUT");
                    }
                });
        queue.add(jsonObjectRequest);
    }

    public Stock (String companyName, String tickerName, double currentPrice, double openingPrice, int stockID) {
        mCompanyName = companyName;
        mTickerName = tickerName;
        mChartURL = String.format(mChartURL, mTickerName);
        mCurrentPrice = currentPrice;
        mOpeningPrice = openingPrice;
        mStockID = stockID;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public String getTickerName() {
        return mTickerName;
    }

    public double getCurrentPrice() {
        return mCurrentPrice;
    }

    public double getOpeningPrice() {
        return mOpeningPrice;
    }

    public int getStockID() {
        return mStockID;
    }

    public boolean isGreen() {
        return mCurrentPrice >= mOpeningPrice;
    }
}
