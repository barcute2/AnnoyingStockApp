package apackage.annoyingstockapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.Exchanger;

public class NewStockActivity extends AppCompatActivity {

    private TreeMap<String, Stock> stocks;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_stock);

        Button saveStock = (Button) findViewById(R.id.save_stock);
        saveStock.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                initializeMap();
                EditText stockName = (EditText) findViewById(R.id.stock_name);
                EditText stockHighValue = (EditText) findViewById(R.id.stock_high_value);
                EditText stockLowValue = (EditText) findViewById(R.id.stock_low_value);
                Stock newStock = new Stock();
                newStock.setStockName(stockName.getText().toString());
                try {
                    newStock.setHighValue(Float.parseFloat(stockHighValue.getText().toString()));
                    newStock.setLowValue(Float.parseFloat(stockLowValue.getText().toString()));
                    stocks.put(newStock.getStockName(), newStock);
                    storeMap();
                    Intent backToMainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    backToMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(backToMainIntent);
                }catch(Exception e){}
            }
        });

        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent backToMainIntent = new Intent(getApplicationContext(), MainActivity.class);
                backToMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backToMainIntent);
            }
        });
    }

    private void initializeMap(){
        try {
            FileInputStream fileInputStream = openFileInput("stockMap.map");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            stocks = (TreeMap<String, Stock>) objectInputStream.readObject();
            objectInputStream.close();
        }catch (Exception e){}
        if(stocks == null){
            stocks = new TreeMap<String, Stock>();
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
}
