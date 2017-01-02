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

    public void setOnForLight(String id, boolean state)
    {
        setOn(URI_LIGHTS_STATE, id, state);
    }

    public void setOnForGroup(String id, boolean state)
    {
        setOn(URI_GROUPS_ACTION, id, state);
    }

    private void setOn(String uri, String id, boolean state)
    {
        try
        {
            final StringEntity jsonBody = new StringEntity(String.format("{ \"on\": %1$s }", Boolean.toString(state)));

            defaultHttpClient.execute(createHttpPut(uri, id, jsonBody));
        }
        catch (Exception e)
        {
        }
    }

    public void setXYForLight(String id, float[] xyColor)
    {
        setXY(URI_LIGHTS_STATE, id, xyColor);
    }

    public void setXYForGroup(String id, float[] xyColor)
    {
        setXY(URI_GROUPS_ACTION, id, xyColor);
    }

    private void setXY(String uri, String id, float[] xyColor)
    {
        try
        {
            final StringEntity jsonBody = new StringEntity(String.format("{ \"xy\": [%1$f, %2$f] }", xyColor[0], xyColor[1]));

            defaultHttpClient.execute(createHttpPut(uri, id, jsonBody));
        }
        catch (Exception e)
        {
        }
    }

    public void setBriForLight(String id, int value)
    {
        setBri(URI_LIGHTS_STATE, id, value);
    }

    public void setBriForGroup(String id, int value)
    {
        setBri(URI_GROUPS_ACTION, id, value);
    }

    private void setBri(String uri, String id, int value)
    {
        try
        {
            final StringEntity jsonBody = new StringEntity(String.format("{ \"bri\": %1$d }", value));

            defaultHttpClient.execute(createHttpPut(uri, id, jsonBody));
        }
        catch (Exception e)
        {
        }
    }

    public void test()
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

    private HttpPut createHttpPut(String uri, String id, StringEntity entity)
    {
        final HttpPut httpPut = new HttpPut(String.format(uri, ipAddress, userName, id));
        httpPut.setHeader("Accept", "application/json");
        httpPut.setHeader("Content-type", "application/json");
        httpPut.setEntity(entity);

        return httpPut;
    }
}
