package apackage.annoyingstockapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.xmlpull.v1.XmlPullParser;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Object.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import yahoofinance.YahooFinance;

public class MainActivity extends AppCompatActivity {
    public final static String MAP = "com.example.annoyingstockapp.MAP";
    public final static String STOCK = "com.example.annoyingstockapp.STOCK";
    static final String ENABLE_NOTIFICATIONS = "enableNotifications";
    static final String NOTIFICATION_LENGTH = "notificationLength";
    static final String UPDATE_FREQUENCY = "updateFrequency";
    private boolean enableNotifications;
    private int notificationLength, updateFrequency;
    private static TreeMap<String, Stock> stocks;
    private static HashMap<String, Button> stocksButtons;
    private AtomicBoolean executing;
    private static Timer timer;
    private NumberFormat nf;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    //region OVERRIDEN
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        super.onCreate(savedInstanceState);
        notificationLength = 1000;
        updateFrequency = 15 * 60 * 1000;
        enableNotifications = true;
        try {
            FileInputStream fileInputStream = openFileInput("stockMap.map");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            stocks = (TreeMap<String, Stock>) objectInputStream.readObject();
            if(stocks == null){
                stocks = new TreeMap<String, Stock>();
            }
            objectInputStream.close();
        }catch (Exception e){
            stocks = new TreeMap<String, Stock>();
            e.printStackTrace();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
        setContentView(R.layout.activity_main);
        populateActivity();
        executing = new AtomicBoolean(false);
        startTimer(this.getApplicationContext());
    }
    @Override
    public void onStop() {
        super.onStop();
        //if(timer != null) {
            //timer.cancel();
            //timer.purge();
            //timer = null;
        //}
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void populateActivity() {
        listStocks();
        makeAddStockButton();
        makeSettingsButton();
    }
    //endregion

    private void listStocks() {
        stocksButtons = new HashMap();
        if((stocks == null) || stocks.isEmpty()) return;
        ViewGroup layout = (ViewGroup) findViewById(R.id.stock_names);
        Iterator it = stocks.entrySet().iterator();
        while (it.hasNext()) {
            TreeMap.Entry pair = (TreeMap.Entry) it.next();
            final Stock st = (Stock) pair.getValue();
            Button stock = new Button(this);
            stock.setText(st.getStockName() + ": $" + String.valueOf(nf.format(st.getActualValue())));
            stocksButtons.put(st.getStockName(), stock);
            stock.setOnClickListener(new View.OnClickListener(){
                public void onClick(View view){
                    Intent intent = new Intent(view.getContext(), EditStockActivity.class);
                    intent.putExtra(STOCK, st.getStockName());
                    startActivity(intent);
                }
            });
            layout.addView(stock);
        }
    }

    private synchronized void startTimer(final Context context){
        if(timer == null) {
            timer = new Timer();
        }else{
            timer.cancel();
            timer.purge();
            timer = new Timer();
        }
        TimerTask task = new TimerTask(){
            @Override
            public void run(){
                //final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                //final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                //final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                //if ((!wifi.isConnected() && !mobile.isConnected()) || (executing.get())) {
                //    return;
                //}
                executing.set(true);
                retrieveRealStockPrice();
            }
        };
        timer.schedule(task, 01, updateFrequency);
    }

    private void makeAddStockButton() {
        Button newStock = (Button) findViewById(R.id.add_stock);
        newStock.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(view.getContext(), NewStockActivity.class);
                intent.putExtra(MAP, stocks);
                startActivity(intent);
            }
        });
    }

    private void makeSettingsButton(){
        Button settings = (Button) findViewById(R.id.settings_button);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    protected synchronized void retrieveRealStockPrice(){
        try {
            new RetrieveStockPrice().execute(stocks);
        }catch(Exception e){
            e.printStackTrace();
            return;
        }
    }

    private void storeMap(){
        try{
            FileOutputStream fileOutputStream = openFileOutput("stockMap.map", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(stocks);
            objectOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }



    private class RetrieveStockPrice extends AsyncTask<TreeMap<String, Stock>, Void, TreeMap<String, Stock>> {

        protected TreeMap<String, Stock> doInBackground(TreeMap<String, Stock>... st){
            executing.set(true);
            Iterator it = stocks.entrySet().iterator();
            String[] symbols = new String[stocks.size()];
            int symbolCounter = 0;
            while (it.hasNext()){
                TreeMap.Entry pair = (TreeMap.Entry) it.next();
                Stock stock = (Stock) pair.getValue();
                symbols[symbolCounter] = stock.getStockName();
                symbolCounter++;
            }
            try {
                java.util.Map<String, yahoofinance.Stock> yahooStocks = YahooFinance.get(symbols);
                for(String stockName: symbols){
                    yahoofinance.Stock yahooStock = yahooStocks.get(stockName.toUpperCase());
                    if(yahooStock != null) {
                        updateStockValue(yahooStock.getQuote().getPrice().floatValue(), stocks.get(stockName));
                    }
                }
                storeMap();
            } catch (IOException ex) {}
            return st[0];
        }

        @Override
        protected void onPostExecute(TreeMap<String, Stock> st){
            boolean notify = false;
            //update the color of the stock buttons
            Iterator it = stocksButtons.entrySet().iterator();
            while(it.hasNext()){
                HashMap.Entry<String, Button> pair = (HashMap.Entry<String, Button>) it.next();
                Button bt = pair.getValue();
                Stock stock = stocks.get(pair.getKey());
                if(stock.getColor() != android.R.drawable.btn_default){
                    notify = true;
                    bt.setBackgroundColor(stock.getColor());
                }else{
                    bt.setBackgroundResource(android.R.drawable.btn_default);
                }
                bt.setText(stock.getStockName() + ": $" + String.valueOf(nf.format(stock.getActualValue())));
            }
            if(notify && enableNotifications){
                Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(notificationLength);
            }
            executing.set(false);
        }

        private boolean updateStockValue(Float lastTradePriceOnly, Stock stock){
            boolean outOfInterval = false;
            if(lastTradePriceOnly > 0.01){
                if(stock.getHighValue() < lastTradePriceOnly){
                    outOfInterval = true;
                    stock.setColor(Color.GREEN);
                }else if (stock.getLowValue() > lastTradePriceOnly){
                    outOfInterval = true;
                    stock.setColor(Color.RED);
                }else{
                    stock.setColor(android.R.drawable.btn_default);
                }
                stock.setActualValue(lastTradePriceOnly);
            }
            return outOfInterval;
        }
    }
}


