package de.achimonline.hueciq;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class SharedPreferences
{
    private static final String SHARED_PREFERENCES_STORE = "HueCIQSharedPrefs";

    private static final String HUE_LAST_CONNECTED_USERNAME = "HueLastConnectedUsername";
    private static final String HUE_LAST_CONNECTED_IP_ADDRESS = "HueLastConnectedIP";
    private static final String HUE_LIGHT_IDS_AND_NAMES = "HueLightIDsAndNames";

    private static final String IQ_DEVICE_IDENTIFIER = "IQDeviceIdentifier";
    private static final String IQ_DEVICE_NAME = "IQDeviceName";

    private static final String USER_REQUESTED_SHUTDOWN = "UserRequestedShutdown";

    private static final Type GSON_MAP_TYPE = new TypeToken<HashMap<String, String>>(){}.getType();

    private static SharedPreferences instance = null;
    private android.content.SharedPreferences sharedPreferences = null;

    private android.content.SharedPreferences.Editor sharedPreferencesEditor = null;

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
        sharedPreferences = appContext.getSharedPreferences(SHARED_PREFERENCES_STORE, 0);
        sharedPreferencesEditor = sharedPreferences.edit();
    }

    public String getHueLastConnectedUsername()
    {
        return sharedPreferences.getString(HUE_LAST_CONNECTED_USERNAME, "");
    }

    public boolean setHueLastConnectedUsername(String username)
    {
        sharedPreferencesEditor.putString(HUE_LAST_CONNECTED_USERNAME, username);

        return sharedPreferencesEditor.commit();
    }

    public String getHueLastConnectedIPAddress()
    {
        return sharedPreferences.getString(HUE_LAST_CONNECTED_IP_ADDRESS, "");
    }

    public boolean setHueLastConnectedIPAddress(String ipAddress)
    {
        sharedPreferencesEditor.putString(HUE_LAST_CONNECTED_IP_ADDRESS, ipAddress);

        return sharedPreferencesEditor.commit();
    }

    public HashMap<String, String> getHueLightIdsAndNames()
    {
        String lightIdsAndNames = sharedPreferences.getString(HUE_LIGHT_IDS_AND_NAMES, "");

        if (lightIdsAndNames != null && !lightIdsAndNames.isEmpty())
        {
            return new Gson().fromJson(lightIdsAndNames, GSON_MAP_TYPE);
        }
        else
        {
            return new HashMap<String, String>();
        }
    }

    public boolean setHueLightIdsAndNames(HashMap<String, String> lightIdsAndNames)
    {
        if (lightIdsAndNames != null && !lightIdsAndNames.isEmpty())
        {
            sharedPreferencesEditor.putString(HUE_LIGHT_IDS_AND_NAMES, new Gson().toJson(lightIdsAndNames, GSON_MAP_TYPE));

            return sharedPreferencesEditor.commit();
        }
        else
        {
            return false;
        }
    }

    public Long getIQDeviceIdentifier()
    {
        return sharedPreferences.getLong(IQ_DEVICE_IDENTIFIER, 0l);
    }

    public boolean setIQDeviceIdentifier(Long deviceIdentifier)
    {
        sharedPreferencesEditor.putLong(IQ_DEVICE_IDENTIFIER, deviceIdentifier);

        return sharedPreferencesEditor.commit();
    }

    public String getIQDeviceName()
    {
        return sharedPreferences.getString(IQ_DEVICE_NAME, "");
    }

    public boolean setIQDeviceName(String deviceName)
    {
        sharedPreferencesEditor.putString(IQ_DEVICE_NAME, deviceName);

        return sharedPreferencesEditor.commit();
    }
}
