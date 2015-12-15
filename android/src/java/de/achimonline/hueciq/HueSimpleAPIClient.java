package de.achimonline.hueciq;

import com.philips.lighting.hue.sdk.utilities.PHUtilities;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class HueSimpleAPIClient
{
    private static final String URI_LIGHTS_STATE = "http://%1$s/api/%2$s/lights/%3$s/state";

    private String ipAddress;
    private String userName;

    private DefaultHttpClient defaultHttpClient;

    public HueSimpleAPIClient(String ipAddress, String userName)
    {
        this.ipAddress = ipAddress;
        this.userName = userName;

        defaultHttpClient = new DefaultHttpClient();
    }

    public void setOn(String lightId, boolean state)
    {
        try
        {
            final StringEntity jsonBody = new StringEntity(String.format("{ \"on\": %1$s }", Boolean.toString(state)));

            defaultHttpClient.execute(createHttpPutForStateChange(lightId, jsonBody));
        }
        catch (Exception e)
        {
        }
    }

    public void setXYFromRGB(String lightId, int[] rgb)
    {
        try
        {
            final float[] xy = xyColorFromRGB(rgb);

            final StringEntity jsonBody = new StringEntity(String.format("{ \"xy\": [%1$f, %2$f] }", xy[0], xy[1]));

            defaultHttpClient.execute(createHttpPutForStateChange(lightId, jsonBody));
        }
        catch (Exception e)
        {
        }
    }

    public void setBri(String lightId, int value)
    {
        try
        {
            final StringEntity jsonBody = new StringEntity(String.format("{ \"bri\": %1$d }", value));

            defaultHttpClient.execute(createHttpPutForStateChange(lightId, jsonBody));
        }
        catch (Exception e)
        {
        }
    }

    private HttpPut createHttpPutForStateChange(String lightId, StringEntity entity)
    {
        final HttpPut httpPut = new HttpPut(String.format(URI_LIGHTS_STATE, ipAddress, userName, lightId));
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-type", "application/json");
        httpPut.setEntity(entity);

        return httpPut;
    }

    private float[] xyColorFromRGB(int[] rgb)
    {
        return PHUtilities.calculateXYFromRGB(rgb[0], rgb[1], rgb[2], "");
    }
}
