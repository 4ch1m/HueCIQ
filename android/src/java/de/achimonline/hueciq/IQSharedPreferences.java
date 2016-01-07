package de.achimonline.hueciq;

import android.content.Context;
import android.content.SharedPreferences;

public class IQSharedPreferences
{
    private static final String IQ_SHARED_PREFERENCES_STORE = "IQSharedPrefs";

    private static final String DEVICE_IDENTIFIER = "DeviceIdentifier";
    private static final String DEVICE_NAME = "DeviceName";

    private static IQSharedPreferences instance = null;

    private SharedPreferences sharedPreferences = null;
    private SharedPreferences.Editor sharedPreferencesEditor = null;

    public static IQSharedPreferences getInstance(Context ctx)
    {
        if (instance == null)
        {
            instance = new IQSharedPreferences(ctx);
        }

        return instance;
    }

    private IQSharedPreferences(Context appContext)
    {
        sharedPreferences = appContext.getSharedPreferences(IQ_SHARED_PREFERENCES_STORE, 0);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    public Long getDeviceIdentifier()
    {
        return sharedPreferences.getLong(DEVICE_IDENTIFIER, 0l);
    }

    public boolean setDeviceIdentifier(Long deviceIdentifier)
    {
        sharedPreferencesEditor.putLong(DEVICE_IDENTIFIER, deviceIdentifier);

        return sharedPreferencesEditor.commit();
    }

    public String getDeviceName()
    {
        return sharedPreferences.getString(DEVICE_NAME, "");
    }

    public boolean setDeviceName(String deviceName)
    {
        sharedPreferencesEditor.putString(DEVICE_NAME, deviceName);

        return sharedPreferencesEditor.commit();
    }
}
