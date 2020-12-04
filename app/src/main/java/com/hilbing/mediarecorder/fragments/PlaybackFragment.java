package com.hilbing.mediarecorder.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.hilbing.mediarecorder.R;
import com.hilbing.mediarecorder.models.RecordingItem;
import com.melnykov.fab.FloatingActionButton;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaybackFragment extends DialogFragment {

    @BindView(R.id.txt_file_name_playback)
    TextView fileName;
    @BindView(R.id.txt_length)
    TextView length;
    @BindView(R.id.txt_current_progress)
    TextView currentProgress;
    @BindView(R.id.seekbar)
    SeekBar seekBar;
    @BindView(R.id.fab_play)
    FloatingActionButton floatingActionButton;

    private RecordingItem recordingItem;
    private Handler handler = new Handler();
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    long minutes = 0;
    long seconds = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordingItem = (RecordingItem) getArguments().getSerializable("item");
        minutes = TimeUnit.MILLISECONDS.toMinutes(recordingItem.getLength());
        seconds = TimeUnit.MILLISECONDS.toSeconds(recordingItem.getLength()) - TimeUnit.MINUTES.toSeconds(minutes);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_playback, null);
        ButterKnife.bind(this, view);

        setSeekBarValues();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay(isPlaying);
                isPlaying = !isPlaying;
            }
        });

        fileName.setText(recordingItem.getName());
        length.setText(String.format("%02d:%02d", minutes, seconds));

        builder.setView(view);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return builder.create();
    }

    private void onPlay(boolean isPlaying) {
        if(!isPlaying){
            if(mediaPlayer == null){
                try {
                    startPlaying();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                pausePlaying();
            }
        }
    }

    private void pausePlaying() {
        floatingActionButton.setImageResource(R.drawable.ic_play_white);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.pause();
    }

    private void startPlaying() throws IOException {

        floatingActionButton.setImageResource(R.drawable.ic_pause_white);
        mediaPlayer = new MediaPlayer();

        mediaPlayer.setDataSource(recordingItem.getPath());
        mediaPlayer.prepare();
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
            }
        });
        updateSeekBar();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setSeekBarValues(){
        ColorFilter colorFilter = new LightingColorFilter(getResources().getColor(R.color.purple_500), getResources().getColor(R.color.purple_500));
        seekBar.getProgressDrawable().setColorFilter(colorFilter);
        seekBar.getThumb().setColorFilter(colorFilter);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if(mediaPlayer != null && b){
                    mediaPlayer.seekTo(progress);
                    handler.removeCallbacks(mRunnable);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()) - TimeUnit.MINUTES.toSeconds(minutes);
                    currentProgress.setText(String.format("%02d:%02d", minutes, seconds));
                    updateSeekBar();
                } else if(mediaPlayer == null && b) {
                    try {
                        prepareMediaPlayerFromPoint(progress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void prepareMediaPlayerFromPoint(int currentProgress) throws IOException {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(recordingItem.getPath());
        mediaPlayer.prepare();
        seekBar.setMax(mediaPlayer.getDuration());
        mediaPlayer.seekTo(currentProgress);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopPlaying();
            }
        });
    }

    private void stopPlaying() {
        floatingActionButton.setImageResource(R.drawable.ic_play_white);
        handler.removeCallbacks(mRunnable);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;

        seekBar.setProgress(seekBar.getMax());
        isPlaying = !isPlaying;

        currentProgress.setText(length.getText());
        seekBar.setProgress(seekBar.getMax());
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if(mediaPlayer != null){
                int mCurrentPosition = mediaPlayer.getCurrentPosition();
                seekBar.setProgress(mCurrentPosition);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(mCurrentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(mCurrentPosition) - TimeUnit.MINUTES.toSeconds(minutes);
                currentProgress.setText(String.format("%02d:%02d", minutes, seconds));
                updateSeekBar();

            }
        }
    };

    private void updateSeekBar() {
        handler.postDelayed(mRunnable, 1000);
    }
}
