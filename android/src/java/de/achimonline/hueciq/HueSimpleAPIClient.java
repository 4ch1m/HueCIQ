package de.achimonline.hueciq;

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

    public void setHue(String lightId, int value)
    {
        try
        {
            final StringEntity jsonBody = new StringEntity(String.format("{ \"hue\": %1$d }", value));

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
}
