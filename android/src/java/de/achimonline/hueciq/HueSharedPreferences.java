package de.achimonline.hueciq;

import android.content.Context;

import java.util.Collections;
import java.util.Set;

public class HueSharedPreferences
{
    private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";

    private static final String LAST_CONNECTED_USERNAME = "LastConnectedUsername";
    private static final String LAST_CONNECTED_IP = "LastConnectedIP";
    private static final String LIGHT_IDS = "LightIDs";

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

    public Set<String> getLightIds()
    {
        return mSharedPreferences.getStringSet(LIGHT_IDS, Collections.<String>emptySet());
    }

    public boolean setLightIds(Set<String> lightIds)
    {
        mSharedPreferencesEditor.putStringSet(LIGHT_IDS, lightIds);

        return mSharedPreferencesEditor.commit();
    }
}
