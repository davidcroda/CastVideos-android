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

package com.google.sample.cast.refplayer;

import com.google.sample.cast.refplayer.settings.CastPreference;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;

import android.app.Application;

/**
 * The {@link Application} for this demo application.
 */
public class CastApplication extends Application {
    private static String sApplicationId;
    private static VideoCastManager sCastMgr = null;
    public static final double VOLUME_INCREMENT = 0.05;

    /*
     * (non-Javadoc)
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sApplicationId = getString(R.string.app_id);
        initializeCastManager();
        Utils.saveFloatToPreference(getApplicationContext(),
                VideoCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);
    }

    private void initializeCastManager() {
        sCastMgr = VideoCastManager.initialize(getApplicationContext(), sApplicationId, null, null);
        sCastMgr.enableFeatures(
                VideoCastManager.FEATURE_NOTIFICATION |
                        VideoCastManager.FEATURE_LOCKSCREEN |
                        VideoCastManager.FEATURE_WIFI_RECONNECT |
                        VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
                        VideoCastManager.FEATURE_DEBUGGING);
        String destroyOnExitStr = Utils.getStringFromPreference(getApplicationContext(),
                CastPreference.TERMINATION_POLICY_KEY);
        sCastMgr.setStopOnDisconnect(null != destroyOnExitStr
                && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));
    }

    public static VideoCastManager getCastManager() {
        if (sCastMgr == null) {
            throw new IllegalStateException("Application has not been started");
        }
        return sCastMgr;
    }

}
