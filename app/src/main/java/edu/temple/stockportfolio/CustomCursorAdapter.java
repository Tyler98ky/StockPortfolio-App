package edu.temple.stockportfolio;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CustomCursorAdapter extends SimpleCursorAdapter {


    public CustomCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.fragment_stock, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView itemNumber = view.findViewById(R.id.item_number);
        TextView content = view.findViewById(R.id.content);

        itemNumber.setText(cursor.getString(0));
        content.setText(String.format("%s (%s)",cursor.getString(1), cursor.getString(4)));
        double currentPrice = cursor.getDouble(2);
        double openingPrice = cursor.getDouble(3);

        if (currentPrice > openingPrice) {
            view.setBackgroundColor(Color.GREEN);
        } else {
            view.setBackgroundColor(Color.RED);
        }
    }
}
