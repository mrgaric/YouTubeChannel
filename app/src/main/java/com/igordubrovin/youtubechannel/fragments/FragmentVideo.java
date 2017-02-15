package com.igordubrovin.youtubechannel.fragments;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.igordubrovin.youtubechannel.R;

/**
 * Created by Игорь on 15.02.2017.
 */

public class FragmentVideo extends YouTubePlayerFragment implements YouTubePlayer.OnInitializedListener{

    YouTubePlayer player;
    private String videoId;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initialize(getResources().getString(R.string.youtube_apikey), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }

    public void setVideoId(String videoId){
        if (videoId != null && !videoId.equals(this.videoId)){
            this.videoId = videoId;
            if (player != null){
                player.cueVideo(videoId);
            }
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {

        this.player = youTubePlayer;
        youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
        player.setOnFullscreenListener((YouTubePlayer.OnFullscreenListener) getActivity());

        if (!b && videoId != null){
            youTubePlayer.cueVideo(videoId);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        this.player = null;
    }

    public  void backNormal(){
        player.setFullscreen(false);
    }

}
