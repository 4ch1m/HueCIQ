package de.achimonline.hueciq;

import android.content.Context;

public class SharedPreferences
{
    private static final String HUE_SHARED_PREFERENCES_STORE = "HueSharedPrefs";
    private static final String LAST_CONNECTED_USERNAME = "LastConnectedUsername";
    private static final String LAST_CONNECTED_IP = "LastConnectedIP";

    private static SharedPreferences instance = null;
    private android.content.SharedPreferences mSharedPreferences = null;

    private android.content.SharedPreferences.Editor mSharedPreferencesEditor = null;

    public static SharedPreferences getInstance(Context ctx)
    {
        if (instance == null)
        {
            instance = new SharedPreferences(ctx);
        }

        return instance;
    }

    private SharedPreferences(Context appContext)
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
}
