package com.igordubrovin.youtubechannel.fragments;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import com.igordubrovin.youtubechannel.R;

public class FragmentVideo extends YouTubePlayerFragment implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayer player;
    private String videoId;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        initialize(getResources().getString(R.string.youtube_apikey), this);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
        }
        super.onDestroy();
    }

    public void setVideoId(String videoId) {
        if (videoId != null && !videoId.equals(this.videoId)) {
            this.videoId = videoId;
            if(player != null) {
                player.cueVideo(videoId);
            }
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean restored) {

        this.player = player;
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
        player.setOnFullscreenListener((YouTubePlayer.OnFullscreenListener) getActivity());

        if (!restored && videoId != null) {
            player.cueVideo(videoId);
        }


    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {

        this.player = null;

    }

    public void backnormal() {
        player.setFullscreen(false);
    }
}
