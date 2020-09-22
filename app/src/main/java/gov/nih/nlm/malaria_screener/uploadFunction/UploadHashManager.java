/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

Licensed under GNU General Public License v3.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package gov.nih.nlm.malaria_screener.uploadFunction;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/* This class is to provide function that:
    1. create a LinkedHashMap used to keep track of images to be uploaded
    2. save the modified LinkedHashMap to SharedPreference when changes are made to the map
    3. load the LinkedHashMap when app starts

* 03/30/2020 by Hang Yu
*/
public class UploadHashManager {

    private static final String TAG = "MyDebug";

    static final String mapKey = "hashmap_for_upload_list";

    public static Map<String, String> hashmap_for_upload;  //key: image ID, value: folder name

    public static void saveMap(Context context, Map<String, String> inputMap) {

        SharedPreferences pSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pSharedPref != null) {
            JSONObject jsonObject = new JSONObject(inputMap);
            String jsonString = jsonObject.toString();
            SharedPreferences.Editor editor = pSharedPref.edit();
            editor.remove(mapKey).apply();
            editor.putString(mapKey, jsonString);
            editor.commit();
        }
    }

    public static Map<String, String> loadMap(Context context) {

        /*  LinkedHashMap is used to keep items in insertion-order;
            LinkedHashMap is "wrapped" using the Collections.synchronizedMap method to handle
            concurrent modification.
        */
        Map outputMap = Collections.synchronizedMap(new LinkedHashMap<>());
        SharedPreferences pSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            if (pSharedPref != null) {
                String jsonString = pSharedPref.getString(mapKey, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while (keysItr.hasNext()) {
                    String key = keysItr.next();
                    outputMap.put(key, jsonObject.getString(key));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        return outputMap;
    }



}
