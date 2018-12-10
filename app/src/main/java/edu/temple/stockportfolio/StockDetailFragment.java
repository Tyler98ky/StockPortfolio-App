package edu.temple.stockportfolio;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 */
public class StockDetailFragment extends Fragment {
    private Stock mStock;
    private TextView mCompanyNameTV;
    private TextView mCurrentPriceTV;
    private TextView mOpeningPriceTV;
    private WebView mWebView;


    public StockDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_stock_detail, container, false);
        Bundle arguments = getArguments();
        mStock = new Stock(getContext(), arguments.getInt(StockUtils.STOCK_ID_EXTRA));

        mCompanyNameTV = view.findViewById(R.id.companyNameTV);
        mCompanyNameTV.setText(mStock.getCompanyName());

        mCurrentPriceTV = view.findViewById(R.id.currentPriceTV);
        mCurrentPriceTV.setText(toDollarsFormat(mStock.getCurrentPrice()));

        mOpeningPriceTV = view.findViewById(R.id.openingPriceTV);
        mOpeningPriceTV.setText(toDollarsFormat(mStock.getOpeningPrice()));

        mWebView = view.findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(String.format("https://macc.io/lab/cis3515/?symbol=%s", mStock.getStockID()));


        return view;
    }


    private String toDollarsFormat(int amount) {
        return String.format("$%.2f", amount);
    }

    private String toDollarsFormat(double amount) {
        return String.format("$%.2f", amount);
    }
}
