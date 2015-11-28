package de.achimonline.hueciq;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHLightState;

import java.util.List;
import java.util.Map;

public class ListenerService extends Service implements ConnectIQ.ConnectIQListener, ConnectIQ.IQDeviceEventListener, ConnectIQ.IQApplicationEventListener
{
    private static final String LOG_PREFIX = ListenerService.class.getSimpleName() + " - ";

    public static final String EXTRA_IQDEVICE = "IQDevice";

    public static final String SERVICE_NAME = "HueCIQService";

    public static final String BROADCAST_ACTION = ListenerService.class.getPackage().getName() + ".BROADCAST.ACTION";
    public static final String BROADCAST_DATA = ListenerService.class.getPackage().getName() + ".BROADCAST.DATA";

    private PHHueSDK phHueSDK;

    private ConnectIQ connectIQ;
    private IQDevice iqDevice;
    private IQApp iqApp;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Starting service ...");
        }

        phHueSDK = PHHueSDK.create();
        connectIQ = ConnectIQ.getInstance();

        try
        {
            connectIQ.unregisterAllForEvents();
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to unregister all events on CIQ-SDK.");
            }
        }

        retrieveAndInitializeConnectIQSDK();
        registerForEvents();

        iqDevice = (IQDevice) intent.getParcelableExtra(EXTRA_IQDEVICE);
        iqApp = new IQApp(getString(R.string.ciq_app_id));

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void registerForEvents()
    {
        registerForDeviceEvents();
        registerForAppEvents();
    }

    private void registerForDeviceEvents()
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Registering for CIQ-device-events (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ")...");
        }

        try
        {
            connectIQ.registerForDeviceEvents(iqDevice, this);
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to register for device-events!\n”" + e.getMessage());
            }
        }
    }

    private void registerForAppEvents()
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Registering for CIQ-app-events (id=" + getString(R.string.ciq_app_id) + ")...");
        }

        try
        {
            connectIQ.registerForAppEvents(iqDevice, iqApp, this);
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to register for app-events!\n”" + e.getMessage());
            }
        }
    }

    private void retrieveAndInitializeConnectIQSDK()
    {
        connectIQ = ConnectIQ.getInstance(this, ConnectIQ.IQConnectType.WIRELESS);
        connectIQ.initialize(this, false, this);
    }

    @Override
    public void onDestroy()
    {
        if (connectIQ != null)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Unregistering all CIQ-SDK events.");
            }

            try
            {
                connectIQ.unregisterAllForEvents();
            }
            catch (Exception e)
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to unregister from all CIQ-SDK events.");
                }
            }
        }

        super.onDestroy();
    }

    @Override
    public void onInitializeError(ConnectIQ.IQSdkErrorStatus errStatus)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Error while trying to initialize CIQ-SDK.");
        }
    }

    @Override
    public void onSdkReady()
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-SDK ready.");
        }

        registerForEvents();
    }

    @Override
    public void onSdkShutDown()
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Shutting down CIQ-SDK.");
        }
    }

    @Override
    public void onDeviceStatusChanged(IQDevice iqDevice, IQDevice.IQDeviceStatus iqDeviceStatus)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-device status changed. (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ", status=" + iqDeviceStatus != null ? iqDeviceStatus.name() : "<unknown>" + ")");
        }

        broadcastMessage(iqDeviceStatus != null ? "[ " + iqDeviceStatus.name() + " ]" : "[ Device status changed. ]");

        if (iqDeviceStatus == IQDevice.IQDeviceStatus.NOT_CONNECTED || iqDeviceStatus == IQDevice.IQDeviceStatus.UNKNOWN)
        {
            retrieveAndInitializeConnectIQSDK();
        }
    }

    @Override
    public void onMessageReceived(IQDevice device, IQApp app, List<Object> message, ConnectIQ.IQMessageStatus status)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-app-message received! device=" + (device != null ? device.getFriendlyName() : "<unknown>") + ", message=" + message + ", status=" + status.name());
        }

        for (Object messageItem : message)
        {
            if (messageItem instanceof String)
            {
                broadcastMessage((String) messageItem);

                try
                {
                    processCIQMessage((String) messageItem);
                }
                catch (Exception e)
                {
                    if (Constants.LOG_ACTIVE)
                    {
                        Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to process CIQ-message.\n”" + e.getMessage());
                    }
                }
            }
        }
    }

    private void processCIQMessage(String message) throws Exception
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Processing CIQ-message: " + message);
        }

        if (message.startsWith(getString(R.string.ciq_switch_command_prefix)))
        {
            switchLight(message.endsWith(getString(R.string.ciq_switch_command_on)));
        }

        if (message.startsWith(getString(R.string.ciq_brightness_command_prefix)))
        {
            changeBrightness(Integer.parseInt(message.substring(message.lastIndexOf("_") + 1)));
        }

        if (message.startsWith(getString(R.string.ciq_color_command_prefix)))
        {
            float[] hueColor;
            int[] rgbColor;

            if (message.endsWith(getString(R.string.ciq_color_command_blue)))
            {
                rgbColor = getResources().getIntArray(R.array.blue);
                hueColor = PHUtilities.calculateXYFromRGB(rgbColor[0], rgbColor[1], rgbColor[2], null);
            }
            else if (message.endsWith(getString(R.string.ciq_color_command_green)))
            {
                rgbColor = getResources().getIntArray(R.array.green);
                hueColor = PHUtilities.calculateXYFromRGB(rgbColor[0], rgbColor[1], rgbColor[2], null);
            }
            else if (message.endsWith(getString(R.string.ciq_color_command_yellow)))
            {
                rgbColor = getResources().getIntArray(R.array.yellow);
                hueColor = PHUtilities.calculateXYFromRGB(rgbColor[0], rgbColor[1], rgbColor[2], null);
            }
            else if (message.endsWith(getString(R.string.ciq_color_command_orange)))
            {
                rgbColor = getResources().getIntArray(R.array.orange);
                hueColor = PHUtilities.calculateXYFromRGB(rgbColor[0], rgbColor[1], rgbColor[2], null);
            }
            else if (message.endsWith(getString(R.string.ciq_color_command_purple)))
            {
                rgbColor = getResources().getIntArray(R.array.purple);
                hueColor = PHUtilities.calculateXYFromRGB(rgbColor[0], rgbColor[1], rgbColor[2], null);
            }
            else
            {
                rgbColor = getResources().getIntArray(R.array.red);
                hueColor = PHUtilities.calculateXYFromRGB(rgbColor[0], rgbColor[1], rgbColor[2], null);
            }

            changeColor(hueColor);
        }
    }

    private void broadcastMessage(String message)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Broadcasting message: " + message);
        }

        final Intent intent = new Intent(BROADCAST_ACTION).putExtra(BROADCAST_DATA, message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void switchLight(boolean on)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Hue-SDK - switching light " + (on ? "on" : "off"));
        }

        final PHLightState lightState = new PHLightState();
        lightState.setOn(on);

        for (PHLight phLight : phHueSDK.getSelectedBridge().getResourceCache().getAllLights())
        {
            phHueSDK.getSelectedBridge().updateLightState(phLight, lightState, new LightListener("switchLight"));
        }
    }

    private void changeBrightness(int value)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Hue-SDK - changing brightness to '" + value + "'");
        }

        final PHLightState lightState = new PHLightState();
        lightState.setBrightness(value);

        for (PHLight phLight : phHueSDK.getSelectedBridge().getResourceCache().getAllLights())
        {
            phHueSDK.getSelectedBridge().updateLightState(phLight, lightState, new LightListener("changeBrightness"));
        }
    }

    private void changeColor(float[] hueColor)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Hue-SDK - changing color; x=" + hueColor[0] + ", y=" + hueColor[1]);
        }

        final PHLightState lightState = new PHLightState();
        lightState.setX(hueColor[0]);
        lightState.setY(hueColor[1]);

        for (PHLight phLight : phHueSDK.getSelectedBridge().getResourceCache().getAllLights())
        {
            phHueSDK.getSelectedBridge().updateLightState(phLight, lightState, new LightListener("changeColor"));
        }
    }

    private class LightListener implements PHLightListener
    {
        private String caller;

        public LightListener(String caller)
        {
            this.caller = caller;
        }

        @Override
        public void onReceivingLightDetails(PHLight phLight)
        {
        }

        @Override
        public void onReceivingLights(List<PHBridgeResource> list)
        {
        }

        @Override
        public void onSearchComplete()
        {
        }

        @Override
        public void onSuccess()
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Hue-SDK - success (" + caller + ")");
            }
        }

        @Override
        public void onError(int i, String s)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Hue-SDK - error (caller=" + caller + ", code=" + i + ", message=" + s + ")");
            }
        }

        @Override
        public void onStateUpdate(Map<String, String> map, List<PHHueError> list)
        {
        }
    }
}
