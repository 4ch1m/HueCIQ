package de.achimonline.hueciq;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;

import java.util.Arrays;

public class Main extends Activity
{
    private static final String LOG_PREFIX = Main.class.getSimpleName() + " - ";

    private PHHueSDK phHueSDK;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (Console.isRunning)
        {
            startActivity(Helpers.getIntent(this, Console.class));
        }

        phHueSDK = PHHueSDK.create();

        sharedPreferences = SharedPreferences.getInstance(getApplicationContext());

        setTitle(getString(R.string.app_name));

        setContentView(R.layout.main);

        final TextView mainInfo = (TextView) findViewById(R.id.maininfo);
        mainInfo.setText(Html.fromHtml(String.format(getString(R.string.main_info), getString(R.string.app_name))));
        mainInfo.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void start(View view)
    {
        startActivity(Helpers.getIntent(this, HueBridgeList.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.clear_cached_bridge_info:
                clearCachedBridgeInfo();
                break;
            case R.id.about:
                showAbout();
                break;
        }

        return true;
    }

    private void clearCachedBridgeInfo()
    {
        if (Constants.LOG_ACTIVE)
        {
            Log.i(getString(R.string.app_log_tag), LOG_PREFIX + " - " + "Clearing cached bridge info.");
        }

        final PHAccessPoint phAccessPoint = new PHAccessPoint();
        phAccessPoint.setIpAddress(sharedPreferences.getHueLastConnectedIPAddress());
        phAccessPoint.setUsername(sharedPreferences.getHueLastConnectedUsername());

        if (phHueSDK.isAccessPointConnected(phAccessPoint))
        {
            phHueSDK.setDisconnectedAccessPoint(Arrays.asList(phAccessPoint));
        }

        sharedPreferences.setHueLastConnectedIPAddress("");

        final Toast toast = Toast.makeText(this, getString(R.string.cached_bridge_info_cleared), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    private void showAbout()
    {
        String versionName = "?";

        try
        {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        }
        catch (PackageManager.NameNotFoundException e)
        {
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setTitle(getString(R.string.app_name) + " " + versionName);
        builder.setMessage(Html.fromHtml(String.format(getString(R.string.about_text), new String(Base64.decode("QWNoaW0gU2V1ZmVydA==", Base64.DEFAULT)))));

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        final Dialog dialog = builder.create();

        dialog.show();

        Linkify.addLinks((TextView) dialog.findViewById(android.R.id.message), Linkify.ALL);
    }
}
