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
    private static final String HUE_GROUP_IDS_AND_NAMES = "HueGroupIDsAndNames";

    private static final String IQ_DEVICE_IDENTIFIER = "IQDeviceIdentifier";
    private static final String IQ_DEVICE_NAME = "IQDeviceName";

    private static final String ACTION_LOG_HISTORY = "LogHistory";

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
        return getStringMap(HUE_LIGHT_IDS_AND_NAMES);
    }

    public boolean setHueLightIdsAndNames(HashMap<String, String> lightIdsAndNames)
    {
        return setStringMap(HUE_LIGHT_IDS_AND_NAMES, lightIdsAndNames);
    }

    public HashMap<String, String> getHueGroupIdsAndNames()
    {
        return getStringMap(HUE_GROUP_IDS_AND_NAMES);
    }

    public boolean setHueGroupIdsAndNames(HashMap<String, String> groupIdsAndNames)
    {
        return setStringMap(HUE_GROUP_IDS_AND_NAMES, groupIdsAndNames);
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

    public String[] getActionLogHistory()
    {
        return new Gson().fromJson(sharedPreferences.getString(ACTION_LOG_HISTORY, ""), String[].class);
    }

    public boolean setActionLogHistory(String[] logHistory)
    {
        sharedPreferencesEditor.putString(ACTION_LOG_HISTORY, new Gson().toJson(logHistory));

        return sharedPreferencesEditor.commit();
    }

    private HashMap<String, String> getStringMap(String preferencesKey)
    {
        String jsonString = sharedPreferences.getString(preferencesKey, "");

        if (jsonString != null && !jsonString.isEmpty())
        {
            return new Gson().fromJson(jsonString, GSON_MAP_TYPE);
        }
        else
        {
            return new HashMap<String, String>();
        }
    }

    private boolean setStringMap(String preferencesKey, HashMap<String, String> stringMap)
    {
        if (stringMap != null && !stringMap.isEmpty())
        {
            sharedPreferencesEditor.putString(preferencesKey, new Gson().toJson(stringMap, GSON_MAP_TYPE));

            return sharedPreferencesEditor.commit();
        }
        else
        {
            sharedPreferencesEditor.remove(preferencesKey);

            return true;
        }
    }
}
