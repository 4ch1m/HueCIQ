package de.achimonline.hueciq;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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

    public static final int SERVICE_NOTIFICATION_ID = 1;

    public static final String EXTRA_IQDEVICE_IDENTIFIER = "IQDeviceIdentifier";
    public static final String EXTRA_IQDEVICE_NAME = "IQDeviceName";
    public static final String EXTRA_PHHUE_IP_ADDRESS = "PHHueIPAddress";
    public static final String EXTRA_PHHUE_USER_NAME = "PHHueUserName";
    public static final String EXTRA_PHHUE_LIGHT_IDS_AND_NAMES = "PHHueLightIDsAndNames";
    public static final String EXTRA_PHHUE_GROUP_IDS_AND_NAMES = "PHHueGroupIDsAndNames";

    private ConnectIQ connectIQ;
    private HueSimpleAPIClient hueSimpleAPIClient;

    private IQDevice iqDevice;
    private IQApp iqApp;

    private long iqDeviceIdentifier;
    private String iqDeviceName;

    private HashMap<String, String> hueLightIdsAndNames;
    private HashMap<String, String> hueGroupIdsAndNames;

    private String serviceAction = "";

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

    public static boolean isRunning(Activity activity)
    {
        final ActivityManager activityManager = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : activityManager.getRunningServices(Integer.MAX_VALUE))
        {
            if (Service.class.getName().equals(runningServiceInfo.service.getClassName()))
            {
                return true;
            }
        }

        return false;
    }

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

        SharedPreferences sharedPreferences = SharedPreferences.getInstance(getApplicationContext());

        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Starting service ...");
        }

        String hueIpAddress;
        String hueUserName;

        if (intent.getExtras() == null || intent.getExtras().size() == 0)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "No extras in service-intent.");
            }

            iqDeviceIdentifier = sharedPreferences.getIQDeviceIdentifier();
            iqDeviceName = sharedPreferences.getIQDeviceName();

            hueIpAddress = sharedPreferences.getHueLastConnectedIPAddress();
            hueUserName = sharedPreferences.getHueLastConnectedUsername();
            hueLightIdsAndNames = sharedPreferences.getHueLightIdsAndNames();
            hueGroupIdsAndNames = sharedPreferences.getHueGroupIdsAndNames();
        }
        else
        {
            iqDeviceIdentifier = intent.getLongExtra(EXTRA_IQDEVICE_IDENTIFIER, 0l);
            iqDeviceName = intent.getStringExtra(EXTRA_IQDEVICE_NAME);

            hueIpAddress = intent.getStringExtra(EXTRA_PHHUE_IP_ADDRESS);
            hueUserName = intent.getStringExtra(EXTRA_PHHUE_USER_NAME);
            hueLightIdsAndNames = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_PHHUE_LIGHT_IDS_AND_NAMES);
            hueGroupIdsAndNames = (HashMap<String, String>) intent.getSerializableExtra(EXTRA_PHHUE_GROUP_IDS_AND_NAMES);
        }

        hueSimpleAPIClient = new HueSimpleAPIClient(hueIpAddress, hueUserName);

        retrieveAndInitializeConnectIQSDK();

        propagateAction(getString(R.string.action_log_background_service_started));

        startForeground(SERVICE_NOTIFICATION_ID, createNotification());

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        stopForeground(true);

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
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-device status changed. (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ", status=" + (iqDeviceStatus != null ? iqDeviceStatus.name() : "<unknown>") + ")");
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
                final String message = buildIdsAndNamesMessage();

                connectIQ.sendMessage(iqDevice, iqApp, message, new ConnectIQ.IQSendMessageListener()
                {
                    @Override
                    public void onMessageStatus(IQDevice iqDevice, IQApp iqApp, ConnectIQ.IQMessageStatus iqMessageStatus)
                    {
                        if (ConnectIQ.IQMessageStatus.SUCCESS == iqMessageStatus)
                        {
                            propagateAction(getString(R.string.action_log_hue_infos_send_success));
                        }
                        else
                        {
                            if (Constants.LOG_ACTIVE)
                            {
                                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Failure while trying to send message to CIQ-device. (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ", messageStatus=" + (iqMessageStatus != null ? iqMessageStatus.name() : "<unknown>") + ")");
                            }

                            propagateAction(getString(R.string.action_log_hue_infos_send_failure));
                        }
                    }
                });
            }
            catch (Exception e)
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Failed to send message with ids and names for lights/groups! (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ", status=" + (iqDeviceStatus != null ? iqDeviceStatus.name() : "<unknown>") + ")");
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
                propagateAction(getString(R.string.action_log_received_command) + " [ " + messageItem + " ]");

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

    private void propagateAction(final String action)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
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
                                if (Constants.LOG_ACTIVE)
                                {
                                    Log.w(getString(R.string.app_log_tag), LOG_PREFIX + "Failed to notify service-listener! " + serviceListener, e);
                                }
                            }
                        }
                    }
                }
            }
        }).start();
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

    private Notification createNotification()
    {
        final PendingIntent pendingIntent = (PendingIntent.getActivity(this, 0 , new Intent(this, Console.class), PendingIntent.FLAG_UPDATE_CURRENT));

        return new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_bulb)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_text))
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_SECRET)
                    .build();
    }

    private String buildIdsAndNamesMessage()
    {
        final StringBuilder message = new StringBuilder("");

        String lightIdsAndNames = concatIdsAndNames(hueLightIdsAndNames);
        String groupIdsAndNames = concatIdsAndNames(hueGroupIdsAndNames);

        message.append(lightIdsAndNames);

        if (!"".equals(groupIdsAndNames)) {
            message.append(getString(R.string.ciq_mail_light_group_separator));
            message.append(groupIdsAndNames);
        }

        return message.toString();
    }

    private String concatIdsAndNames(HashMap<String, String> map)
    {
        final StringBuilder stringBuilder = new StringBuilder("");

        if (map != null && !map.isEmpty())
        {
            for (String key : map.keySet())
            {
                if (!stringBuilder.toString().isEmpty())
                {
                    stringBuilder.append(getString(R.string.ciq_mail_item_separator));
                }

                stringBuilder.append(key);
                stringBuilder.append(getString(R.string.ciq_mail_id_name_separator));
                stringBuilder.append(map.get(key));
            }
        }

        return stringBuilder.toString();
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

        if (hueCIQCommand.isGroup())
        {
            hueSimpleAPIClient.setOnForGroup(hueCIQCommand.getId(), status);
        }
        else
        {
            hueSimpleAPIClient.setOnForLight(hueCIQCommand.getId(), status);
        }
    }

    private void changeBrightness(String message)
    {
        final HueCIQCommand hueCIQCommand = new HueCIQCommand(message);
        final int brightness = (int)(Integer.parseInt(hueCIQCommand.getCommand().substring(message.lastIndexOf(getString(R.string.ciq_command_token_separator)) + 1)) * 2.5);

        if (hueCIQCommand.isGroup())
        {
            hueSimpleAPIClient.setBriForGroup(hueCIQCommand.getId(), brightness);
        }
        else
        {
            hueSimpleAPIClient.setBriForLight(hueCIQCommand.getId(), brightness);
        }
    }

    private void changeColor(String message)
    {
        final HueCIQCommand hueCIQCommand = new HueCIQCommand(message);
        final String command = hueCIQCommand.getCommand();
        final String id = hueCIQCommand.getId();

        int[] rgbColor;

        if (command.endsWith(getString(R.string.ciq_color_command_blue)))
        {
            rgbColor = getResources().getIntArray(R.array.blue);
        }
        else if (command.endsWith(getString(R.string.ciq_color_command_green)))
        {
            rgbColor = getResources().getIntArray(R.array.green);
        }
        else if (command.endsWith(getString(R.string.ciq_color_command_yellow)))
        {
            rgbColor = getResources().getIntArray(R.array.yellow);
        }
        else if (command.endsWith(getString(R.string.ciq_color_command_orange)))
        {
            rgbColor = getResources().getIntArray(R.array.orange);
        }
        else if (command.endsWith(getString(R.string.ciq_color_command_purple)))
        {
            rgbColor = getResources().getIntArray(R.array.purple);
        }
        else
        {
            rgbColor = getResources().getIntArray(R.array.red);
        }

        final float[] xyColor = PHUtilities.calculateXYFromRGB(rgbColor[0], rgbColor[1], rgbColor[2], "");

        if (hueCIQCommand.isGroup())
        {
            hueSimpleAPIClient.setXYForGroup(id, xyColor);
        }
        else
        {
            hueSimpleAPIClient.setXYForLight(id, xyColor);
        }
    }

    private class HueCIQCommand
    {
        private String command;
        private String id;
        private boolean group;

        public HueCIQCommand(String message)
        {
            if (message != null && !message.isEmpty())
            {
                final String[] splittedMessage = message.split(getString(R.string.ciq_command_id_separator));

                if (splittedMessage.length == 2)
                {
                    this.command = splittedMessage[0];
                    this.id = splittedMessage[1];

                    if (id.startsWith(getString(R.string.ciq_command_group_id_prefix)))
                    {
                        this.id = this.id.substring(1);
                        this.group = true;
                    }
                }
            }
        }

        public String getCommand()
        {
            return command;
        }

        public String getId()
        {
            return id;
        }

        public boolean isGroup()
        {
            return group;
        }
    }
}
