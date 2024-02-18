package com.example.coursepr_vorobyevp_pr_21102;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

public class RecordActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1;

    private ImageButton btnRecord;
    private ImageButton btnPause;
    private ImageButton btnDelete;
    private Chronometer chronometer;
    private long pausedTime = 0;

    private WaveformView waveformView;
    private MediaRecorder mediaRecorder;
    private String outputFile;

    private boolean isRecording = false;
    private boolean isPaused = false;

    private Visualizer visualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        btnRecord = findViewById(R.id.btnRecord);
        btnPause = findViewById(R.id.btnPause);
        btnDelete = findViewById(R.id.btnDelete);
        chronometer = findViewById(R.id.tvRecordingTimer);
        waveformView = findViewById(R.id.waveformView); // Находим элемент WaveformView из разметки
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecording) {
                    startRecording();
                } else {
                    stopRecording();
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPaused) {
                    pauseRecording();
                } else {
                    resumeRecording();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRecording();
            }
        });

        // Проверяем разрешения на запись и доступ к файлам
        if (checkPermissions()) {
            // Разрешения уже предоставлены
            createRecordingDirectory();
        } else {
            // Запрашиваем разрешения
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        int recordPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return recordPermission == PackageManager.PERMISSION_GRANTED && storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Разрешения предоставлены
                createRecordingDirectory();
            } else {
                // Разрешения не предоставлены, обрабатываем это
                Toast.makeText(this, "Для записи аудио и доступа к файлам необходимо предоставить разрешения", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void createRecordingDirectory() {
        File directory = new File(getExternalFilesDir(null), "Recordings");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private void startRecording() {
        try {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            File directory = new File(getExternalFilesDir(null), "Recordings");
            outputFile = directory.getAbsolutePath() + "/recording.3gp";
            mediaRecorder.setOutputFile(outputFile);

            mediaRecorder.prepare();
            mediaRecorder.start();

            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();

            btnPause.setVisibility(View.VISIBLE);

            isRecording = true;

            // Создаем AudioRecord для получения audioSessionId
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 4096);
            audioRecord.startRecording();
            int audioSessionId = audioRecord.getAudioSessionId();
            audioRecord.stop();
            audioRecord.release();

            visualizer = new Visualizer(audioSessionId);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    updateVisualizer(waveform);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    // Не используется
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);
            visualizer.setEnabled(true);
        } catch (IOException e) {
            // Обработка исключения
            Toast.makeText(this, "Ошибка при записи аудио", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (SecurityException e) {
            // Обработка исключения безопасности
            Toast.makeText(this, "Недостаточно разрешений для записи аудио", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            chronometer.stop();
            chronometer.setBase(SystemClock.elapsedRealtime());

            btnRecord.setImageResource(R.drawable.ic_record);

            isRecording = false;
            isPaused = false;

            // Отключаем и освобождаем Visualizer
            if (visualizer != null) {
                visualizer.setEnabled(false);
                visualizer.release();
                visualizer = null;
            }
        }
    }


    private void pauseRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.pause();
            pausedTime = SystemClock.elapsedRealtime() - chronometer.getBase();
            chronometer.stop();
            btnPause.setImageResource(R.drawable.ic_play);
            isPaused = true;
        }
    }

    private void resumeRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.resume();
            chronometer.setBase(SystemClock.elapsedRealtime() - pausedTime);
            chronometer.start();
            btnPause.setImageResource(R.drawable.ic_pause);
            isPaused = false;
        }
    }

    private void deleteRecording() {
        stopRecording();

        if (outputFile != null) {
            File file = new File(outputFile);
            boolean deleted = file.delete();
            if (deleted) {
                Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Не удалось удалить запись", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateVisualizer(byte[] waveform) {
        waveformView.setWaveform(waveform); // Обновляем визуализацию на экране
    }
}
