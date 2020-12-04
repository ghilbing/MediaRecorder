package com.hilbing.mediarecorder.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hilbing.mediarecorder.database.DBHelper;
import com.hilbing.mediarecorder.models.RecordingItem;

import java.io.File;
import java.io.IOException;

public class RecordingService extends Service {

    MediaRecorder mediaRecorder;
    long mStartingTimeMillis = 0;
    long mElapsedMillis = 0;

    File file;
    String fileName;

    DBHelper dbHelper;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }

    private void startRecording() {
            Long tsLong = System.currentTimeMillis()/1000;
            String ts = tsLong.toString();

            fileName = "audio_"+ts;

            file = new File(Environment.getExternalStorageDirectory() + "/MySoundRec/" + fileName + ".mp3");

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(file.getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioChannels(1);

            try{
                mediaRecorder.prepare();
                mediaRecorder.start();
                mStartingTimeMillis = System.currentTimeMillis();
            }catch (IOException e){
                e.printStackTrace();
            }

    }

    private void stopRecording(){
        try{
            mediaRecorder.stop();
            mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
            mediaRecorder.release();
            Toast.makeText(getApplicationContext(), "Recording saved at "+ file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            //Add to database
            RecordingItem recordingItem = new RecordingItem(fileName, file.getAbsolutePath(), mElapsedMillis, System.currentTimeMillis());
            dbHelper.addRecording(recordingItem);
        } catch (RuntimeException e){
            file.delete();
        } finally {
            mediaRecorder.release();
            mediaRecorder = null;
        }

    }

    @Override
    public void onDestroy() {
        if(mediaRecorder != null)
            stopRecording();
        super.onDestroy();

    }
}
