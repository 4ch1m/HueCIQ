package de.achimonline.hueciq;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ProgressBar;
import com.philips.lighting.hue.sdk.PHAccessPoint;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.hue.sdk.PHMessageType;
import com.philips.lighting.hue.sdk.PHSDKListener;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHHueParsingError;

import java.util.List;

public class HuePushLinkAuthentication extends Activity
{
    private static final int MAX_TIME = 30;

    private ProgressBar progressBar;
    private PHHueSDK phHueSDK;

    private boolean isDialogShowing;

    private final PHSDKListener listener = new PHSDKListener()
    {
        @Override
        public void onError(int code, final String message)
        {
            if (code == PHMessageType.PUSHLINK_BUTTON_NOT_PRESSED)
            {
                incrementProgress();
            }
            else if (code == PHMessageType.PUSHLINK_AUTHENTICATION_FAILED)
            {
                incrementProgress();

                if (!isDialogShowing)
                {
                    isDialogShowing = true;

                    HuePushLinkAuthentication.this.runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(HuePushLinkAuthentication.this);

                            builder.setMessage(message).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                    finish();
                                }
                            });

                            builder.create();
                            builder.show();
                        }
                    });
                }
            }
        }

        @Override
        public void onCacheUpdated(List<Integer> list, PHBridge phBridge)
        {
        }

        @Override
        public void onBridgeConnected(PHBridge phBridge, String userName)
        {
        }

        @Override
        public void onAuthenticationRequired(PHAccessPoint phAccessPoint)
        {
        }

        @Override
        public void onAccessPointsFound(List<PHAccessPoint> list)
        {
        }

        @Override
        public void onConnectionResumed(PHBridge phBridge)
        {
        }

        @Override
        public void onConnectionLost(PHAccessPoint phAccessPoint)
        {
        }

        @Override
        public void onParsingErrors(List<PHHueParsingError> list)
        {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.pushlinkauthentication_title));

        setContentView(R.layout.pushlinkauthentication);

        isDialogShowing = false;

        progressBar = (ProgressBar) findViewById(R.id.countdownPB);
        progressBar.setMax(MAX_TIME);

        phHueSDK = PHHueSDK.getInstance();
        phHueSDK.getNotificationManager().registerSDKListener(listener);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        phHueSDK.getNotificationManager().unregisterSDKListener(listener);
    }

    private void incrementProgress()
    {
        progressBar.incrementProgressBy(1);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        phHueSDK.getNotificationManager().unregisterSDKListener(listener);
    }
}
