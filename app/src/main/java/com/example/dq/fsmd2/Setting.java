package com.example.dq.fsmd2;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Setting extends AppCompatActivity {
    public ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final TextView input = (TextView) findViewById(R.id.ipTF);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String IP = preferences.getString("IP", "");
        input.setText(IP.replace("http://", "").replace("/", ""));

        Button ok = (Button) findViewById(R.id.okB);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(Setting.this, "Checking", "Checking the IP.", false);
                String IP = input.getText().toString();
                IP = "http://" + IP + "/";
                new RequestTask().execute(IP + "START", IP);
            }
        });
    }

    class RequestTask extends AsyncTask<String, String, String> {
        private String IP = "";
        private boolean error = false;

        @Override
        protected String doInBackground(String... uri) {
            IP = uri[1];
            String responseString;

            try {
                URLConnection connection = new URL(uri[0]).openConnection();
                connection.setRequestProperty("Connection", "close");
                BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                responseString = sb.toString();
            } catch (Exception e) {
                error = true;
                responseString = e.getLocalizedMessage();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            dialog.dismiss();

            Toast.makeText(Setting.this, result, Toast.LENGTH_LONG).show();
            if (!error) {
                if (result.equals("YES")) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Setting.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("IP", IP);
                    editor.apply();
                    Setting.this.finish();
                } else {
                    Toast.makeText(Setting.this, "This is a server, but it is not the Arduino/LinkIt ONE and/or running the correct software.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(Setting.this, "Oops, something went wrong. Error message:" + result,Toast.LENGTH_LONG).show();
            }
        }
    }
}