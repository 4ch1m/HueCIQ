package de.achimonline.hueciq;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class HueSimpleAPIClient
{
    private static final String URI_LIGHTS_STATE = "http://%1$s/api/%2$s/lights/%3$s/state";
    private static final String URI_GROUPS_ACTION = "http://%1$s/api/%2$s/groups/%3$s/action";

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

            defaultHttpClient.execute(createHttpPut(URI_LIGHTS_STATE, lightId, jsonBody));
        }
        catch (Exception e)
        {
        }
    }

    public void setXY(String lightId, float[] xyColor)
    {
        try
        {
            final StringEntity jsonBody = new StringEntity(String.format("{ \"xy\": [%1$f, %2$f] }", xyColor[0], xyColor[1]));

            defaultHttpClient.execute(createHttpPut(URI_LIGHTS_STATE, lightId, jsonBody));
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

            defaultHttpClient.execute(createHttpPut(URI_LIGHTS_STATE, lightId, jsonBody));
        }
        catch (Exception e)
        {
        }
    }

    public void testLights()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final HttpPut httpPut = createHttpPut(URI_GROUPS_ACTION, "0", new StringEntity("{ \"on\": true, \"bri\": 255, \"sat\": 255, \"hue\": 0, \"alert\": \"select\" }"));

                    for (int i = 0; i < 3; i++)
                    {
                        defaultHttpClient.execute(httpPut);
                        Thread.sleep(3_000);
                    }

                    defaultHttpClient.execute(createHttpPut(URI_GROUPS_ACTION, "0", new StringEntity("{ \"on\": false }")));
                }
                catch (Exception e)
                {
                }
            }
        }).start();
    }

    private HttpPut createHttpPut(String uri, String lightId, StringEntity entity)
    {
        final HttpPut httpPut = new HttpPut(String.format(uri, ipAddress, userName, lightId));
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-type", "application/json");
        httpPut.setEntity(entity);

        return httpPut;
    }
}
