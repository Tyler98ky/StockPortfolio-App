package edu.temple.stockportfolio;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class StockListFragment extends Fragment {
    private SQLiteDatabase mDB;
    private Cursor mCursor;
    private ListView mListView;
    private FileObserver mFileObserver;
    private TextView mNoStocksMessage;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StockListFragment() {
    }

    // TODO: Customize parameter initialization
    public static StockListFragment newInstance(int columnCount) {
        StockListFragment fragment = new StockListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SQLiteOpenHelper stockDBHelper = new StockDatabaseHelper(getContext(), "STOCKDB", null, 1);
        mDB = stockDBHelper.getWritableDatabase();
        mCursor = mDB.query("STOCK", new String[] {"_id", "COMPANY_NAME", "CURRENT_PRICE", "OPENING_PRICE", "TICKER_NAME"},
                null,
                null,
                null,
                null,
                null);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stock_list, container, false);

        mNoStocksMessage = view.findViewById(R.id.textView2);

        if (mCursor.getCount() == 0) {
            mNoStocksMessage.setVisibility(View.VISIBLE);
        } else {
            mNoStocksMessage.setVisibility(View.INVISIBLE);
        }

        // Set the adapter
        Context context = view.getContext();
        mListView = (ListView) view.findViewById(R.id.list);
        CursorAdapter listAdapter = new CustomCursorAdapter(
                context,
                R.layout.fragment_stock,
                mCursor,
                new String[]{"_id", "COMPANY_NAME", "CURRENT_PRICE", "OPENING_PRICE", "TICKER_NAME"},
                new int[] {R.id.item_number, R.id.content},
                0);
        mListView.setAdapter(listAdapter);
        mListView.setOnItemClickListener((ListView.OnItemClickListener) getActivity());

        return view;
    }

    public void updateDataSet() {
        SQLiteOpenHelper stockDBHelper = new StockDatabaseHelper(getContext(), "STOCKDB", null, 1);
        mDB = stockDBHelper.getReadableDatabase();
        mCursor = mDB.query("STOCK", new String[] {"_id", "COMPANY_NAME", "CURRENT_PRICE", "OPENING_PRICE", "TICKER_NAME"},
                null,
                null,
                null,
                null,
                null);

        ListView lv = getView().findViewById(R.id.list);
        CursorAdapter adapter = (CursorAdapter) lv.getAdapter();

        try {
            adapter.changeCursor(mCursor);
        } catch (SQLiteException e) {
            Toast.makeText(getContext(), R.string.database_update_error, Toast.LENGTH_SHORT).show();
        }

        if (mCursor.getCount() == 0) {
            mNoStocksMessage.setVisibility(View.VISIBLE);
        } else {
            mNoStocksMessage.setVisibility(View.INVISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

}
