package com.analysisgroup.streamingapp.LiveVideoPlayer;

import static com.analysisgroup.streamingapp.MainActivity.RTMP_BASE_URL;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.analysisgroup.streamingapp.R;

public class LiveVideoPlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video_player);

    }
}