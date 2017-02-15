package com.igordubrovin.youtubechannel.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.igordubrovin.youtubechannel.R;
import com.igordubrovin.youtubechannel.adapter.AdapterList;
import com.igordubrovin.youtubechannel.utils.MySingleton;
import com.igordubrovin.youtubechannel.utils.Utils;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.marshalchen.ultimaterecyclerview.ItemTouchListenerAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Игорь on 15.02.2017.
 */

public class FragmentChannelVideo extends Fragment implements View.OnClickListener {
    private static final String TAG = FragmentChannelVideo.class.getSimpleName();
    private static final String TAGS = "URL";

    private TextView mLblNoResult;
    private LinearLayout mLytRetry;
    private CircleProgressBar mPrgLoading;
    private UltimateRecyclerView mUltimateRecyclerView;


    private int mVideoType;
    private String mChannelId;


    private OnVideoSelectedListener mCallback;


    private AdapterList mAdapterList = null;

    private ArrayList<HashMap<String, String>> mTempVideoData = new ArrayList<>();
    private ArrayList<HashMap<String, String>> mVideoData     = new ArrayList<>();

    private String mNextPageToken = "";
    private String mVideoIds = "";
    private String mDuration = "00:00";

    private boolean mIsStillLoading = true;

    private boolean mIsAppFirstLaunched = true;

    private boolean mIsFirstVideo = true;

    public interface OnVideoSelectedListener {
        public void onVideoSelected(String ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_list, container, false);
        setHasOptionsMenu(true);
        Bundle bundle = this.getArguments();

        mVideoType = Integer.parseInt(bundle.getString(Utils.TAG_VIDEO_TYPE));
        mChannelId = bundle.getString(Utils.TAG_CHANNEL_ID);

        mUltimateRecyclerView       = (UltimateRecyclerView)
                view.findViewById(R.id.ultimate_recycler_view);
        mLblNoResult                = (TextView) view.findViewById(R.id.lblNoResult);
        mLytRetry                   = (LinearLayout) view.findViewById(R.id.lytRetry);
        mPrgLoading                 = (CircleProgressBar) view.findViewById(R.id.prgLoading);
        AppCompatButton btnRetry    = (AppCompatButton) view.findViewById(R.id.raisedRetry);

        btnRetry.setOnClickListener(this);
        mPrgLoading.setColorSchemeResources(R.color.accent_color);
        mPrgLoading.setVisibility(View.VISIBLE);

        mIsAppFirstLaunched = true;
        mIsFirstVideo = true;



        mVideoData = new ArrayList<>();

        mAdapterList = new AdapterList(getActivity(), mVideoData);
        mUltimateRecyclerView.setAdapter(mAdapterList);
        mUltimateRecyclerView.setHasFixedSize(false);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mUltimateRecyclerView.setLayoutManager(linearLayoutManager);
        mUltimateRecyclerView.enableLoadmore();

        mAdapterList.setCustomLoadMoreView(LayoutInflater.from(getActivity())
                .inflate(R.layout.progressbar, null));

        mUltimateRecyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, final int maxLastVisiblePosition) {
                if (mIsStillLoading) {
                    mIsStillLoading = false;
                    mAdapterList.setCustomLoadMoreView(LayoutInflater.from(getActivity())
                            .inflate(R.layout.progressbar, null));

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            getVideoData();

                        }
                    }, 1000);
                } else {
                    disableLoadmore();
                }

            }
        });
        ItemTouchListenerAdapter itemTouchListenerAdapter =
                new ItemTouchListenerAdapter(mUltimateRecyclerView.mRecyclerView,
                        new ItemTouchListenerAdapter.RecyclerViewOnItemClickListener() {
                            @Override
                            public void onItemClick(RecyclerView parent, View clickedView, int position) {

                                if (position < mVideoData.size()) {

                                    mCallback.onVideoSelected(mVideoData.get(position).get(Utils.KEY_VIDEO_ID));
                                }
                            }

                            @Override
                            public void onItemLongClick(RecyclerView recyclerView, View view, int i) {
                            }
                        });


        mUltimateRecyclerView.mRecyclerView.addOnItemTouchListener(itemTouchListenerAdapter);


        getVideoData();

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


        try {
            mCallback = (OnVideoSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnVideoSelectedListener");
        }
    }



    private void getVideoData() {

        mVideoIds = "";
        final String[] videoId = new String[1];

        String url;
        if(mVideoType == 2) {
            url = Utils.API_YOUTUBE + Utils.FUNCTION_PLAYLIST_ITEMS_YOUTUBE +
                    Utils.PARAM_PART_YOUTUBE + "snippet,id&" +
                    Utils.PARAM_FIELD_PLAYLIST_YOUTUBE + "&" +
                    Utils.PARAM_KEY_YOUTUBE + getResources().getString(R.string.youtube_apikey) + "&" +
                    Utils.PARAM_PLAYLIST_ID_YOUTUBE + mChannelId + "&" +
                    Utils.PARAM_PAGE_TOKEN_YOUTUBE + mNextPageToken + "&" +
                    Utils.PARAM_MAX_RESULT_YOUTUBE + Utils.PARAM_RESULT_PER_PAGE;
        }else {
            url = Utils.API_YOUTUBE + Utils.FUNCTION_SEARCH_YOUTUBE +
                    Utils.PARAM_PART_YOUTUBE + "snippet,id&" + Utils.PARAM_ORDER_YOUTUBE + "&" +
                    Utils.PARAM_TYPE_YOUTUBE + "&" +
                    Utils.PARAM_FIELD_SEARCH_YOUTUBE + "&" +
                    Utils.PARAM_KEY_YOUTUBE + getResources().getString(R.string.youtube_apikey) + "&" +
                    Utils.PARAM_CHANNEL_ID_YOUTUBE + mChannelId + "&" +
                    Utils.PARAM_PAGE_TOKEN_YOUTUBE + mNextPageToken + "&" +
                    Utils.PARAM_MAX_RESULT_YOUTUBE + Utils.PARAM_RESULT_PER_PAGE;
        }

        Log.d(TAGS, url);

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    JSONArray dataItemArray;
                    JSONObject itemIdObject, itemSnippetObject, itemSnippetThumbnailsObject,
                            itemSnippetResourceIdObject;
                    @Override
                    public void onResponse(JSONObject response) {
                        Activity activity = getActivity();
                        if(activity != null && isAdded()){
                            try {
                                dataItemArray = response.getJSONArray(Utils.ARRAY_ITEMS);

                                if (dataItemArray.length() > 0) {
                                    haveResultView();
                                    for (int i = 0; i < dataItemArray.length(); i++) {
                                        HashMap<String, String> dataMap = new HashMap<>();

                                        JSONObject itemsObject = dataItemArray.getJSONObject(i);
                                        itemSnippetObject = itemsObject.
                                                getJSONObject(Utils.OBJECT_ITEMS_SNIPPET);

                                        if(mVideoType == 2){
                                            itemSnippetResourceIdObject = itemSnippetObject.
                                                    getJSONObject(Utils.OBJECT_ITEMS_SNIPPET_RESOURCEID);
                                            dataMap.put(Utils.KEY_VIDEO_ID,
                                                    itemSnippetResourceIdObject.
                                                            getString(Utils.KEY_VIDEO_ID));
                                            videoId[0] = itemSnippetResourceIdObject.
                                                    getString(Utils.KEY_VIDEO_ID);

                                            mVideoIds = mVideoIds + itemSnippetResourceIdObject.
                                                    getString(Utils.KEY_VIDEO_ID) + ",";
                                        }else {
                                            itemIdObject = itemsObject.
                                                    getJSONObject(Utils.OBJECT_ITEMS_ID);
                                            dataMap.put(Utils.KEY_VIDEO_ID,
                                                    itemIdObject.getString(Utils.KEY_VIDEO_ID));
                                            videoId[0] = itemIdObject.getString(Utils.KEY_VIDEO_ID);

                                            mVideoIds = mVideoIds + itemIdObject.
                                                    getString(Utils.KEY_VIDEO_ID) + ",";
                                        }

                                        if(mIsFirstVideo && i == 0) {
                                            mIsFirstVideo = false;
                                            mCallback.onVideoSelected(videoId[0]);
                                        }

                                        dataMap.put(Utils.KEY_TITLE,
                                                itemSnippetObject.getString(Utils.KEY_TITLE));

                                        String formattedPublishedDate = Utils.formatPublishedDate(
                                                getActivity(),
                                                itemSnippetObject.getString(Utils.KEY_PUBLISHEDAT));

                                        dataMap.put(Utils.KEY_PUBLISHEDAT, formattedPublishedDate);

                                        itemSnippetThumbnailsObject = itemSnippetObject.
                                                getJSONObject(Utils.OBJECT_ITEMS_SNIPPET_THUMBNAILS);
                                        itemSnippetThumbnailsObject = itemSnippetThumbnailsObject.
                                                getJSONObject
                                                        (Utils.OBJECT_ITEMS_SNIPPET_THUMBNAILS_MEDIUM);
                                        dataMap.put(Utils.KEY_URL_THUMBNAILS,
                                                itemSnippetThumbnailsObject.getString
                                                        (Utils.KEY_URL_THUMBNAILS));

                                        mTempVideoData.add(dataMap);
                                    }

                                    getDuration();

                                    if (dataItemArray.length() == Utils.PARAM_RESULT_PER_PAGE) {
                                        mNextPageToken = response.getString(Utils.ARRAY_PAGE_TOKEN);

                                    } else {
                                        mNextPageToken = "";
                                        disableLoadmore();
                                    }

                                    mIsAppFirstLaunched = false;

                                } else {
                                    if (mIsAppFirstLaunched &&
                                            mAdapterList.getAdapterItemCount() <= 0) {
                                        noResultView();
                                    }
                                    disableLoadmore();
                                }

                            } catch (JSONException e) {
                                Log.d(Utils.TAG_FANDROID + TAG, "JSON Parsing error: " +
                                        e.getMessage());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                            mPrgLoading.setVisibility(View.GONE);
                        }
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Activity activity = getActivity();
                        if(activity != null && isAdded()){
                            Log.d(Utils.TAG_FANDROID + TAG, "on Error Response: " + error.getMessage());
                            try {
                                String msgSnackBar;
                                if (error instanceof NoConnectionError) {
                                    msgSnackBar = getResources().getString(R.string.no_internet_connection);
                                } else {
                                    msgSnackBar = getResources().getString(R.string.response_error);
                                }

                                if (mVideoData.size() == 0) {
                                    retryView();

                                } else {
                                    mAdapterList.setCustomLoadMoreView(null);
                                    mAdapterList.notifyDataSetChanged();
                                }

                                Utils.showSnackBar(getActivity(), msgSnackBar);
                                mPrgLoading.setVisibility(View.GONE);

                            } catch (Exception e) {
                                Log.d(Utils.TAG_FANDROID + TAG, "failed catch volley " + e.toString());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                        }
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);

    }

    private void getDuration() {
        String url = Utils.API_YOUTUBE+Utils.FUNCTION_VIDEO_YOUTUBE+
                Utils.PARAM_PART_YOUTUBE+"contentDetails&"+
                Utils.PARAM_FIELD_VIDEO_YOUTUBE+"&"+
                Utils.PARAM_KEY_YOUTUBE+getResources().getString(R.string.youtube_apikey)+"&"+
                Utils.PARAM_VIDEO_ID_YOUTUBE+mVideoIds;

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    JSONArray dataItemArrays;
                    JSONObject itemContentObject;
                    @Override
                    public void onResponse(JSONObject response) {
                        Activity activity = getActivity();
                        if(activity != null && isAdded()){
                            try {
                                haveResultView();
                                dataItemArrays = response.getJSONArray(Utils.ARRAY_ITEMS);
                                if (dataItemArrays.length() > 0 && !mTempVideoData.isEmpty()) {
                                    for (int i = 0; i < dataItemArrays.length(); i++) {
                                        HashMap<String, String> dataMap = new HashMap<>();

                                        JSONObject itemsObjects = dataItemArrays.getJSONObject(i);

                                        itemContentObject = itemsObjects.
                                                getJSONObject(Utils.OBJECT_ITEMS_CONTENT_DETAIL);
                                        mDuration         = itemContentObject.
                                                getString(Utils.KEY_DURATION);

                                        String mDurationInTimeFormat = Utils.
                                                getTimeFromString(mDuration);

                                        dataMap.put(Utils.KEY_DURATION, mDurationInTimeFormat);
                                        dataMap.put(Utils.KEY_URL_THUMBNAILS,
                                                mTempVideoData.get(i).get(Utils.KEY_URL_THUMBNAILS));
                                        dataMap.put(Utils.KEY_TITLE,
                                                mTempVideoData.get(i).get(Utils.KEY_TITLE));
                                        dataMap.put(Utils.KEY_VIDEO_ID,
                                                mTempVideoData.get(i).get(Utils.KEY_VIDEO_ID));
                                        dataMap.put(Utils.KEY_PUBLISHEDAT,
                                                mTempVideoData.get(i).get(Utils.KEY_PUBLISHEDAT));

                                        mVideoData.add(dataMap);

                                        mAdapterList.notifyItemInserted(mVideoData.size());

                                    }
                                    mIsStillLoading = true;

                                    mTempVideoData.clear();
                                    mTempVideoData = new ArrayList<>();

                                }else {
                                    if (mIsAppFirstLaunched && mAdapterList.getAdapterItemCount() <= 0)
                                    {
                                        noResultView();
                                    }
                                    disableLoadmore();
                                }

                            } catch (JSONException e) {
                                Log.d(Utils.TAG_FANDROID + TAG,
                                        "JSON Parsing error: " + e.getMessage());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                            mPrgLoading.setVisibility(View.GONE);
                        }
                    }
                },

                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Activity activity = getActivity();
                        if(activity != null && isAdded()){
                            Log.d(Utils.TAG_FANDROID + TAG, "on Error Response: " + error.getMessage());
                            try {
                                String msgSnackBar;
                                if (error instanceof NoConnectionError) {
                                    msgSnackBar = getResources().getString(R.string.no_internet_connection);
                                } else {
                                    msgSnackBar = getResources().getString(R.string.response_error);
                                }

                                if (mVideoData.size() == 0) {
                                    retryView();
                                }

                                Utils.showSnackBar(getActivity(), msgSnackBar);
                                mPrgLoading.setVisibility(View.GONE);

                            } catch (Exception e) {
                                Log.d(Utils.TAG_FANDROID + TAG, "failed catch volley " + e.toString());
                                mPrgLoading.setVisibility(View.GONE);
                            }
                        }
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(Utils.ARG_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MySingleton.getInstance(getActivity()).getRequestQueue().add(request);
    }

    private void retryView() {
        mLytRetry.setVisibility(View.VISIBLE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.GONE);
    }

    private void haveResultView() {
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.VISIBLE);
        mLblNoResult.setVisibility(View.GONE);
    }

    private void noResultView() {
        mLytRetry.setVisibility(View.GONE);
        mUltimateRecyclerView.setVisibility(View.GONE);
        mLblNoResult.setVisibility(View.VISIBLE);

    }

    private void disableLoadmore() {
        mIsStillLoading = false;
        if (mUltimateRecyclerView.isLoadMoreEnabled()) {
            mUltimateRecyclerView.disableLoadmore();
        }
        mAdapterList.notifyDataSetChanged();
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.raisedRetry:
                mPrgLoading.setVisibility(View.VISIBLE);
                haveResultView();
                getVideoData();
                break;
            default:
                break;
        }
    }
}
