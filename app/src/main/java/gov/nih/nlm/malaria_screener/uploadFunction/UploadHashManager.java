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
