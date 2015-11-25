package de.achimonline.hueciq;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.InvalidStateException;
import com.garmin.android.connectiq.exception.ServiceUnavailableException;

import java.util.List;

public class DeviceList extends ListActivity
{
    private static final String LOG_PREFIX = DeviceList.class.getName() + " - ";

    private IQDeviceAdapter iqDeviceAdapter;
    private ConnectIQ connectIQ;

    private boolean sdkReady = false;

    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.devicelist_title));

        setContentView(R.layout.devicelist);

        emptyView = (TextView) findViewById(android.R.id.empty);

        iqDeviceAdapter = new IQDeviceAdapter(this);
        getListView().setAdapter(iqDeviceAdapter);

        connectIQ = ConnectIQ.getInstance(this, ConnectIQ.IQConnectType.WIRELESS);
        connectIQ.initialize(this, true, new ConnectIQ.ConnectIQListener()
        {
            @Override
            public void onInitializeError(ConnectIQ.IQSdkErrorStatus errStatus)
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.e(getString(R.string.app_log_tag), LOG_PREFIX + "Error while trying to initialize CIQ-SDK.");
                }

                emptyView.setText(R.string.initialization_error + errStatus.name());
                sdkReady = false;
            }

            @Override
            public void onSdkReady()
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-SDK ready.");
                }

                loadDevices();

                sdkReady = true;
            }

            @Override
            public void onSdkShutDown()
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Shutting down CIQ-SDK.");
                }

                sdkReady = false;
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (sdkReady)
        {
            loadDevices();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (isFinishing()) {
            try
            {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.devicelist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.load_devices)
        {
            loadDevices();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        final IQDevice iqDevice = iqDeviceAdapter.getItem(position);

        if (iqDevice.getStatus() != IQDevice.IQDeviceStatus.CONNECTED)
        {
            final Toast toast = Toast.makeText(this, getString(R.string.device_has_to_be_connected), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
        else
        {
            final Intent intent = new Intent(this, Console.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(Console.EXTRA_IQDEVICE, iqDevice);

            startActivity(intent);
        }
    }

    public void loadDevices()
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "Loading CIQ-devices...");
        }

        try
        {
            final List<IQDevice> devices = connectIQ.getKnownDevices();

            if (devices != null)
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-devices found. Registering for device-events...");
                }

                iqDeviceAdapter.setDevices(devices);

                for (IQDevice device : devices)
                {
                    connectIQ.registerForDeviceEvents(device, new ConnectIQ.IQDeviceEventListener()
                    {
                        @Override
                        public void onDeviceStatusChanged(IQDevice device, IQDevice.IQDeviceStatus status)
                        {
                            if (Constants.LOG_ACTIVE)
                            {
                                Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-device-status changed; device=" + (device != null ? device.getFriendlyName() : "<unknown>") + ", status=" + status != null ? status.name() : "<unknown>");
                            }

                            iqDeviceAdapter.updateDeviceStatus(device, status);
                        }
                    });
                }
            }
            else
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "No CIQ-devices found.");
                }
            }
        }
        catch (InvalidStateException e)
        {
            if (Constants.LOG_ACTIVE)
            {
                Log.wtf(getString(R.string.app_log_tag), LOG_PREFIX + "InvalidStateException while trying to fetch known CIQ-devices.");
            }
        }
        catch (ServiceUnavailableException e)
        {
            emptyView.setText(R.string.service_unavailable);
        }
    }

    private class IQDeviceAdapter extends ArrayAdapter<IQDevice>
    {
        private LayoutInflater layoutInflater;

        public IQDeviceAdapter(Context context)
        {
            super(context, android.R.layout.simple_list_item_2);

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            final IQDevice device = getItem(position);

            ((TextView) convertView.findViewById(android.R.id.text1)).setText((device.getFriendlyName() == null) ? device.getDeviceIdentifier() + "" : device.getFriendlyName());
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(device.getStatus().name());

            return convertView;
        }

        public void setDevices(List<IQDevice> devices)
        {
            clear();
            addAll(devices);
            notifyDataSetChanged();
        }

        public synchronized void updateDeviceStatus(IQDevice device, IQDevice.IQDeviceStatus status)
        {
            IQDevice iqDeviceFromList;

            for (int i = 0; i < getCount(); i++)
            {
                iqDeviceFromList = getItem(i);

                if (iqDeviceFromList.getDeviceIdentifier() == device.getDeviceIdentifier())
                {
                    iqDeviceFromList.setStatus(status);
                    notifyDataSetChanged();
                    return;
                }
            }
        }
    }
}

