package com.greenhub.counter.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.greenhub.counter.R;


public class ThirdSettingActivity extends AppCompatActivity {

    final String FOURTH_SETTING_ACTIVITY = "FOURTH_SETTING_ACTIVITY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third_setting);

        Bundle args = getIntent().getExtras();

        if (args != null) {
            EditText et_id_label = findViewById(R.id.et_id_label);
            EditText et_id_meter = findViewById(R.id.et_id_meter);
            EditText et_type_meter = findViewById(R.id.et_type_meter);

            et_id_label.setText(args.getString("code_label2"));
            et_id_meter.setText(args.getString("code_meter2"));
            et_type_meter.setText(args.getString("type_Of_Meter2"));

        }else System.out.println("args = null");
    }

    public void onClickStepFourth(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        EditText et_indication_meter = findViewById(R.id.et_indication_meter);
        String indication_meter3 = et_indication_meter.getText().toString();

        if (et_indication_meter.getText().length() == 9){
            Intent intent = new Intent(FOURTH_SETTING_ACTIVITY);

            Bundle args = getIntent().getExtras();
            String id_meter3 = args.getString("code_meter2");
            String id_label3 = args.getString("code_label2");
            String typeOfMeter3 = args.getString("type_Of_Meter2");

            intent.putExtra("code_meter3", id_meter3);
            intent.putExtra("code_label3", id_label3);
            intent.putExtra("type_Of_Meter3", typeOfMeter3);
            intent.putExtra("indication_meter3",indication_meter3);

            System.out.println(id_label3);
            System.out.println(id_meter3);
            System.out.println(indication_meter3);
            System.out.println(typeOfMeter3);

            startActivity(intent);
        } else if (et_indication_meter.getText().length() <9) {
            Toast.makeText(this,"Данные счетчика слишком короткие",Toast.LENGTH_SHORT).show();
        } else if (et_indication_meter.getText().length() >9){
            Toast.makeText(this,"Данные счетчика слишком длинные",Toast.LENGTH_SHORT).show();
        }else Toast.makeText(this,"Введите данные в поле счетчика",Toast.LENGTH_SHORT).show();
    }
}