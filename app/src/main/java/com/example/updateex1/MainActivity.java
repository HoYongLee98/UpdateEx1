package com.example.updateex1;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.updateex1.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.*;
import java.net.*;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    String File_Name = "FocusBuddy.apk";
    String File_extend = "apk";

    String fileURL = "http://focusbuddy.co.kr/media/downloads/app-debug.apk";
    String Save_Path;
    String Save_folder = "/mydown";

    Context context;
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    String CHANNEL_ID = "my_Chanel";
    String[] list = {"종료", "업데이트"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        context = this.getBaseContext();
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.abc_vector_test)
                .setContentTitle("title")
                .setContentText("text")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("업데이트가 필요합니다.");
//            builder.setItems(list, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    Toast.makeText(getApplicationContext(), list[i], Toast.LENGTH_LONG).show();
//                }
//
//            });
                builder.setNegativeButton("종료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "종료하기", Toast.LENGTH_LONG).show();
                        dialogInterface.cancel();
                    }
                });
                builder.setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "업데이트하기", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

                AlertDialog alertD = builder.create();
                alertD.show();

                DownloadThread dd = new DownloadThread();
                dd.start();
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void installApk() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File_Name);

        Uri fileUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider",file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
        finish();
        Log.e("InstallApk", "End");
    }

    class DownloadThread extends Thread {

        DownloadThread(){}


        @Override
        public void run() {
            ex2();

        }
        public void ex1(){
            try {
                Log.e("asdfDOWNLOAD", "start");
                URL url = new URL(fileURL);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();


                urlConnection.connect();

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File_Name);

                if(file.exists()){
                    file.delete();
                }

                FileOutputStream fileOutput = new FileOutputStream(file);

                InputStream inputStream = urlConnection.getInputStream();
                int totalSize = urlConnection.getContentLength();

                int downloadedSize = 0;
                byte[] buffer = new byte[1024];
                int bufferLength = 0;
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
//                    mProgressBar.setProgress(downloadedSize);
                    Log.e("DOWNLOAD", "saving...");
                }
                fileOutput.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("DOWNLOAD", "end");

            Log.e("DOWNLOAD", "InstallAPK Method Called");
            installApk();
        }
        public void ex2(){
            try {
                Log.e("asdfDOWNLOAD", "start");

                URL url = new URL(fileURL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File_Name);
                if(file.exists()){
                    file.delete();
                }
                FileOutputStream fileOutput = new FileOutputStream(file);
                InputStream inputStream = urlConnection.getInputStream();

                byte[] buffer = new byte[1024];

                int len = urlConnection.getContentLength();
                int Read;
                for(;;){
                    Read = inputStream.read(buffer);
                    if(Read <= 0) break;
                    fileOutput.write(buffer, 0, Read);
                    mBuilder.setProgress(len,len -Read, false);
                    Log.e("DOWNLOAD", "saving...");
                }
                mBuilder.setContentText("Download Complete");
                mBuilder.setProgress(0,0,false);
                mNotifyManager.notify(101, mBuilder.build());
                fileOutput.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("DOWNLOAD", "end");

            Log.e("DOWNLOAD", "InstallAPK Method Called");
            installApk();
        }
    }

    private void createNotificationChannel(){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "채널이름";
            String description = "채널설명";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel("my_notification_channel", name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}