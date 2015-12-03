package de.achimonline.hueciq;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Stack;

public class Console extends ListActivity
{
    private static final String LOG_PREFIX = Console.class.getSimpleName() + " - ";

    private static final String STATE_ACTION_LOG = "actionLog";
    private static final String STATE_SERVICE_STARTED = "serviceStarted";
    private static final String STATE_BROADCASTRECEIVER_REGISTERED = "broadcastReceiverStarted";

    public static final String EXTRA_IQDEVICE = "IQDevice";

    private Context context;

    private ConnectIQ connectIQ;
    private IQDevice iqDevice;

    private boolean broadcastReceiverIsRegistered = false;

    private TextView deviceNameView;

    private ServiceBroadcastReceiver serviceBroadcastReceiver;

    private boolean serviceStarted = false;

    private class SizedStack<T> extends Stack<T>
    {
        private int maxSize;

        public SizedStack(int size)
        {
            super();

            this.maxSize = size;
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
    }

    private SizedStack<String> actionLog = new SizedStack<String>(500);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.console_title));

        setContentView(R.layout.console);

        context = getBaseContext();

        iqDevice = (IQDevice) getIntent().getParcelableExtra(EXTRA_IQDEVICE);

        deviceNameView = (TextView) findViewById(R.id.devicename);

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

        setListAdapter(new ActionLogAdapter(this, R.layout.actionlogrow, R.id.actionlogitem, actionLog));

        if (!broadcastReceiverIsRegistered)
        {
            LocalBroadcastManager.getInstance(context).registerReceiver(new ServiceBroadcastReceiver(), new IntentFilter(ListenerService.BROADCAST_ACTION));
        }

        if (iqDevice != null)
        {
            deviceNameView.setText(iqDevice.getFriendlyName());

            if (!serviceStarted)
            {
                connectIQ = ConnectIQ.getInstance(this, ConnectIQ.IQConnectType.WIRELESS);
                connectIQ.initialize(this, true, new ConnectIQ.ConnectIQListener()
                {
                    @Override
                    public void onSdkReady()
                    {
                        getCIQApplicationInfoAndStartService();
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
        }
    }

    private void getCIQApplicationInfoAndStartService()
    {
        try
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Checking for app on CIQ-device. (device=" + (iqDevice != null ? iqDevice.getFriendlyName() : "<unknown>") + ", appId=" + getString(R.string.ciq_app_id) + ")");
            }

            connectIQ.getApplicationInfo(getString(R.string.ciq_app_id), iqDevice, new ConnectIQ.IQApplicationInfoListener()
            {
                @Override
                public void onApplicationInfoReceived(IQApp app)
                {
                    if (Constants.LOG_ACTIVE)
                    {
                        Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-app found on device. Starting background service...");
                    }

                    addToActionLog(String.format(getString(R.string.action_log_app_found), getString(R.string.app_name), iqDevice.getFriendlyName()));

                    final Intent serviceIntent = new Intent(Console.this, ListenerService.class);
                    serviceIntent.putExtra(ListenerService.EXTRA_IQDEVICE, iqDevice);

                    startService(serviceIntent);

                    serviceStarted = true;
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

        outState.putBoolean(STATE_BROADCASTRECEIVER_REGISTERED, broadcastReceiverIsRegistered);
        outState.putBoolean(STATE_SERVICE_STARTED, serviceStarted);
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

        broadcastReceiverIsRegistered = savedInstanceState.getBoolean(STATE_BROADCASTRECEIVER_REGISTERED);
        serviceStarted = savedInstanceState.getBoolean(STATE_SERVICE_STARTED);

        actionLog.clear();

        String[] savedActionLogItems = savedInstanceState.getStringArray(STATE_ACTION_LOG);

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
                finish();
                break;
            case R.id.clear_log:
                actionLog.clear();
                ((ActionLogAdapter)getListAdapter()).notifyDataSetChanged();
                break;
        }

        return true;
    }

    private void sanitize()
    {
        try
        {
            stopService(new Intent(Console.this, ListenerService.class));
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while trying to stop/unregister the service/receiver.");
            }
        }

        try
        {
            connectIQ.shutdown(this);
        }
        catch (Exception e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.wtf(getString(R.string.app_log_tag), LOG_PREFIX + "Exception while shutting down ConnectIQ-instance.");
            }
        }
    }

    private class ServiceBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String data = intent.getStringExtra(ListenerService.BROADCAST_DATA);

            if (Constants.LOG_ACTIVE)
            {
                Log.d(getString(R.string.app_log_tag), LOG_PREFIX + "Received data from ListenerService: " + data);
            }

            addToActionLog(data);
        }
    }

    private void addToActionLog(String actionItem)
    {
        actionLog.push(actionItem + " (" + DateFormat.getDateTimeInstance().format(new Date()) + ")");

        ((ActionLogAdapter)getListAdapter()).notifyDataSetChanged();
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
}
