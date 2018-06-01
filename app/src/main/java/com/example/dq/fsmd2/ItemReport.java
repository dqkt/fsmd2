package com.example.dq.fsmd2;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemReport {

    public static int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private Context context;
    private Item item;
    private MonitorData.MonitorDataDao monitorDataDao;
    private List<MonitorData> monitorDataList;

    public ItemReport(Context context, Item item, MonitorData.MonitorDataDao monitorDataDao) {
        this.context = context;
        this.item = item;
        this.monitorDataDao = monitorDataDao;
    }

    public void toCsv() {
        if (isExternalStorageWritable()) {
            SimpleDateFormat currentTimeFormatter = new SimpleDateFormat("HH'h'mm'm'ss's'_MM-dd-yyyy", Locale.US);
            String fileName = item.getName().replaceAll("\\s+", "_").toLowerCase() + "_" + currentTimeFormatter.format(new Date()) + ".csv";
            File docsFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/LoggerData");
            boolean isPresent = true;
            if (!docsFolder.exists()) {
                isPresent = docsFolder.mkdir();
            }
            if (isPresent) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    } else {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                }
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/LoggerData", fileName);
                try {
                    if (file.exists()) {
                        if (file.delete()) {
                            if (!file.createNewFile()) {
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    PrintWriter writer = new PrintWriter(file, "UTF-8");
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z MM-dd-yyyy", Locale.US);

                    Thread getMonitorDataListThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            monitorDataList = monitorDataDao.getAllCurrentMonitorDatasFromItem(item.getItemID());
                        }
                    });
                    getMonitorDataListThread.start();
                    while (getMonitorDataListThread.isAlive()) ;

                    writer.println("Name," + item.getName());
                    writer.println("IP," + item.getIp());
                    writer.println("Time added," + formatter.format(item.getDateAdded()));
                    writer.println();
                    writer.println("# data points," + String.valueOf(monitorDataList.size()));
                    writer.println();
                    writer.println("Avg. Vibration Magnitude (g),Avg. X-Axis Vibration (g),Avg. Y-Axis Vibration (g),Avg. Z-Axis Vibration (g),"
                            + "Peak Vibration Magnitude (g),Peak X-Axis Vibration (g),Peak Y-Axis Vibration (g),Peak Z-Axis Vibration (g),"
                            + "Latitude,Longitude,Time");

                    MonitorData monitorData;
                    VibrationData vibrationData;
                    PositionData positionData;
                    Date dateAdded;

                    int numMonitorData = monitorDataList.size();
                    for (int i = 0; i < numMonitorData; i++) {
                        monitorData = monitorDataList.get(i);
                        vibrationData = monitorData.getVibData();
                        positionData = monitorData.getPosData();
                        dateAdded = monitorData.getTimeData();

                        writer.println(vibrationData.getAvgVibMagnitude() + "," + vibrationData.getAvgXVibration() + ","
                                + vibrationData.getAvgYVibration() + "," + vibrationData.getAvgZVibration() + ","
                                + vibrationData.getPeakVibMagnitude() + "," + vibrationData.getPeakXVibration() + ","
                                + vibrationData.getAvgYVibration() + "," + vibrationData.getAvgZVibration() + ","
                                + positionData.getLatitude() + "," + positionData.getLongitude() + "," + formatter.format(dateAdded));
                    }

                    writer.close();
                    Toast.makeText(context, "Exported item report CSV successfully.", Toast.LENGTH_LONG).show();

                    MimeTypeMap map = MimeTypeMap.getSingleton();
                    String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
                    String type = map.getMimeTypeFromExtension(ext);

                    if (type == null)
                        type = "*/*";

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                    intent.setDataAndType(fileUri, type);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    PendingIntent openExportedData = PendingIntent.getActivity(context, 0, intent, 0);
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "LOGGER_CHANNEL")
                            .setSmallIcon(android.R.drawable.stat_sys_download_done)
                            .setContentTitle(fileName)
                            .setContentText("Exported data for '" + item.getName() + "' to " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/LoggerData.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setStyle(new NotificationCompat.BigTextStyle())
                            .setContentIntent(openExportedData)
                            .setAutoCancel(true);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(0, mBuilder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to export item report CSV", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(context, "Cannot write to external storage", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

}
