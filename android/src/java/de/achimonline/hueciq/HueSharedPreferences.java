package de.achimonline.hueciq;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class HueSharedPreferences
{
    private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";

    private static final String LAST_CONNECTED_USERNAME = "LastConnectedUsername";
    private static final String LAST_CONNECTED_IP = "LastConnectedIP";
    private static final String LIGHT_IDS_AND_NAMES = "LightIDsAndNames";

    private static final Type GSON_MAP_TYPE = new TypeToken<HashMap<String, String>>(){}.getType();

    private static HueSharedPreferences instance = null;
    private android.content.SharedPreferences mSharedPreferences = null;

    private android.content.SharedPreferences.Editor mSharedPreferencesEditor = null;

    public static HueSharedPreferences getInstance(Context ctx)
    {
        if (instance == null)
        {
            instance = new HueSharedPreferences(ctx);
        }

        return instance;
    }

    private HueSharedPreferences(Context appContext)
    {
        mSharedPreferences = appContext.getSharedPreferences(HUE_SHARED_PREFERENCES_STORE, 0);
        mSharedPreferencesEditor = mSharedPreferences.edit();
    }

    public String getUsername()
    {
        return mSharedPreferences.getString(LAST_CONNECTED_USERNAME, "");
    }

    public boolean setUsername(String username)
    {
        mSharedPreferencesEditor.putString(LAST_CONNECTED_USERNAME, username);

        return mSharedPreferencesEditor.commit();
    }

    public String getLastConnectedIPAddress()
    {
        return mSharedPreferences.getString(LAST_CONNECTED_IP, "");
    }

    public boolean setLastConnectedIPAddress(String ipAddress)
    {
        mSharedPreferencesEditor.putString(LAST_CONNECTED_IP, ipAddress);

        return mSharedPreferencesEditor.commit();
    }

    public HashMap<String, String> getLightIdsAndNames()
    {
        String lightIdsAndNames = mSharedPreferences.getString(LIGHT_IDS_AND_NAMES, "");

        if (lightIdsAndNames != null && !lightIdsAndNames.isEmpty())
        {
            return new Gson().fromJson(lightIdsAndNames, GSON_MAP_TYPE);
        }
        else
        {
            return new HashMap<String, String>();
        }
    }

    public boolean setLightIdsAndNames(HashMap<String, String> lightIdsAndNames)
    {
        if (lightIdsAndNames != null && !lightIdsAndNames.isEmpty())
        {
            mSharedPreferencesEditor.putString(LIGHT_IDS_AND_NAMES, new Gson().toJson(lightIdsAndNames, GSON_MAP_TYPE));

            return mSharedPreferencesEditor.commit();
        }
        else
        {
            return false;
        }
    }
}
