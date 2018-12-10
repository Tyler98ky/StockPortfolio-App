package edu.temple.stockportfolio;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class MainActivity extends AppCompatActivity implements ListView.OnItemClickListener, MyCustomDialogFragment.OnDialogTextEntryListener{
    private String mStockName;
    private StockListFragment mStockListFragment;
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        startDatabaseUpdateLoop();

        FloatingActionButton fabAdd = findViewById(R.id.fab);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyCustomDialogFragment dialogFragment = new MyCustomDialogFragment();
                FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                dialogFragment.show(t, "dialog");
            }
        });

        FloatingActionButton fabClear = findViewById(R.id.floatingActionButton);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.clear_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getApplicationContext().deleteDatabase("stocks");
                        mStockListFragment.updateDataSet();
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        final AlertDialog dialog = builder.create();
        fabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        mStockListFragment = new StockListFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.stockListFrameContainer, mStockListFragment).commit();
    }

    private void startDatabaseUpdateLoop() {
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                new UpdateStockDatabase().execute();
                mHandler.postDelayed(this, 60000);  // update every minute
            }
        };
        mHandler.post(mRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogTextEntryReceived(String message) {
        mStockName = message;
        addStockToDB(message);
    }

    private void addStockToDB(String tickerName) {
        new FetchNewStock().execute(tickerName);
    }

    private String parseCompanyName(JSONObject jsonObject) {
        String name = null;
        try {
            name = jsonObject.get("Name").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return name;
    }

    private JSONObject volleyJsonRequest(String tickerName) {
        final String stockInfoURL = String.format("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=%s", tickerName);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        RequestFuture<JSONObject> future = RequestFuture.newFuture();

        JSONObject response = null;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, stockInfoURL,
                null, future, future);
        queue.add(jsonObjectRequest);

        try {
            response = future.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            Log.e("TAG", "The network volley connection timed out");
        }

        return response;
    }

    private double parseCurrentPrice(JSONObject jsonObject) {
        String price = null;
        try {
            price = jsonObject.get("LastPrice").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Double.parseDouble(price);
    }

    private double parseOpeningPrice(JSONObject jsonObject) {
        String price = null;
        try {
            price = jsonObject.get("Open").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Double.parseDouble(price);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        View stockDetailContainer = findViewById(R.id.stockDetailFrameContainer);
        if (stockDetailContainer == null) {
            Intent detailIntent = new Intent(this, StockDetailActivity.class);
            detailIntent.putExtra(StockUtils.STOCK_ID_EXTRA, i + 1); // plus one because SQL is 1-indexed
            startActivity(detailIntent);

        } else {
            //TODO replace the detail container with a StockDetailFragment
            StockDetailFragment stockDetailFragment = new StockDetailFragment();
            Bundle args = new Bundle();
            args.putInt(StockUtils.STOCK_ID_EXTRA, i + 1);
            stockDetailFragment.setArguments(args);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.stockDetailFrameContainer, stockDetailFragment).addToBackStack(null).commit();
        }
    }

    private class FetchNewStock extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            for (String tickerName : strings) {
                ContentValues valuePairs = new ContentValues();
                JSONObject response = volleyJsonRequest(tickerName);
                if (isValidResponse(response)) {
                    double currentPrice = parseCurrentPrice(response);
                    double openingPrice = parseOpeningPrice(response);
                    String companyName = parseCompanyName(response);
                    String chartURL = String.format("https://macc.io/lab/cis3515/?symbol=%s", tickerName);

                    valuePairs.put("COMPANY_NAME", companyName);
                    valuePairs.put("CURRENT_PRICE", currentPrice);
                    valuePairs.put("OPENING_PRICE", openingPrice);
                    valuePairs.put("CHART_URL", chartURL);
                    valuePairs.put("TICKER_NAME", tickerName);

                    StockDatabaseHelper sBHelper = new StockDatabaseHelper(getApplicationContext(), null, null, 1);
                    SQLiteDatabase db = sBHelper.getWritableDatabase();
                    db.insert("STOCK", null, valuePairs);
                    db.close();
                    sBHelper.close();
                } else {
                    Runnable errorToast = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.stock_not_found, Toast.LENGTH_SHORT).show();
                        }
                    };
                    runOnUiThread(errorToast);
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mStockListFragment.updateDataSet();
        }
    }

    private class UpdateStockDatabase extends AsyncTask<Void, Void, Boolean> {
        private Cursor cursor;
        private SQLiteDatabase db;
        private StockDatabaseHelper dbHelper;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("Database update", "DATABASE WAS UPDATED");
            dbHelper = new StockDatabaseHelper(getApplicationContext(), null, null, 1);
            db = dbHelper.getWritableDatabase();
            cursor = db.query("STOCK", new String[]{"TICKER_NAME"}, null, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            cursor.close();
            db.close();
            dbHelper.close();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ContentValues values = new ContentValues();
            while (cursor.moveToNext()) {
                String currentTicker = cursor.getString(0);  // this should be the ticker name
                JSONObject response = volleyJsonRequest(currentTicker);
                if (isValidResponse(response)) {
                    double newCurrentPrice = parseCurrentPrice(response);
                    double newOpeningPrice = parseOpeningPrice(response);

                    values.put("OPENING_PRICE", newOpeningPrice);
                    values.put("CURRENT_PRICE", newCurrentPrice);
                    db.update("STOCK", values, "TICKER_NAME = ?", new String[] {currentTicker});
                } else {
                    Runnable errorToast = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), R.string.stock_not_found, Toast.LENGTH_SHORT).show();
                        }
                    };
                    runOnUiThread(errorToast);
                }
            }
            return true;
        }
    }

    private boolean isValidResponse(JSONObject response) {
        return (response != null && response.has("Status"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }
}
