package com.igordubrovin.youtubechannel.adapter;

import android.animation.Animator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.igordubrovin.youtubechannel.R;
import com.igordubrovin.youtubechannel.utils.MySingleton;
import com.igordubrovin.youtubechannel.utils.Utils;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.marshalchen.ultimaterecyclerview.animators.internal.ViewHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Игорь on 15.02.2017.
 */

public class AdapterList extends UltimateViewAdapter<RecyclerView.ViewHolder> {

    private final ArrayList<HashMap<String, String>> DATA;
    private final ImageLoader imageLoader;
    private Interpolator interpolator = new LinearInterpolator();
    private int lastPosition = 5;
    private  final  int ANIMATION_DURATION = 300;

    public AdapterList(Context context, ArrayList<HashMap<String, String>> list) {
        imageLoader = MySingleton.getInstance(context).getImageLoader();
        DATA = list;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_video_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getAdapterItemCount() {
        return DATA.size();
    }

    @Override
    public long generateHeaderId(int position) {
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < getItemCount() && (customHeaderView != null ? position <= DATA.size()
        : position < DATA.size()) && (customHeaderView == null || position > 0)){
            HashMap<String, String> item;
            item = DATA.get(customHeaderView != null ? position - 1 : position);
            ((ViewHolder)holder).txtTitle.setText(item.get(Utils.KEY_TITLE));
            ((ViewHolder)holder).txtDuration.setText(item.get(Utils.KEY_DURATION));
            ((ViewHolder)holder).txtPublished.setText(item.get(Utils.KEY_PUBLISHEDAT));

            imageLoader.get(item.get(Utils.KEY_URL_THUMBNAILS), ImageLoader.getImageListener(((ViewHolder)holder).imgThumbnail, R.mipmap.empty_photo, R.mipmap.empty_photo));
        }

        boolean isFirstOnly = true;
        if (!isFirstOnly || position > lastPosition) {
            // Add animation to the item
            for (Animator anim : getAdapterAnimations(holder.itemView,
                    AdapterAnimationType.SlideInLeft)) {
                anim.setDuration(ANIMATION_DURATION).start();
                anim.setInterpolator(interpolator);
            }
            lastPosition = position;
        } else {
            ViewHelper.clear(holder.itemView);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public static class ViewHolder extends UltimateRecyclerviewViewHolder{
        private TextView txtTitle, txtPublished, txtDuration;
        private ImageView imgThumbnail;

        public ViewHolder (View view){
            super(view);
            txtTitle = (TextView)view.findViewById(R.id.txtTitle);
            txtDuration = (TextView)view.findViewById(R.id.txtDuration);
            txtPublished = (TextView)view.findViewById(R.id.txtPublishedAt);
            imgThumbnail = (ImageView)view.findViewById(R.id.imgThumbnail);
        }
    }

}
