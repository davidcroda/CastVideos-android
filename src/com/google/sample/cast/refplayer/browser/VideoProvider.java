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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.common.images.WebImage;
import com.google.sample.cast.refplayer.VideoBrowserActivity;
import com.google.sample.cast.refplayer.api.ApiRequest;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoProvider {

    private static final String TAG = "VideoProvider";
    private static final String TAG_CATEGORIES = "categories";
    private static final String TAG_NAME = "name";
    private static String TAG_MEDIA = "video";
    private static String TAG_SOURCE = "source";
    public static String BASE_URL =
            "https://daveroda.com";
            //"http://192.168.0.103";
    private static String TAG_THUMB = "thumbnailSmall";
    private static String TAG_IMG_780_1200 = "thumbnailLarge";
    private static String TAG_SUBTITLE = "subtitle";
    private static String TAG_TRACKS = "tracks";
    private static String TAG_TRACK_ID = "id";
    private static String TAG_TRACK_TYPE = "type";
    private static String TAG_TRACK_SUBTYPE = "subtype";
    private static String TAG_TRACK_CONTENT_ID = "contentId";
    private static String TAG_TRACK_NAME = "name";
    private static String TAG_TRACK_LANGUAGE = "language";
    private static String TAG_TITLE = "title";

    private static String TAG_ID = "_id";

    private static String TAG_ACODEC = "acodec";
    private static String TAG_VCODEC = "vcodec";
    private static String TAG_DATE = "date";
    private static String TAG_WATCHED = "watched";
    private static String TAG_TRANSCODING = "transcoding";

    public static String KEY_ID = "id";
    public static final String KEY_TOKEN = "token";

    private static List<MediaInfo> mediaList;

    protected JSONObject parseUrl(String urlString, String token) throws JSONException {
        InputStream is = null;
        try {
            java.net.URL url = new java.net.URL(urlString);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("x-token", token);
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
            throw new JSONException("Invalid json, likely not logged in");
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

    public static List<MediaInfo> buildMedia(String url, String token) throws JSONException {

//        if (null != mediaList) {
//            return mediaList; //
//        }
        mediaList = new ArrayList<MediaInfo>();
        JSONObject jsonObj = new VideoProvider().parseUrl(url, token);

        JSONArray videos = jsonObj.getJSONArray(getJsonMediaTag());
        if (null != videos) {
            for (int j = 0; j < videos.length(); j++) {
                JSONObject video = videos.getJSONObject(j);
                //String subTitle = video.getString(TAG_SUBTITLE);
                String imageurl = getThumbPrefix() + video.getString(TAG_THUMB);
                String bigImageurl = getThumbPrefix() + video.getString(TAG_IMG_780_1200);
                String title = video.getString(TAG_TITLE);
                String id = video.getString(TAG_ID);
                String videoUrl = VideoProvider.BASE_URL + "/load/" + id;

                List<MediaTrack> tracks = new ArrayList<MediaTrack>();
                if (video.has(TAG_TRACKS)) {
                    JSONArray tracksArray = video.getJSONArray(TAG_TRACKS);
                    if (tracksArray != null) {
                        for (int k = 0; k < tracksArray.length(); k++) {
                            JSONObject track = tracksArray.getJSONObject(k);
                            tracks.add(buildTrack(track.getLong(TAG_TRACK_ID),
                                    track.getString(TAG_TRACK_TYPE),
                                    track.getString(TAG_TRACK_SUBTYPE),
                                    track.getString(TAG_TRACK_CONTENT_ID),
                                    track.getString(TAG_TRACK_NAME),
                                    track.getString(TAG_TRACK_LANGUAGE)
                            ));
                        }
                    }
                }

                mediaList.add(buildMediaInfo(id, title, "", videoUrl, imageurl,
                        bigImageurl, tracks));
            }
        }
        return mediaList;
    }

    private static MediaInfo buildMediaInfo(String id, String title, String subTitle,
            String url, String imgUrl, String bigImageUrl, List<MediaTrack> tracks) {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(KEY_ID, id);
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, title);

        movieMetadata.addImage(new WebImage(Uri.parse(imgUrl)));
        movieMetadata.addImage(new WebImage(Uri.parse(bigImageUrl)));

        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(getMediaType())
                .setMetadata(movieMetadata)
                .setMediaTracks(tracks)
                .build();
    }

    private static MediaTrack buildTrack(long id, String type, String subType, String contentId,
            String name, String language) {
        int trackType = MediaTrack.TYPE_UNKNOWN;
        if ("text".equals(type)) {
            trackType = MediaTrack.TYPE_TEXT;
        } else if ("video".equals(type)) {
            trackType = MediaTrack.TYPE_VIDEO;
        } else if ("audio".equals(type)) {
            trackType = MediaTrack.TYPE_AUDIO;
        }

        int trackSubType = MediaTrack.SUBTYPE_NONE;
        if (subType != null) {
            if ("captions".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_CAPTIONS;
            } else if ("subtitle".equals(type)) {
                trackSubType = MediaTrack.SUBTYPE_SUBTITLES;
            }
        }

        return new MediaTrack.Builder(id, trackType)
                .setName(name)
                .setSubtype(trackSubType)
                .setContentId(contentId)
                .setLanguage(language).build();
    }

    private static String getMediaType() {
        return "video/mp4";
    }

    private static String getJsonMediaTag() {
        return TAG_MEDIA;
    }

    private static String getThumbPrefix() {
        return BASE_URL;
    }

}
