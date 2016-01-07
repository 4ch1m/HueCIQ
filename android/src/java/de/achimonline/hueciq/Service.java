package de.achimonline.hueciq;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Service extends android.app.Service implements ConnectIQ.ConnectIQListener, ConnectIQ.IQDeviceEventListener, ConnectIQ.IQApplicationEventListener
{
    private static final String LOG_PREFIX = Service.class.getSimpleName() + " - ";

    public static final String EXTRA_IQDEVICE_IDENTIFIER = "IQDeviceIdentifier";
    public static final String EXTRA_IQDEVICE_NAME = "IQDeviceName";
    public static final String EXTRA_PHHUE_IP_ADDRESS = "PHHueIPAddress";
    public static final String EXTRA_PHHUE_USER_NAME = "PHHueUserName";
    public static final String EXTRA_PHHUE_LIGHT_IDS_AND_NAMES = "PHHueLightIDsAndNames";

    private IQSharedPreferences iqSharedPreferences = IQSharedPreferences.getInstance(getApplicationContext());

    private ConnectIQ connectIQ;
    private HueSimpleAPIClient hueSimpleAPIClient;

    private IQDevice iqDevice;
    private IQApp iqApp;

    private long iqDeviceIdentifier;
    private String iqDeviceName;

    private String hueIpAddress;
    private String hueUserName;
    private HashMap<String, String> hueLightIdsAndNames;

    private String serviceAction = new String();

    private List<ServiceListener> serviceListeners = new ArrayList<ServiceListener>();

    private ServiceAPI serviceAPI = new ServiceAPI.Stub()
    {
        @Override
        public String getLatestAction() throws RemoteException
        {
            synchronized (serviceAction)
            {
                return serviceAction;
            }
        }

        @Override
        public void addListener(ServiceListener listener) throws RemoteException
        {
            synchronized (serviceListeners)
            {
                serviceListeners.add(listener);
            }
        }

        @Override
        public void removeListener(ServiceListener listener) throws RemoteException
        {
            synchronized (serviceListeners)
            {
                serviceListeners.remove(listener);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        if (Service.class.getName().equals(intent.getAction()))
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Bound by intent " + intent);
            }

            return serviceAPI.asBinder();
        }
        else
        {
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Starting service ...");
        }

        if (intent.getExtras() == null || intent.getExtras().size() == 0)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "No extras in service-intent.");
            }

            iqDeviceIdentifier = iqSharedPreferences.getDeviceIdentifier();
            iqDeviceName = iqSharedPreferences.getDeviceName();

            final HueSharedPreferences hueSharedPreferences = HueSharedPreferences.getInstance(getApplicationContext());

            hueIpAddress = hueSharedPreferences.getLastConnectedIPAddress();
            hueUserName = hueSharedPreferences.getUsername();
            hueLightIdsAndNames = hueSharedPreferences.getLightIdsAndNames();
        }
        else
        {
            iqDeviceIdentifier = intent.getLongExtra(EXTRA_IQDEVICE_IDENTIFIER, 0l);
            iqDeviceName = intent.getStringExtra(EXTRA_IQDEVICE_NAME);

            hueIpAddress = intent.getStringExtra(EXTRA_PHHUE_IP_ADDRESS);
            hueUserName = intent.getStringExtra(EXTRA_PHHUE_USER_NAME);
            hueLightIdsAndNames = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_PHHUE_LIGHT_IDS_AND_NAMES);
        }

        hueSimpleAPIClient = new HueSimpleAPIClient(hueIpAddress, hueUserName);

        retrieveAndInitializeConnectIQSDK();

        propagateAction(getString(R.string.action_log_background_service_started));

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        propagateAction(getString(R.string.action_log_background_service_stopped));

        try
        {
            connectIQ.unregisterAllForEvents();
            connectIQ.shutdown(this);
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.wtf(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to shutdown the CIQ-SDK.");
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

        registerForConnectIQEvents();
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

        propagateAction(iqDevice != null && iqDeviceStatus != null ? iqDevice.getFriendlyName() + " " + iqDeviceStatus.name() : getString(R.string.action_log_device_status_changed));

        if (iqDeviceStatus == IQDevice.IQDeviceStatus.NOT_CONNECTED || iqDeviceStatus == IQDevice.IQDeviceStatus.UNKNOWN)
        {
            retrieveAndInitializeConnectIQSDK();
        }
        else
        {
            try
            {
                final String message = buildLightIdsAndNamesMessage();

                connectIQ.sendMessage(iqDevice, iqApp, message, new ConnectIQ.IQSendMessageListener()
                {
                    @Override
                    public void onMessageStatus(IQDevice iqDevice, IQApp iqApp, ConnectIQ.IQMessageStatus iqMessageStatus)
                    {
                        if (ConnectIQ.IQMessageStatus.SUCCESS == iqMessageStatus)
                        {
                            propagateAction(getString(R.string.action_log_light_infos_send_success));
                        }
                        else
                        {
                            if (Constants.LOG_ACTIVE)
                            {
                                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Failure while trying to send message to CIQ-device. (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ", messageStatus=" + iqMessageStatus != null ? iqMessageStatus.name() : "<unknown>" + ")");
                            }

                            propagateAction(getString(R.string.action_log_light_infos_send_failure));
                        }
                    }
                });
            }
            catch (Exception e)
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Failed to send message with light-IDs and light-names! (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ", status=" + iqDeviceStatus != null ? iqDeviceStatus.name() : "<unknown>" + ")");
                }
            }
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
                propagateAction(getString(R.string.action_log_received_command) + " [ " + (String) messageItem + " ]");

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

    private void propagateAction(String action)
    {
        synchronized (serviceAction)
        {
            serviceAction = action;

            synchronized (serviceListeners)
            {
                for (ServiceListener serviceListener : serviceListeners)
                {
                    try
                    {
                        serviceListener.handleAction();
                    }
                    catch (RemoteException e)
                    {
                        Log.w(getString(R.string.app_log_tag), LOG_PREFIX + "Failed to notify service-listener! " + serviceListener, e);
                    }
                }
            }
        }
    }

    private void registerForConnectIQEvents()
    {
        registerForDeviceEvents();
        registerForAppEvents();
    }

    private void registerForDeviceEvents()
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Registering for CIQ-device-events (device=" + iqDevice.getFriendlyName() + ")...");
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

        iqDevice = getIQDeviceFromSDK(iqDeviceIdentifier, iqDeviceName);
        iqApp = new IQApp(getString(R.string.ciq_app_id));
    }

    private IQDevice getIQDeviceFromSDK(long iqDeviceIdentifier, String name)
    {
        try
        {
            for (IQDevice iqDevice : connectIQ.getKnownDevices())
            {
                if (iqDevice.getDeviceIdentifier() == iqDeviceIdentifier)
                {
                    return iqDevice;
                }
            }
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.w(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to get device-object from CIQ-SDK.");
            }
        }

        return new IQDevice(iqDeviceIdentifier, name);
    }

    private String buildLightIdsAndNamesMessage()
    {
        final StringBuilder message = new StringBuilder("");

        for (String lightId : hueLightIdsAndNames.keySet())
        {
            if (!message.toString().isEmpty())
            {
                message.append(getString(R.string.ciq_mail_item_separator));
            }

            message.append(lightId);
            message.append(getString(R.string.ciq_mail_item_light_separator));
            message.append(hueLightIdsAndNames.get(lightId));
        }

        return message.toString();
    }

    private void processCIQMessage(final String message) throws Exception
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Processing CIQ-message: " + message);
        }

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                if (message.startsWith(getString(R.string.ciq_switch_command_prefix)))
                {
                    switchStatus(message);
                }

                if (message.startsWith(getString(R.string.ciq_brightness_command_prefix)))
                {
                    changeBrightness(message);
                }

                if (message.startsWith(getString(R.string.ciq_color_command_prefix)))
                {
                    changeColor(message);
                }
            }
        }).start();
    }

    private void switchStatus(String message)
    {
        final HueCIQCommand hueCIQCommand = new HueCIQCommand(message);

        final boolean status = hueCIQCommand.getCommand().endsWith(getString(R.string.ciq_switch_command_on));

        if (hueCIQCommand.isAllLights())
        {
            for (String lightId : hueLightIdsAndNames.keySet())
            {
                hueSimpleAPIClient.setOn(lightId, status);
            }
        }
        else
        {
            hueSimpleAPIClient.setOn(hueCIQCommand.getLightId(), status);
        }
    }

    private void changeBrightness(String message)
    {
        final HueCIQCommand hueCIQCommand = new HueCIQCommand(message);

        final int brightness = Integer.parseInt(hueCIQCommand.getCommand().substring(message.lastIndexOf(getString(R.string.ciq_command_token_separator)) + 1));

        if (hueCIQCommand.isAllLights())
        {
            for (String lightId : hueLightIdsAndNames.keySet())
            {
                hueSimpleAPIClient.setBri(lightId, (int)(brightness * 2.5));
            }
        }
        else
        {
            hueSimpleAPIClient.setBri(hueCIQCommand.getLightId(), (int)(brightness * 2.5));
        }
    }

    private void changeColor(String message)
    {
        final HueCIQCommand hueCIQCommand = new HueCIQCommand(message);

        int[] rgbColor;

        if (hueCIQCommand.getCommand().endsWith(getString(R.string.ciq_color_command_blue)))
        {
            rgbColor = getResources().getIntArray(R.array.blue);
        }
        else if (hueCIQCommand.getCommand().endsWith(getString(R.string.ciq_color_command_green)))
        {
            rgbColor = getResources().getIntArray(R.array.green);
        }
        else if (hueCIQCommand.getCommand().endsWith(getString(R.string.ciq_color_command_yellow)))
        {
            rgbColor = getResources().getIntArray(R.array.yellow);
        }
        else if (hueCIQCommand.getCommand().endsWith(getString(R.string.ciq_color_command_orange)))
        {
            rgbColor = getResources().getIntArray(R.array.orange);
        }
        else if (hueCIQCommand.getCommand().endsWith(getString(R.string.ciq_color_command_purple)))
        {
            rgbColor = getResources().getIntArray(R.array.purple);
        }
        else
        {
            rgbColor = getResources().getIntArray(R.array.red);
        }

        final float[] xyColor = PHUtilities.calculateXYFromRGB(rgbColor[0], rgbColor[1], rgbColor[2], "");

        if (hueCIQCommand.isAllLights())
        {
            for (String lightId : hueLightIdsAndNames.keySet())
            {
                hueSimpleAPIClient.setXY(lightId, xyColor);
            }
        }
        else
        {
            hueSimpleAPIClient.setXY(hueCIQCommand.getLightId(), xyColor);
        }
    }

    private class HueCIQCommand
    {
        private String command;
        private String lightId;
        private boolean allLights;

        public HueCIQCommand(String message)
        {
            if (message != null && !message.isEmpty())
            {
                final String[] splittedMessage = message.split(getString(R.string.ciq_command_light_id_separator));

                if (splittedMessage.length == 2)
                {
                    this.command = splittedMessage[0];
                    this.lightId = splittedMessage[1];
                    this.allLights = getString(R.string.ciq_command_light_id_all).equals(splittedMessage[1]);
                }
            }
        }

        public String getCommand()
        {
            return command;
        }

        public String getLightId()
        {
            return lightId;
        }

        public boolean isAllLights()
        {
            return allLights;
        }
    }
}
