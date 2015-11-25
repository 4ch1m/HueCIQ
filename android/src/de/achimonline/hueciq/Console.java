package de.achimonline.hueciq;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;

import java.text.DateFormat;
import java.util.Date;
import java.util.Stack;

public class Console extends Activity
{
    private static final String LOG_PREFIX = Console.class.getName() + " - ";

    public static final String EXTRA_IQDEVICE = "IQDevice";

    private Context context;

    private ConnectIQ connectIQ;
    private IQDevice iqDevice;

    private Intent serviceIntent;
    private ServiceBroadcastReceiver serviceBroadcastReceiver;

    private TextView deviceNameView;
    private TextView deviceStatusView;
    private TextView actionLog;

    private SizedStack<String> stack;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.console_title));

        setContentView(R.layout.console);

        context = getBaseContext();

        iqDevice = (IQDevice) getIntent().getParcelableExtra(EXTRA_IQDEVICE);

        deviceNameView = (TextView) findViewById(R.id.devicename);
        deviceStatusView = (TextView) findViewById(R.id.devicestatus);

        actionLog = (TextView) findViewById(R.id.actionlog);
        actionLog.setMovementMethod(new ScrollingMovementMethod());

        stack = new SizedStack<String>(Constants.ACTION_LOG_SIZE);

        if (iqDevice != null)
        {
            deviceNameView.setText(iqDevice.getFriendlyName());
            deviceStatusView.setText(iqDevice.getStatus().name());

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
                    if (serviceIntent == null)
                    {
                        if (Constants.LOG_ACTIVE)
                        {
                            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-app found on device. Starting background service...");
                        }

                        serviceIntent = new Intent(Console.this, ListenerService.class);
                        serviceIntent.putExtra(ListenerService.EXTRA_IQDEVICE, iqDevice);

                        startService(serviceIntent);

                        serviceBroadcastReceiver = new ServiceBroadcastReceiver();

                        final IntentFilter intentFilter = new IntentFilter(ListenerService.BROADCAST_ACTION);
                        LocalBroadcastManager.getInstance(context).registerReceiver(serviceBroadcastReceiver, intentFilter);
                    }
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.close_app:
                finish();
                break;
            case R.id.clear_log:
                ((TextView) findViewById(R.id.actionlog)).setText("");
                stack.clear();
                break;
        }

        return true;
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (isFinishing())
        {
            try
            {
                if (serviceBroadcastReceiver != null)
                {
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(serviceBroadcastReceiver);
                }

                if (serviceIntent != null)
                {
                    stopService(serviceIntent);
                }
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

            stack.push(data + " (" + DateFormat.getDateTimeInstance().format(new Date()) + ")");

            actionLog.setText(TextUtils.join("\n", stack.toArray(new String[stack.size()])));
        }
    }

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
            if (this.size() == maxSize)
            {
                this.remove(0);
            }

            return super.push((T) object);
        }
    }
}
