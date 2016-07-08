/*
 * Copyright 2016 Rohit Sharma (skyrohithigh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.stockhistory.HistoricData;
import com.sam_chordas.android.stockhawk.service.stockhistory.StockMeta;
import com.sam_chordas.android.stockhawk.service.stockhistory.StockSymbol;

import java.util.ArrayList;

public class StockDetails extends AppCompatActivity implements HistoricData.HistoricalDataCallback{

    private static final String TAG = StockDetails.class.getSimpleName();

    HistoricData historicData;
    ArrayList<StockSymbol> stockSymbols;

    LineChart lineChart;
    LinearLayout linearLayout;

    TextView stockName,stockSymbol,firstTrade,lastTrade,currency,closePrice,exchangeName;

    String symbol = "";
    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_details);

        Log.v(TAG,"onCreate");

        //Binding views
        lineChart = (LineChart) findViewById(R.id.lineChart);
        lineChart.setNoDataText("Loading Stock Data...");
        linearLayout = (LinearLayout) findViewById(R.id.ll_stock_details);

        stockName = (TextView) findViewById(R.id.tvStockName);
        stockSymbol = (TextView) findViewById(R.id.tvStockSymbol);
        firstTrade = (TextView) findViewById(R.id.tvfirstTrade);
        lastTrade = (TextView) findViewById(R.id.tvlastTrade);
        currency = (TextView) findViewById(R.id.tvCurrency);
//        closePrice = (TextView) findViewById(R.id.tvClosePrice);
        exchangeName = (TextView) findViewById(R.id.tvExchangeName);

        symbol = getIntent().getStringExtra("symbol_name");

        stockSymbol.setText(symbol);


        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        actionBar.setTitle(symbol+" Details");

        if(Utils.isNetworkAvailable(this)) {
            historicData = new HistoricData(this, this);
            historicData.getHistoricData(symbol);
        }else{
            historicData.setHistoricalDataStatus(HistoricData.STATUS_ERROR_NO_NETWORK);
            onFailure();
        }

    }

    @Override
    public void onSuccess(StockMeta stockMeta) {
        Log.v(TAG,"onSuccess");
        this.stockSymbols = stockMeta.stockSymbols;

        stockName.setText(stockMeta.companyName);
        firstTrade.setText(Utils.convertDate(stockMeta.firstTrade));
        lastTrade.setText(Utils.convertDate(stockMeta.lastTrade));
        currency.setText(stockMeta.currency);
//        closePrice.setText(""+stockMeta.previousClosePrice);
        exchangeName.setText(stockMeta.exchangeName);

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> xvalues = new ArrayList<>();

        for (int i = 0; i < this.stockSymbols.size(); i++) {

            StockSymbol stockSymbol = this.stockSymbols.get(i);
            double yValue = stockSymbol.close;

            xvalues.add(Utils.convertDate(stockSymbol.date));
            entries.add(new Entry((float) yValue, i));
        }


        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setLabelsToSkip(4);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        YAxis left = lineChart.getAxisLeft();
        left.setEnabled(true);
        left.setLabelCount(5, true);

        lineChart.getAxisRight().setEnabled(false);

        lineChart.getLegend().setTextSize(12f);

        LineDataSet dataSet = new LineDataSet(entries, symbol);
        LineData lineData = new LineData(xvalues, dataSet);

        lineData.setDrawValues(false);
        dataSet.setDrawCircles(false);

//        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.setDescription("Last 12 months stock comparision");
        lineChart.setData(lineData);
        lineChart.animateX(3000);
    }

    @Override
    public void onFailure() {
        Log.v(TAG,"onFailure");
        String errorMessage = "";

        @HistoricData.HistoricalDataStatuses
        int status = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(HistoricData.HISTORICAL_DATA_STATUS, -1);

        switch (status) {
            case HistoricData.STATUS_ERROR_JSON:
                errorMessage += getString(R.string.data_error_json);
                break;
            case HistoricData.STATUS_ERROR_NO_NETWORK:
                errorMessage += getString(R.string.data_no_internet);
                break;
            case HistoricData.STATUS_ERROR_PARSE:
                errorMessage += getString(R.string.data_error_parse);
                break;
            case HistoricData.STATUS_ERROR_UNKNOWN:
                errorMessage += getString(R.string.data_unknown_error);
                break;
            case HistoricData.STATUS_ERROR_SERVER:
                errorMessage += getString(R.string.data_server_down);
                break;
            case HistoricData.STATUS_OK:
                errorMessage += getString(R.string.data_no_error);
                break;
            default:
                break;
        }

        lineChart.setNoDataText(errorMessage);

        final Snackbar snackbar = Snackbar
                .make(linearLayout, getString(R.string.no_data_show) + errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        historicData.getHistoricData(symbol);
                    }
                })
                .setActionTextColor(Color.GREEN);

        View subview = snackbar.getView();
        TextView tv = (TextView) subview.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.RED);
        snackbar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
