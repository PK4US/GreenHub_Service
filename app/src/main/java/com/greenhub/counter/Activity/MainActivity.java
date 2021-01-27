package com.greenhub.counter.Activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.greenhub.counter.R;
import com.greenhub.counter.login_screen.LoginScreen;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private String typeOfMeter;
    final String SECOND_SETTING_ACTIVITY = "SECOND_SETTING_ACTIVITY";

    private LoginScreen loginScreen = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Resources res = getApplication().getResources();
        final DisplayMetrics displayMetrics = res.getDisplayMetrics();
        final Configuration conf = res.getConfiguration();
        conf.setLocale(new Locale(Locale.UK.getDisplayLanguage()));
        res.updateConfiguration(conf, displayMetrics);

        final ImageButton iv_electricity_meter =  findViewById(R.id.iv_electricity_meter);
        final ImageButton iv_cold_water_meter =  findViewById(R.id.iv_cold_water_meter);
        final ImageButton iv_heating_meter =  findViewById(R.id.iv_heating_meter);
        final ImageButton iv_hot_water_meter =  findViewById(R.id.iv_hot_water_meter);


        iv_electricity_meter.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("IMAGE1", "motion event: " + event.toString());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        iv_electricity_meter.setImageResource(R.drawable.electricity_meter);
                        iv_electricity_meter.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    case MotionEvent.ACTION_UP: {
                        typeOfMeter = "Счетчик электроэнергии";
                        System.out.println(typeOfMeter);
                        iv_electricity_meter.setImageResource(R.drawable.green_electricity_meter);
                        iv_cold_water_meter.setImageResource(R.drawable.cold_water_meter);
                        iv_heating_meter.setImageResource(R.drawable.heating_meter);
                        iv_hot_water_meter.setImageResource(R.drawable.hot_water_meter);
                    }
                }
                return false;
            }
        });

        iv_cold_water_meter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("IMAGE2", "motion event: " + event.toString());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        iv_cold_water_meter.setImageResource(R.drawable.cold_water_meter);
                        iv_cold_water_meter.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    case MotionEvent.ACTION_UP: {
                        typeOfMeter = "Счетчик холодной воды";
                        System.out.println(typeOfMeter);
                        iv_cold_water_meter.setImageResource(R.drawable.green_cold_water_meter);
                        iv_electricity_meter.setImageResource(R.drawable.electricity_meter);
                        iv_heating_meter.setImageResource(R.drawable.heating_meter);
                        iv_hot_water_meter.setImageResource(R.drawable.hot_water_meter);
                    }
                }
                return false;
            }
        });

        iv_heating_meter.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("IMAGE3", "motion event: " + event.toString());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        iv_heating_meter.setImageResource(R.drawable.heating_meter);
                        iv_heating_meter.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    case MotionEvent.ACTION_UP: {
                        typeOfMeter = "Счетчик отопления";
                        System.out.println(typeOfMeter);
                        iv_heating_meter.setImageResource(R.drawable.green_heating_meter);
                        iv_cold_water_meter.setImageResource(R.drawable.cold_water_meter);
                        iv_electricity_meter.setImageResource(R.drawable.electricity_meter);
                        iv_hot_water_meter.setImageResource(R.drawable.hot_water_meter);
                    }
                }
                return false;
            }
        });

        iv_hot_water_meter.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("IMAGE4", "motion event: " + event.toString());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        iv_hot_water_meter.setImageResource(R.drawable.hot_water_meter);
                        iv_hot_water_meter.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }
                    case MotionEvent.ACTION_UP: {
                        typeOfMeter = "Счетчик горячей воды";
                        System.out.println(typeOfMeter);
                        iv_hot_water_meter.setImageResource(R.drawable.green_hot_water_meter);
                        iv_heating_meter.setImageResource(R.drawable.heating_meter);
                        iv_cold_water_meter.setImageResource(R.drawable.cold_water_meter);
                        iv_electricity_meter.setImageResource(R.drawable.electricity_meter);
                    }
                }
                return false;
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onClickStepTwo(View view) {
        if (typeOfMeter!=null){
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

            Intent intent = new Intent(SECOND_SETTING_ACTIVITY);
            intent.putExtra("type_Of_Meter",typeOfMeter);
            startActivity(intent);
        }else Toast.makeText(this,"Вібиріть тип лічильника",Toast.LENGTH_SHORT).show();
    }
}