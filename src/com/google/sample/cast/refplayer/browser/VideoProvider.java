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

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class VideoProvider {

    private static final String TAG = "VideoProvider";
    private static String TAG_MEDIA = "video";
    private static String TAG_SOURCES = "sources";
    private static String TAG_THUMB = "thumbnailSmall";
    private static String TAG_IMG_780_1200 = "thumbnailLarge";
    private static String TAG_TITLE = "title";
    private static String TAG_ID = "_id";
    private static String TAG_ACODEC = "acodec";
    private static String TAG_VCODEC = "vcodec";
    private static String TAG_DATE = "date";
    private static String TAG_WATCHED = "watched";
    private static String TAG_TRANSCODING = "transcoding";


    public static String KEY_ID = "_id";
    private static String TOKEN = "jdQO12KpRsqN2@^L";

    private static List<MediaInfo> mediaList;

    protected JSONObject parseUrl(String urlString) {
        InputStream is = null;
        try {
            java.net.URL url = new java.net.URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("x-token", TOKEN);
            is = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String json = sb.toString();
            if (sb.length() > 4000) {
                Log.v(TAG, "sb.length = " + sb.length());
                int chunkCount = sb.length() / 4000;     // integer division
                for (int i = 0; i <= chunkCount; i++) {
                    int max = 4000 * (i + 1);
                    if (max >= sb.length()) {
                        Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i));
                    } else {
                        Log.v(TAG, "chunk " + i + " of " + chunkCount + ":" + sb.substring(4000 * i, max));
                    }
                }
            }
            return new JSONObject(json);
        } catch (Exception e) {
            Log.d(TAG, "Failed to parse the json for media list");
            Log.d(TAG, e.getMessage());
            return null;
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    // 
                }
            }
        }
    }

    public static List<MediaInfo> buildMedia(String url) throws JSONException {

//        if (null != mediaList) {
//            return mediaList; //
//        }
        mediaList = new ArrayList<MediaInfo>();
        JSONObject jsonObj = new VideoProvider().parseUrl(url);
        Log.d(TAG, "Received JSON: ");
        Log.d(TAG, jsonObj.toString(2));
        JSONArray videos = jsonObj.getJSONArray(TAG_MEDIA);
        if (null != videos) {
            for (int j = 0; j < videos.length(); j++) {
                try {
                    JSONObject video = videos.getJSONObject(j);
                    JSONArray videoUrls = video.getJSONArray(TAG_SOURCES);
                    if (null == videoUrls || videoUrls.length() == 0) {
                        continue;
                    }
                    String videoUrl = videoUrls.getString(0);
                    String videoId = video.getString(TAG_ID);
                    String imageurl = video.getString(TAG_THUMB);
                    String bigImageurl = video.getString(TAG_IMG_780_1200);
                    String title = video.getString(TAG_TITLE);

                    String subTitle = "";
                    String studio = String.format("V: %s, A: %s, W: %s", video.getString(TAG_VCODEC), video.getString(TAG_ACODEC), video.getBoolean(TAG_WATCHED));
                    mediaList.add(buildMediaInfo(videoId, title, studio, subTitle, videoUrl, imageurl,
                            bigImageurl));
                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }
        return mediaList;
    }

    private static MediaInfo buildMediaInfo(String id, String title,
                                            String subTitle, String studio, String url, String imgUrl, String bigImageUrl) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(KEY_ID, id);
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);
        movieMetadata.putString(MediaMetadata.KEY_STUDIO, studio);
        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(getMediaType())
                .setMetadata(movieMetadata)
                .build();
    }

    private static String getMediaType() {
        return "video/mp4";
    }

}
