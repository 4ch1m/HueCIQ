package de.achimonline.hueciq;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

public class Console extends ListActivity
{
    public static boolean isRunning = false;

    private static final String LOG_PREFIX = Console.class.getSimpleName() + " - ";

    private static final String STATE_ACTION_LOG = "actionLog";

    public static final String EXTRA_IQDEVICE_IDENTIFIER = "IQDeviceIdentifier";
    public static final String EXTRA_IQDEVICE_NAME = "IQDeviceName";

    private ConnectIQ connectIQ;

    private long iqDeviceIdentifier;
    private String iqDeviceName;
    private String bridgeIP;

    private SizedStackWithReversedGetter<String> actionLog;

    private ActionLogAdapter actionLogAdapter;

    private ServiceAPI serviceAPI;

    private SharedPreferences sharedPreferences;

    private ServiceListener.Stub serviceListener = new ServiceListener.Stub()
    {
        @Override
        public void handleAction() throws RemoteException
        {
            String action = serviceAPI.getLatestAction();

            if (Constants.LOG_ACTIVE)
            {
                Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Received data from ListenerService: " + action);
            }

            addToActionLog(action);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Service connection established.");
            }

            serviceAPI = ServiceAPI.Stub.asInterface(service);

            try
            {
                serviceAPI.addListener(serviceListener);
            }
            catch (RemoteException e)
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Failed to add service-listener.");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Service connection closed.");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        isRunning = true;

        sharedPreferences = SharedPreferences.getInstance(getApplicationContext());

        iqDeviceIdentifier = getIntent().getLongExtra(EXTRA_IQDEVICE_IDENTIFIER, 0l);
        iqDeviceName = getIntent().getStringExtra(EXTRA_IQDEVICE_NAME);
        bridgeIP = sharedPreferences.getHueLastConnectedIPAddress();

        if (iqDeviceIdentifier != 0l && iqDeviceName != null)
        {
            sharedPreferences.setIQDeviceName(iqDeviceName);
            sharedPreferences.setIQDeviceIdentifier(iqDeviceIdentifier);

            actionLog = new SizedStackWithReversedGetter<String>(Constants.ACTION_LOG_SIZE);
        }
        else
        {
            iqDeviceIdentifier = sharedPreferences.getIQDeviceIdentifier();
            iqDeviceName = sharedPreferences.getIQDeviceName();

            actionLog = new SizedStackWithReversedGetter<String>(Constants.ACTION_LOG_SIZE, sharedPreferences.getActionLogHistory());
        }

        setTitle(getString(R.string.console_title));

        setContentView(R.layout.console);

        ((TextView) findViewById(R.id.devicename)).setText(iqDeviceName);
        ((TextView) findViewById(R.id.bridgeip)).setText(bridgeIP);

        final TextView headerView = new TextView(this);
        headerView.setText(R.string.console_actionlog);
        headerView.setBackgroundResource(android.R.color.black);

        getListView().setBackgroundResource(android.R.color.black);
        getListView().addHeaderView(headerView);
        getListView().setSelectionAfterHeaderView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        actionLogAdapter = new ActionLogAdapter(this, R.layout.actionlogrow, R.id.actionlogitem, actionLog);

        setListAdapter(actionLogAdapter);

        if (!Service.isRunning(this))
        {
            connectIQ = ConnectIQ.getInstance(this, ConnectIQ.IQConnectType.WIRELESS);
            connectIQ.initialize(this, true, new ConnectIQ.ConnectIQListener()
            {
                @Override
                public void onSdkReady()
                {
                    getCIQApplicationInfoAndStartService(getIQDeviceFromSDK(iqDeviceIdentifier, iqDeviceName));
                }

                @Override
                public void onInitializeError(ConnectIQ.IQSdkErrorStatus iqSdkErrorStatus)
                {
                    if (Constants.LOG_ACTIVE)
                    {
                        Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Error while trying to initialize CIQ-SDK.");
                    }
                }

                @Override
                public void onSdkShutDown()
                {
                    if (Constants.LOG_ACTIVE)
                    {
                        Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Shutting down CIQ-SDK.");
                    }
                }
            });
        }
        else
        {
            bindServiceConnection();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        sharedPreferences.setActionLogHistory(actionLog.toStringArray());

        unregisterFromAllConnectIQEventsAndShutdownSDK();
    }

    @Override
    protected void onDestroy()
    {
        sanitize();

        isRunning = false;

        super.onDestroy();
    }

    @Override
    public void onBackPressed()
    {
    }

    private void removeListenerAndUnbindService()
    {
        try
        {
            serviceAPI.removeListener(serviceListener);
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to remove service-listeners.");
            }
        }

