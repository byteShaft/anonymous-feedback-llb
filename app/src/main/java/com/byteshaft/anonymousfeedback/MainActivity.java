package com.byteshaft.anonymousfeedback;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.byteshaft.requests.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {

    private String android_id;
    private EditText etFeedback;
    private String feedbackString;
    private Button sendButton;
    private final int TIMES_LLB_CODE = 0;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        Log.wtf("ID ", android_id);
        etFeedback = findViewById(R.id.feedback_edit_text);
        sendButton = findViewById(R.id.button_send);
        sendButton.setOnClickListener(view -> {
            if (validate()) {
                sendFeedback(android_id, feedbackString, TIMES_LLB_CODE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        canProvideFeedback(android_id);
    }

    private boolean validate() {
        boolean valid = true;
        feedbackString = etFeedback.getText().toString();
        if (feedbackString.trim().isEmpty()) {
            etFeedback.setError("Required");
            valid = false;
        } else {
            etFeedback.setError(null);
        }
        return valid;
    }

    private void alertMessage(String message) {
        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setTitle("Message");
        alertDialogBuilder.setMessage(message)
                .setCancelable(false).setPositiveButton("OK",
                (dialog, id) -> {
                    dialog.dismiss();
                    finish();
                });
        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void canProvideFeedback(String deviceId) {
        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener((request1, readyState) -> {
            switch (readyState) {
                case HttpRequest.STATE_DONE:
                    Log.wtf("Awesome ", request1.getResponseText());
                    switch (request1.getStatus()) {
                        case HttpURLConnection.HTTP_OK:
                            sendButton.setEnabled(true);
                            break;
                        case HttpURLConnection.HTTP_NOT_ACCEPTABLE:
                            alertMessage("Can't provide feedback today.");
                            sendButton.setEnabled(false);
                    }
            }
        });
        request.setOnErrorListener((request12, readyState, error, exception) -> {
            // TODO: 18/04/2018 on error code
        });
        request.open("POST", "http://178.62.69.210:8000/api/can-provide-feedback");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("device_id", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.send(jsonObject.toString());
    }

    private void sendFeedback(String deviceId, String feedback, int classId) {
        HttpRequest request = new HttpRequest(getApplicationContext());
        request.setOnReadyStateChangeListener((request1, readyState) -> {
            switch (readyState) {
                case HttpRequest.STATE_DONE:
                    Log.wtf("Awesome ", request1.getResponseText());
                    switch (request1.getStatus()) {
                        case HttpURLConnection.HTTP_CREATED:
                            Log.wtf("Awesome ", request1.getResponseText());
                            alertMessage("Feedback Sent");
                            // TODO: 18/04/2018
                            break;
                        case HttpURLConnection.HTTP_BAD_REQUEST:
                            alertMessage("Can't provide feedback today.");
                    }
            }
        });
        request.setOnErrorListener((request12, readyState, error, exception) -> {
            // TODO: 18/04/2018 on error code
        });
        request.open("POST", "http://178.62.69.210:8000/api/feedback");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("text", feedback);
            jsonObject.put("device_id", deviceId);
            jsonObject.put("klass", classId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        request.send(jsonObject.toString());
    }
}
