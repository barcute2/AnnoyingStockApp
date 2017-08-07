/**
 * Created by zeno on 9/18/16.
 */
public class Stock {
    private float currentValue;
    private float lowValue;
    private float highValue;

    public float getCurrentValue(){
        return currentValue;
    }
    public float getLowValue(){
        return lowValue;
    }
    public float getHighValue(){
        return highValue;
    }

    public void setCurrentValue(float cValue){
        currentValue = cValue;
    }
    public void setLowValue(float lValue){
        lowValue = lValue;
    }
    public void setHighValue(float hValue){
        highValue = hValue;
    }
}
