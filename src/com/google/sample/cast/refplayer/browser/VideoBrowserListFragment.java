/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.google.sample.cast.refplayer.browser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.sample.cast.refplayer.R;
import com.google.sample.cast.refplayer.api.ApiRequest;
import com.google.sample.cast.refplayer.mediaplayer.LocalPlayerActivity;
import com.google.sample.castcompanionlibrary.utils.Utils;

import java.util.List;

import eu.erikw.PullToRefreshListView;

public class VideoBrowserListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<MediaInfo>> {

    private static final String TAG = "VideoBrowserList";
    private static final String CATALOG_URL =
            //"http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/" +
    		VideoProvider.BASE_URL + "/api/video";
    private static final int ID_PLAY = 1;
    private static final int ID_DELETE = 2;
    private VideoListAdapter mAdapter;
    private PullToRefreshListView mListView;

    private SharedPreferences prefs;
    private String token;

    public RequestQueue queue;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        View lvOld = viewGroup.findViewById(android.R.id.list);

        queue = Volley.newRequestQueue(getActivity());
        prefs = getActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE);

        token = prefs.getString(VideoProvider.KEY_TOKEN,"");

        mListView = new PullToRefreshListView(getActivity());
        mListView.setId(android.R.id.list);
        mListView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mListView.setDrawSelectorOnTop(false);
        mListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {

            @Override
            public void onRefresh() {
                // Your code to refresh the list contents

                // ...

                // Make sure you call listView.onRefreshComplete()
                // when the loading is done. This can be done from here or any
                // other place, like on a broadcast receive from your loading
                // service or the onPostExecute of your AsyncTask.
                getLoaderManager().restartLoader(0, null, VideoBrowserListFragment.this);
            }
        });

        FrameLayout parent = (FrameLayout) lvOld.getParent();

        parent.removeView(lvOld);
        lvOld.setVisibility(View.GONE);

        parent.addView(mListView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        return viewGroup;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setFastScrollEnabled(true);
        registerForContextMenu(getListView());
        mAdapter = new VideoListAdapter(getActivity());
        setEmptyText(getString(R.string.no_video_found));
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);
    }


    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
     * android.os.Bundle)
     */
    @Override
    public Loader<List<MediaInfo>> onCreateLoader(int arg0, Bundle arg1) {
        return new VideoItemLoader(getActivity(), CATALOG_URL, this.token);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android
     * .support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<List<MediaInfo>> arg0, List<MediaInfo> data) {
        mAdapter.setData(data);
        mListView.onRefreshComplete();
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android
     * .support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<List<MediaInfo>> arg0) {
        mAdapter.setData(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MediaInfo selectedMedia = mAdapter.getItem(position);
        handleNavigation(selectedMedia, false);
    }

    private void handleNavigation(MediaInfo info, boolean autoStart) {
        Intent intent = new Intent(getActivity(), LocalPlayerActivity.class);
        intent.putExtra("media", Utils.fromMediaInfo(info));
        intent.putExtra("shouldStart", autoStart);
        getActivity().startActivity(intent);
    }

    public static VideoBrowserListFragment newInstance() {
        VideoBrowserListFragment f = new VideoBrowserListFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static VideoBrowserListFragment newInstance(Bundle b) {
        VideoBrowserListFragment f = new VideoBrowserListFragment();
        f.setArguments(b);
        return f;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, ID_PLAY, 0, "Play");
        menu.add(0, ID_DELETE, 1, "Delete");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    private class DeleteTask extends AsyncTask<MediaInfo, Integer, Integer> {

        private MediaInfo selectedMedia;
        @Override
        protected Integer doInBackground(MediaInfo... media) {

            selectedMedia = media[0];
            MediaMetadata mm = selectedMedia.getMetadata();

            String id = mm.getString(VideoProvider.KEY_ID);

            String url = VideoProvider.BASE_URL + "/api/video/" + id;

            Log.d(TAG, "Sending DELETE to url: " + url);

            queue.add(new ApiRequest(Request.Method.DELETE, url, token,new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("DELETE", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("DELETE", error.toString());
                }
            }));
            return 200;
        }

        protected void onPostExecute(Integer result) {
            Log.d(TAG, "DELETE RESULT: " + Integer.toString(result));
            if(result == 200) {
                mAdapter.remove(selectedMedia);
            }
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Log.d(TAG, "SELECTED " + Long.toString(info.id));
        MediaInfo selectedMedia = mAdapter.getItem((int)info.id);

        String action = "";

        if(item.getItemId() == ID_PLAY) {
            action = "PLAY";
        } else if(item.getItemId() == ID_DELETE) {
            action = "DELETE";
            new DeleteTask().execute(selectedMedia);
        }

        Log.d(TAG, "Received Action: " + action + " for Video ID: " + selectedMedia.getContentId());

        return true;
    }
}
