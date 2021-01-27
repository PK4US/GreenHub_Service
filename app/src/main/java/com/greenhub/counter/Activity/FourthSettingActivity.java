package com.greenhub.counter.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.greenhub.counter.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FourthSettingActivity extends AppCompatActivity {

    final String FIRST_SETTING_ACTIVITY = "FIRST_SETTING_ACTIVITY";
    private static final String TAG = "Response";

    AlertDialog.Builder dialogBuilder;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fourth_setting);
    }

    public void onClickAddMeter(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        try {
            postRequest();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showAlertDialog(R.layout.dialog);
    }

    private void showAlertDialog(int layout){
        dialogBuilder = new AlertDialog.Builder(FourthSettingActivity.this);
        View layoutView = getLayoutInflater().inflate(layout, null);
        dialogBuilder.setView(layoutView);
        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                    Intent intent = new Intent(FIRST_SETTING_ACTIVITY);
                    startActivity(intent);
                    finish();
                }
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    public void postRequest() throws IOException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "http://34.252.42.20:1029/addCounter";

        OkHttpClient client = new OkHttpClient();

        JSONObject postdata = new JSONObject();
        try {
            Bundle args = getIntent().getExtras();
            String typeOfMeter4 = args.getString("code_meter3");
            String  id_meter4 = args.getString("code_meter3");
            String id_label4 = args.getString("code_label3");
            String indication_meter4 = args.getString("indication_meter3");

            postdata.put("lc_id", "1");
            postdata.put("flat_no", "1");
            postdata.put("count_id", id_meter4);
            postdata.put("count_type", "1");
            postdata.put("pad_id", id_label4);
            postdata.put("pad_type", "1");
            postdata.put("readings", indication_meter4);

        } catch(JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("failure Response", mMessage);
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String mMessage = response.body().string();
                Log.e(TAG, mMessage);
            }
        });
    }
}