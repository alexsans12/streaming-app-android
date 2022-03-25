package com.analysisgroup.streamingapp.LiveVideoPlayer;

import static com.analysisgroup.streamingapp.MainActivity.RTMP_BASE_URL;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.analysisgroup.streamingapp.R;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.analytics.PlaybackStatsListener;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AdViewProvider;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.Util;


public class LiveVideoPlayerActivity extends AppCompatActivity {

    private PlaybackStatsListener playbackStatsListener;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;

    boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_video_player);

        playerView = findViewById(R.id.player_view);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (Util.SDK_INT>23) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(Util.SDK_INT<23) {
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(Util.SDK_INT>23) {
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if(exoPlayer != null) {
            playbackPosition = exoPlayer.getCurrentPosition();
            currentWindow = exoPlayer.getCurrentMediaItemIndex();
            playWhenReady = exoPlayer.getPlayWhenReady();
            exoPlayer.removeListener(playbackStatsListener);
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void hideSystemUI() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void initializePlayer() {
        if(exoPlayer == null) {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
            trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd());
            exoPlayer = new ExoPlayer.Builder(this).setTrackSelector(trackSelector).build();
        }

        playerView.setPlayer(exoPlayer);
        Uri uri = Uri.parse(RTMP_BASE_URL);
        MediaSource mediaSource = buildMediaSource(uri);

        exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.seekTo(currentWindow, playbackPosition);
        exoPlayer.addAudioOffloadListener((ExoPlayer.AudioOffloadListener) playbackStatsListener);
        exoPlayer.setMediaSource(mediaSource, false);

    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSource = new DefaultDataSource.Factory(this);
        DashMediaSource.Factory mediaSourceFactory = new DashMediaSource.Factory(dataSource);
        return  mediaSourceFactory.createMediaSource(MediaItem.fromUri(uri));
    }

    private class PlaybackStatsListener implements Player.Listener {

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady,int playbackState) {
            Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, playbackState);

            String stateString;

            switch(playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "Exoplayer.STATE_IDLE";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "Exoplayer.STATE_IDLE";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "Exoplayer.STATE_READY";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "Exoplayer.STATE_ENDED";
                    break;
                default:
                    stateString = "UNKNOWN_STATE";
                    break;
            }

            Log.d("LiveVideoPlayerActivity", "Changed state to " + stateString + " playWhenREady: " + playWhenReady);
        }
    }
}