        try
        {
            unbindService(serviceConnection);
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to remove unbind the service.");
            }
        }
    }

    private void stopService()
    {
        try
        {
            stopService(new Intent(Service.class.getName()));
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to stop the service.");
            }
        }
    }

    private void unregisterFromAllConnectIQEventsAndShutdownSDK()
    {
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

    private void getCIQApplicationInfoAndStartService(final IQDevice iqDevice)
    {
        try
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Checking for app on CIQ-device. (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ", appId=" + getString(R.string.ciq_app_id) + ")");
            }

            addToActionLog(String.format(getString(R.string.action_log_requesting_app_info), iqDevice.getFriendlyName()));

            connectIQ.getApplicationInfo(getString(R.string.ciq_app_id), iqDevice, new ConnectIQ.IQApplicationInfoListener()
            {
                @Override
                public void onApplicationInfoReceived(IQApp app)
                {
                    if (Constants.LOG_ACTIVE)
                    {
                        Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-app found on device. Starting background service...");
                    }

                    unregisterFromAllConnectIQEventsAndShutdownSDK();

                    addToActionLog(String.format(getString(R.string.action_log_app_found), getString(R.string.app_name), iqDevice.getFriendlyName()));

                    final Intent serviceIntent = new Intent(Service.class.getName());
                    serviceIntent.putExtra(Service.EXTRA_IQDEVICE_IDENTIFIER, iqDevice.getDeviceIdentifier());
                    serviceIntent.putExtra(Service.EXTRA_IQDEVICE_NAME, iqDevice.getFriendlyName());
                    serviceIntent.putExtra(Service.EXTRA_PHHUE_IP_ADDRESS, sharedPreferences.getHueLastConnectedIPAddress());
                    serviceIntent.putExtra(Service.EXTRA_PHHUE_USER_NAME, sharedPreferences.getHueLastConnectedUsername());
                    serviceIntent.putExtra(Service.EXTRA_PHHUE_LIGHT_IDS_AND_NAMES, sharedPreferences.getHueLightIdsAndNames());

                    startService(serviceIntent);

                    bindServiceConnection();
                }

                @Override
                public void onApplicationNotInstalled(String applicationId)
                {
                    if (Constants.LOG_ACTIVE)
                    {
                        Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-app not installed on device.");
                    }

                    final AlertDialog.Builder dialog = new AlertDialog.Builder(Console.this);
                    dialog.setTitle(R.string.missing_ciq_app);
                    dialog.setMessage(R.string.missing_ciq_app_message);
                    dialog.setPositiveButton(android.R.string.ok, null);
                    dialog.create().show();
                }
            });
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to get ApplicationInfo.");
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        menu.findItem(R.id.close_app).setTitle(String.format(getString(R.string.menu_exit_app), getString(R.string.app_name)));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.console, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Saving instance.");
        }

        outState.putStringArray(STATE_ACTION_LOG, actionLog.toArray(new String[actionLog.size()]));

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Restoring instance.");
        }

        actionLog.clear();

        final String[] savedActionLogItems = savedInstanceState.getStringArray(STATE_ACTION_LOG);

        if (savedActionLogItems != null)
        {
            for (String savedActionLogItem : savedActionLogItems)
            {
                actionLog.push(savedActionLogItem);
            }
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.close_app:
                sanitize();
                stopService();
                finish();
                break;
            case R.id.clear_log:
                actionLog.clear();
                actionLogAdapter.notifyDataSetChanged();
                break;
            case R.id.test_lights:
                new HueSimpleAPIClient(sharedPreferences.getHueLastConnectedIPAddress(), sharedPreferences.getHueLastConnectedUsername()).testLights();
                Toast.makeText(this, getString(R.string.console_toast_test_lights), Toast.LENGTH_LONG).show();
                break;
        }

        return true;
    }

    private void bindServiceConnection()
    {
        bindService(new Intent(Service.class.getName()), serviceConnection, MODE_PRIVATE);
    }

    private void sanitize()
    {
        removeListenerAndUnbindService();
        unregisterFromAllConnectIQEventsAndShutdownSDK();
    }

    private void addToActionLog(String actionItem)
    {
        actionLog.push(actionItem + " (" + DateFormat.getDateTimeInstance().format(new Date()) + ")");

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                actionLogAdapter.notifyDataSetChanged();
            }
        });
    }

    private class ActionLogAdapter extends ArrayAdapter<String>
    {
        private class ViewHolder
        {
            public TextView actionLogItem;
        }

        private List<String> listObjects;
        private int layoutResourceId;
        private int textViewResourceId;

        private LayoutInflater inflater;

        public ActionLogAdapter(Context context, int layoutResourceId, int textViewResourceId, List<String> listObjects)
        {
            super(context, layoutResourceId, textViewResourceId, listObjects);

            this.listObjects = listObjects;
            this.layoutResourceId = layoutResourceId;
            this.textViewResourceId = textViewResourceId;

            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            ViewHolder viewHolder;

            if (convertView == null)
            {
                convertView = inflater.inflate(layoutResourceId, null);

                viewHolder = new ViewHolder();
                viewHolder.actionLogItem = (TextView)convertView.findViewById(textViewResourceId);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            convertView.setBackgroundResource(position % 2 == 0 ? R.drawable.list_selector_even : R.drawable.list_selector_odd);

            viewHolder.actionLogItem.setText(listObjects.get(position));

            return convertView;
        }
    }

    private class SizedStackWithReversedGetter<T> extends Stack<T>
    {
        private int maxSize;

        public SizedStackWithReversedGetter(int size)
        {
            super();

            this.maxSize = size;
        }

        public SizedStackWithReversedGetter(int size, String[] elements)
        {
            this(size);

            for (String element : elements)
            {
                this.push(element);
            }
        }

        @Override
        public Object push(Object object)
        {
            while (this.size() >= maxSize)
            {
                this.remove(0);
            }

            return super.push((T) object);
        }

        @Override
        public T get(int location)
        {
            return location >= 0 && location < size() ? super.get((size() - location) - 1) : super.get(location);
        }

        public String[] toStringArray()
        {
            Enumeration<T> elements = this.elements();
            ArrayList<String> arrayList = new ArrayList<String>(this.size());

            while (elements.hasMoreElements())
            {
                arrayList.add((String) elements.nextElement());
            }

            return arrayList.toArray(new String[arrayList.size()]);
        }
    }
}
