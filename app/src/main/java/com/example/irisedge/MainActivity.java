package com.example.irisedge;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ImageView imgRecord;
    private TextView txtSpeech,txtFromPython;
    private Button ttsButton, sendData;
    private ExtendedFloatingActionButton button;
    private TextToSpeech toSpeech;
    String result;

    static final int REQUEST_VIDEO_CAPTURE = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        imgRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speech();
            }
        });
//        sendData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new SendDataTask().execute(txtSpeech.getText().toString());
//            }
//        });
//        ttsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setTxtSpeech(txtFromPython.getText().toString());
//            }
//        });



    }


    public void setTxtSpeech(String text){
        toSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR){
                    toSpeech.setLanguage(Locale.UK);
                }

                toSpeech.speak(text,TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }
    public void speech(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Start speaking");
        startActivityForResult(intent,100);
    }



    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
        else {
            //display error state to the user
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK){
            Toast.makeText(this, "taking video", Toast.LENGTH_SHORT).show();
        } else if (requestCode == 100 && resultCode == RESULT_OK) {
            if (data != null) {
                String recorded = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);
                txtSpeech.setText(recorded);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void sendData(String data){

    }
//    private class SendDataTask extends AsyncTask<String, Void, Void>{
//
//        @Override
//        protected Void doInBackground(String... params) {
//            String finalResult;
//            String data = params[0];
//            OkHttpClient client = new OkHttpClient();
//            RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\n\t\"text\": \"" + data + "\"\n}");
//            Request request = new Request.Builder()
//                    .url("http://192.168.0.104:5000/process")
//                    .post(body)
//                    .addHeader("Content-Type", "application/json")
//                    .build();
//            try {
//                Response response = client.newCall(request).execute();
//                finalResult = response.body().string();
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void unused) {
//            super.onPostExecute(unused);
//
//        }
//    }
private class SendDataTask extends AsyncTask<String, Void, String>{

    @Override
    protected String doInBackground(String... params) {
        String finalResult;
        String data = params[0];
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\n\t\"text\": \"" + data + "\"\n}");
        Request request = new Request.Builder()
                .url("http://192.168.0.104:5000/process")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            finalResult = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return finalResult;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        txtFromPython.setText(result);
    }
}




    private void initViews() {
        button = findViewById(R.id.btnTakePicture);
        imgRecord = findViewById(R.id.imgRecord);
        txtSpeech = findViewById(R.id.txtSpeech);
//        ttsButton = findViewById(R.id.btnTTS);
//        txtFromPython = findViewById(R.id.txtFromPython);
//        sendData = findViewById(R.id.btnSendData);
    }

}
