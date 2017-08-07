package apackage.annoyingstockapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

public class SettingsActivity extends AppCompatActivity {
    static final String ENABLE_NOTIFICATIONS = "enableNotifications";
    static final String NOTIFICATION_LENGTH = "notificationLength";
    static final String UPDATE_FREQUENCY = "updateFrequency";
    private boolean enableNotifications;
    private int notificationLength, updateFrequency;
    private EditText  notificationLengthEditText, updateFrequencyEditText;
    private ToggleButton enableNotificationsButton;
    private Button saveButton, cancelButton;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        bundle = savedInstanceState;
        populateControls(savedInstanceState);
        handleSaveButton();
        handleCancelButton();
    }

    private void populateControls(Bundle savedInstanceState){
        notificationLengthEditText = (EditText) findViewById(R.id.notification_length);
        updateFrequencyEditText = (EditText) findViewById(R.id.update_frequency);
        enableNotificationsButton = (ToggleButton) findViewById(R.id.enable_notifications);
        if(savedInstanceState != null){
            notificationLength = savedInstanceState.getInt(NOTIFICATION_LENGTH);
            updateFrequency = savedInstanceState.getInt(UPDATE_FREQUENCY);
            enableNotifications = savedInstanceState.getBoolean(ENABLE_NOTIFICATIONS);
        }else{
            notificationLength = 500;
            updateFrequency = 15;
            enableNotifications = true;
        }
        notificationLengthEditText.setText(String.valueOf(notificationLength));
        updateFrequencyEditText.setText(String.valueOf(updateFrequency));
        enableNotificationsButton.setChecked(enableNotifications);
        enableNotificationsButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked){
                    enableNotifications = true;
                }else{
                    enableNotifications = false;
                }
            }
        });
    }

    private void handleSaveButton(){
        saveButton = (Button) findViewById(R.id.save_settings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    notificationLength = Integer.parseInt(notificationLengthEditText.getText().toString());
                    updateFrequency = Integer.parseInt(updateFrequencyEditText.getText().toString());
                    enableNotifications = enableNotificationsButton.isActivated();
                    bundle.putFloat(NOTIFICATION_LENGTH, notificationLength);
                    bundle.putFloat(UPDATE_FREQUENCY, updateFrequency);
                    bundle.putBoolean(ENABLE_NOTIFICATIONS, enableNotifications);
                    Intent backToMainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(backToMainIntent);
                }catch(Exception e){}
            }
        });
    }

    private void handleCancelButton(){
        cancelButton = (Button) findViewById(R.id.cancel_settings);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent backToMainIntent = new Intent(getApplicationContext(), MainActivity.class);
                backToMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backToMainIntent);
            }
        });
    }

    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putFloat(NOTIFICATION_LENGTH, notificationLength);
        savedInstanceState.putFloat(UPDATE_FREQUENCY, updateFrequency);
        savedInstanceState.putBoolean(ENABLE_NOTIFICATIONS, enableNotifications);
        super.onSaveInstanceState(savedInstanceState);
    }
}
