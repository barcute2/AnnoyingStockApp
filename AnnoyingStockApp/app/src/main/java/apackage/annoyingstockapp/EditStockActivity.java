package apackage.annoyingstockapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.TreeMap;

public class EditStockActivity extends AppCompatActivity {

    private TreeMap<String, Stock> stocks;
    private Stock currentStock;
    private EditText stockHighValue, stockLowValue;
    private TextView stockName, stockCurrentValue;
    private NumberFormat nf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        nf = new DecimalFormat();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        nf.setGroupingUsed(false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stock);
        initializeMap();
        initializeStock();
        populateActivity();
        handleSaveButton();
        handleDeleteButton();
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

    private void initializeStock(){
        Intent intent = getIntent();
        String stockName = intent.getStringExtra(MainActivity.STOCK);
        currentStock = stocks.get(stockName);
    }

    private void populateActivity(){
        stockName = (TextView) findViewById(R.id.stock_name);
        stockName.setText(currentStock.getStockName());
        stockCurrentValue = (TextView) findViewById(R.id.stock_current_value);
        stockCurrentValue.setText(String.valueOf(nf.format(currentStock.getActualValue())));
        stockHighValue = (EditText) findViewById(R.id.stock_high_value);
        stockHighValue.setText(String.valueOf(nf.format(currentStock.getHighValue())));
        stockLowValue = (EditText) findViewById(R.id.stock_low_value);
        stockLowValue.setText(String.valueOf(nf.format(currentStock.getLowValue())));
    }

    private void handleSaveButton(){
        Button save = (Button) findViewById(R.id.save_stock);
        save.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                try {
                    currentStock.setHighValue(Float.parseFloat(stockHighValue.getText().toString()));
                    currentStock.setLowValue(Float.parseFloat(stockLowValue.getText().toString()));
                    stocks.put(currentStock.getStockName(), currentStock);
                    Intent backToMainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    storeMap();
                    backToMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(backToMainIntent);
                }catch(Exception e){}
            }
        });
    }

    private void handleDeleteButton(){
        Button delete = (Button) findViewById(R.id.delete_stock);
        delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                stocks.remove(stockName.getText().toString());
                Intent backToMainIntent = new Intent(getApplicationContext(), MainActivity.class);
                backToMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backToMainIntent);
                storeMap();
            }
        });
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
