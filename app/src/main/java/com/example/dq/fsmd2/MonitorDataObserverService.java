package com.example.dq.fsmd2;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorDataObserverService extends IntentService {

    private boolean error;
    private String responseString;

    public static final int DATA_INTERVAL = 30000;

    public static final String FSMD_WLAN = "FSMD WLAN";
    public static final String HUB_IP_ADDRESS = "192.168.1.2";
    public static final String HUB_DIRECTORY = "data-request";

    private String ssid;

    private String dataPageUrl;
    private Context context;
    private Callback activity;

    public MonitorDataObserverService() {
        super("MonitorDataObserverService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ssid = "";

        dataPageUrl = "http://" + HUB_IP_ADDRESS + "/" + HUB_DIRECTORY;
        context = this.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        Log.d("DEBUG", "On destroy service");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class LocalBinder extends Binder {
        public MonitorDataObserverService getServiceInstance() {
            return MonitorDataObserverService.this;
        }
    }

    public void registerClient(Activity activity){
        this.activity = (Callback) activity;
    }

    public interface Callback {
        void addMonitorData(MonitorData monitorData);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                    ssid = wifiInfo.getSSID().replaceAll("^\\s+|\\s+$", "").replaceAll("\"", "");
                }
            }
        }
            error = false;
            final Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (ssid.equals(FSMD_WLAN)) {
                        try {
                            URLConnection connection = new URL(dataPageUrl).openConnection();

                            BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                            StringBuilder sb = new StringBuilder();
                            String output;
                            while ((output = br.readLine()) != null) {
                                sb.append(output);
                                sb.append(System.lineSeparator());
                            }
                            responseString = sb.toString();
                        } catch (Exception e) {
                            error = true;
                            responseString = e.getLocalizedMessage();
                        }

                        String result = responseString.replace("HTTP/1.1 200 OKContent-type:text/html", "");
                        Log.d("WEBPAGE_DATA", result);
                    }
                }
            }, 0, DATA_INTERVAL / 2);
    }
}