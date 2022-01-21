package com.example.updateex1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.example.updateex1.support.PermissionSupport;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;
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

    String File_Name = "FocusBuddy.apk"; //다운로드 시 저장할 이름 설정
    String File_extend = "apk"; //다운로드 파일 형식

    String fileURL = "http://focusbuddy.co.kr/media/downloads/app-debug.apk"; //apk 파일 URL
    String Save_Path; //저장소 위치
    String Save_folder = "/mydown"; //저장소 내부 폴더

    //Progress Bar Component
    Context context;
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    String CHANNEL_ID = "my_Chanel";
    String[] list = {"종료", "업데이트", "확인"};

    //Permission Component
    private PermissionSupport permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        //Progress Bar Setup
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

        //Update Button Listener
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("업데이트가 필요합니다.");
                builder.setCancelable(false);

                if(!permission.checkPermission()){
                    Toast.makeText(getApplicationContext(), "파일 변경 권한 필요", Toast.LENGTH_LONG).show();
                    moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }

                builder.setNegativeButton(list[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "종료하기", Toast.LENGTH_LONG).show();
                        dialogInterface.cancel();
                        moveTaskToBack(true);
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
                builder.setPositiveButton(list[1], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "업데이트하기", Toast.LENGTH_LONG).show();
                        DownloadThread dd = new DownloadThread();
                        dd.start();
                        finish();
                    }
                });

                AlertDialog alertD = builder.create();
                alertD.show();


            }
        }));

        //Storage Permission Check
        permissionCheck();

        //Install Unknown apps Check
        if(!getPackageManager().canRequestPackageInstalls()){

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("install unknown apps 승인이 필요합니다.");
            builder.setCancelable(false);

            builder.setNegativeButton(list[0], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "종료", Toast.LENGTH_LONG).show();
                    dialogInterface.cancel();
                    moveTaskToBack(true);
                    finish();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            builder.setPositiveButton(list[2], new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "확인", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
                    //startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:UpdateEx1.package")));
                    finish();
                }
            });
            AlertDialog alertD = builder.create();
            alertD.show();
        }

        createNotificationChannel();
    }

    private void permissionCheck(){
        if(Build.VERSION.SDK_INT >= 23){
            permission = new PermissionSupport(this, this);
            if(!permission.checkPermission()){
                permission.requestPermission();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(!permission.permissionResult(requestCode, permissions, grantResults)){
            //permission.requestPermission();
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
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
            ex1();

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
                mNotifyManager.notify(101, mBuilder.build());
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
//                    mProgressBar.setProgress(downloadedSize);

                    mBuilder.setProgress(totalSize, downloadedSize, false);
                    Log.e("DOWNLOAD", "saving...");
                }
//                Intent install_intent = new Intent(Intent.ACTION_VIEW);
//                install_intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), File_Name)),"application/vnd.android.package-archive");
//                PendingIntent pending = PendingIntent.getActivity(MainActivity.this,0, install_intent, 0);

                mBuilder.setContentText("Download Complete");
                mBuilder.setProgress(0,0,false);
                //mBuilder.setContentIntent(pending);
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
                int downPP = 0;
                int Read;
                mNotifyManager.notify(101, mBuilder.build());
                for(;;){
                    Read = inputStream.read(buffer);
                    if(Read <= 0) break;
                    fileOutput.write(buffer, 0, Read);
                    downPP += 1024;
                    mBuilder.setProgress(len,downPP, false);
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

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}