package edu.temple.stockportfolio;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class StockDetailActivity extends AppCompatActivity {
    private int mStockID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        mStockID = getIntent().getIntExtra(StockUtils.STOCK_ID_EXTRA, 0);

        StockDetailFragment detailFragment = new StockDetailFragment();
        Bundle fragArgs = new Bundle();
        fragArgs.putInt(StockUtils.STOCK_ID_EXTRA, mStockID);
        detailFragment.setArguments(fragArgs);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.stockDetailFrameContainer, detailFragment);
        ft.commit();

    }

}
