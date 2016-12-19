package de.achimonline.hueciq;

import android.content.Context;
import android.content.Intent;

public class Helpers
{
    public static Intent getIntent(Context context, Class clazz)
    {
        final Intent intent = new Intent(context, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return intent;
    }
}
