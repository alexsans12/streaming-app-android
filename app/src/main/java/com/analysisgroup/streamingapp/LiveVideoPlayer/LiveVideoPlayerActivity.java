package com.analysisgroup.streamingapp.LiveVideoPlayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.analysisgroup.streamingapp.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.ext.ima.ImaServerSideAdInsertionMediaSource;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.util.DebugTextViewHelper;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class LiveVideoPlayerActivity extends AppCompatActivity implements StyledPlayerControlView.VisibilityListener {

    // Saved instance state keys.
    private static final String KEY_SERVER_SIDE_ADS_LOADER_STATE = "server_side_ads_loader_state";
    private static final String KEY_ITEM_INDEX = "item_index";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";

    protected StyledPlayerView styledPlayerView;
    protected LinearLayout debugRootView;
    protected TextView debugTextView;
    protected @Nullable
    ExoPlayer player;

    private DebugTextViewHelper debugViewHelper;
    private DataSource.Factory dataSourceFactory;
    protected MediaSource mediaSource;
    protected Uri uri;

    // For ad playback only.
    @Nullable
    private ImaAdsLoader clientSideAdsLoader;
    private ImaServerSideAdInsertionMediaSource.AdsLoader.@MonotonicNonNull State
            serverSideAdsLoaderState;

    private boolean startAutoPlay;
    private int startItemIndex;
    private long startPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_video_player);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = getIntent().getExtras();
        String URL_STREAM = bundle.getString("streamUrl");

        uri = Uri.parse(URL_STREAM);

        findView();

        if (savedInstanceState != null) {
            // Restore as DefaultTrackSelector.Parameters in case ExoPlayer specific parameters were set.
            startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
            startItemIndex = savedInstanceState.getInt(KEY_ITEM_INDEX);
            startPosition = savedInstanceState.getLong(KEY_POSITION);
            Bundle adsLoaderStateBundle = savedInstanceState.getBundle(KEY_SERVER_SIDE_ADS_LOADER_STATE);
            if (adsLoaderStateBundle != null) {
                serverSideAdsLoaderState =
                        ImaServerSideAdInsertionMediaSource.AdsLoader.State.CREATOR.fromBundle(
                                adsLoaderStateBundle);
            }
        } else {
            clearStartPosition();
        }
    }

    public void findView() {
        debugRootView = findViewById(R.id.debug_linear_layout);
        debugTextView = findViewById(R.id.debug_text_view);

        styledPlayerView = findViewById(R.id.player_view);
        styledPlayerView.setControllerVisibilityListener(this);
        styledPlayerView.setErrorMessageProvider(new PlayerErrorMessageProvider());
        styledPlayerView.requestFocus();
    }

    protected boolean initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(/* context= */ this)
                            .setMediaSourceFactory(createMediaSourceFactory())
                            .build();
            player.addListener(listener);
            player.setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
            player.setPlayWhenReady(startAutoPlay);
            styledPlayerView.setPlayer(player);

            clientSideAdsLoader = new ImaAdsLoader.Builder(/* context= */ this).build();
            clientSideAdsLoader.setPlayer(player);

            debugViewHelper = new DebugTextViewHelper(player, debugTextView);
            debugViewHelper.start();
        }

        boolean haveStartPosition = startItemIndex != C.INDEX_UNSET;

        if (haveStartPosition) {
            player.seekTo(startItemIndex, startPosition);
        }

        createMediaSource();
        player.setMediaSource(mediaSource);
        player.prepare();

        return true;
    }

    private MediaSource.Factory createMediaSourceFactory() {
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this);

        MediaSource.Factory mediaSourceFactory =
                new DefaultMediaSourceFactory(dataSourceFactory)
                        .setAdsLoaderProvider(unusedAdTagUri -> clientSideAdsLoader)
                        .setAdViewProvider(styledPlayerView);

        return mediaSourceFactory;
    }

    private void createMediaSource() {
        player.seekTo(0);

        DataSource dataSource = new DefaultDataSource(this,
                Util.getUserAgent(this, getApplicationInfo().name), false);

        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(this, () -> dataSource);

        Uri adUri = Uri.parse("https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/single_preroll_skippable&sz=640x480&ciu_szs=300x250%2C728x90&gdfp_req=1&output=vast&unviewed_position_start=1&env=vp&impl=s&correlator=");

        MediaItem.AdsConfiguration adsConfiguration = new MediaItem.AdsConfiguration.Builder(adUri).build();

        MediaItem mediaItem =
                new MediaItem.Builder()
                        .setUri(uri)
                        .setAdsConfiguration(adsConfiguration)
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .setLiveConfiguration(
                                new MediaItem.LiveConfiguration.Builder()
                                        .setMaxPlaybackSpeed(1.0f)
                                        .build())
                        .build();

        MediaSource.Factory mediaSourceFactory =
                new DefaultMediaSourceFactory(dataSourceFactory)
                        .setAdsLoaderProvider(unusedAdTagUri -> clientSideAdsLoader)
                        .setAdViewProvider(styledPlayerView);

        mediaSource = mediaSourceFactory.createMediaSource(mediaItem);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        releasePlayer();
        releaseClientSideAdsLoader();
        clearStartPosition();
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 21) {
            initializePlayer();
            if (styledPlayerView != null) {
                styledPlayerView.onResume();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Util.SDK_INT <= 21 || player == null) {
            initializePlayer();
            if (styledPlayerView != null) {
                styledPlayerView.onResume();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Util.SDK_INT <= 21) {
            if (styledPlayerView != null) {
                styledPlayerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (Util.SDK_INT > 21) {
            if (styledPlayerView != null) {
                styledPlayerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        releaseClientSideAdsLoader();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) {
            // Empty results are triggered if a permission is requested while another request was already
            // pending and can be safely ignored in this case.
            return;
        }
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializePlayer();
        } else {
            showToast(R.string.storage_permission_denied);
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateTrackSelectorParameters();
        updateStartPosition();
        outState.putBoolean(KEY_AUTO_PLAY, startAutoPlay);
        outState.putInt(KEY_ITEM_INDEX, startItemIndex);
        outState.putLong(KEY_POSITION, startPosition);
        if (serverSideAdsLoaderState != null) {
            outState.putBundle(KEY_SERVER_SIDE_ADS_LOADER_STATE, serverSideAdsLoaderState.toBundle());
        }
    }

    private Player.Listener listener = new Player.Listener() {
        @Override
        public void onRenderedFirstFrame() {
            Player.Listener.super.onRenderedFirstFrame();
            styledPlayerView.setUseController(false);
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            Player.Listener.super.onPlayerError(error);
            Toast.makeText(LiveVideoPlayerActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            releasePlayer();
            finish();
        }
    };

    // Activity input
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // See whether the player view wants to handle media or DPAD keys events.
        return styledPlayerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    protected void releasePlayer() {
        if (player != null) {
            updateTrackSelectorParameters();
            updateStartPosition();
            debugViewHelper.stop();
            debugViewHelper = null;
            player.release();
            player = null;
            styledPlayerView.setPlayer(/* player= */ null);
        }
        if (clientSideAdsLoader != null) {
            clientSideAdsLoader.setPlayer(null);
        } else {
            styledPlayerView.getAdViewGroup().removeAllViews();
        }
    }

    private void releaseClientSideAdsLoader() {
        if (clientSideAdsLoader != null) {
            clientSideAdsLoader.release();
            clientSideAdsLoader = null;
            styledPlayerView.getAdViewGroup().removeAllViews();
        }
    }

    private void updateTrackSelectorParameters() {
        if (player != null) {
            // Until the demo app is fully migrated to TrackSelectionParameters, rely on ExoPlayer to use
            // DefaultTrackSelector by default.
            //trackSelectionParameters =
            //        (DefaultTrackSelector.Parameters) player.getTrackSelectionParameters();
        }
    }

    private void updateStartPosition() {
        if (player != null) {
            startAutoPlay = player.getPlayWhenReady();
            startItemIndex = player.getCurrentMediaItemIndex();
            startPosition = Math.max(0, player.getContentPosition());
        }
    }

    protected void clearStartPosition() {
        startAutoPlay = true;
        startItemIndex = C.INDEX_UNSET;
        startPosition = C.TIME_UNSET;
    }

    private void showToast(int messageId) {
        showToast(getString(messageId));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onVisibilityChange(int visibility) {
        debugRootView.setVisibility(visibility);
    }

    private class PlayerErrorMessageProvider implements ErrorMessageProvider<PlaybackException> {

        @Override
        public Pair<Integer, String> getErrorMessage(PlaybackException e) {
            String errorString = getString(R.string.error_generic);
            Throwable cause = e.getCause();
            if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                // Special case for decoder initialization failures.
                MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                        (MediaCodecRenderer.DecoderInitializationException) cause;
                if (decoderInitializationException.codecInfo == null) {
                    if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString =
                                getString(
                                        R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                    } else {
                        errorString =
                                getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                    }
                } else {
                    errorString =
                            getString(
                                    R.string.error_instantiating_decoder,
                                    decoderInitializationException.codecInfo.name);
                }
            }
            return Pair.create(0, errorString);
        }
    }
}