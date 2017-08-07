package apackage.annoyingstockapp;

import java.io.Serializable;

/**
 * Created by zeno on 9/21/16.
 */
public class Stock implements Serializable{
    private float actualValue;
    private float highValue;
    private float lowValue;
    private String stockName;
    private int color;

    public Stock(){
        actualValue = 0;
        highValue = 0;
        lowValue = 0;
        color = android.R.drawable.btn_default;
    }

    public float getActualValue(){
        return actualValue;
    }
    public float getHighValue(){
        return highValue;
    }
    public float getLowValue(){
        return lowValue;
    }
    public String getStockName(){
        return stockName;
    }
    public int getColor() { return color;  }

    public void setActualValue(float a){
        if(a > 0) {
            actualValue = a;
        }
    }
    public void setHighValue(float h){
        if(h > 0){
            highValue = h;
        }
    }
    public void setLowValue(float l){
        if(l > 0){
            lowValue = l;
        }
    }
    public void setStockName(String n){
        if(n != null){
            stockName = n;
        }
    }
    public void setColor(int color) {
        this.color = color;
    }
}
