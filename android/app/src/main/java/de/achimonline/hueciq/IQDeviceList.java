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

public class IQDeviceList extends ListActivity
{
    private static final String LOG_PREFIX = IQDeviceList.class.getSimpleName() + " - ";

    private IQDeviceAdapter iqDeviceAdapter;
    private ConnectIQ connectIQ;

    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.devicelist_title));

        setContentView(R.layout.devicelist);

        emptyView = (TextView) findViewById(android.R.id.empty);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

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
            }

            @Override
            public void onSdkReady()
            {
                if (Constants.LOG_ACTIVE)
                {
                    Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-SDK ready.");
                }

                loadDevices();
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

    @Override
    protected void onPause()
    {
        super.onPause();

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
            final Intent intent = Helpers.getIntent(this, Console.class);
            intent.putExtra(Console.EXTRA_IQDEVICE_IDENTIFIER, iqDevice.getDeviceIdentifier());
            intent.putExtra(Console.EXTRA_IQDEVICE_NAME, iqDevice.getFriendlyName());

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
                                Log.i(getString(R.string.app_log_tag), LOG_PREFIX + "CIQ-device-status changed; device=" + (device != null ? device.getFriendlyName() : "<unknown>") + ", status=" + (status != null ? status.name() : "<unknown>"));
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

