package tma.elitex.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tma.elitex.SignInActivity;

/**
 * Boot Receiver will start the application on device boot
 *
 * Created by Krum Iliev.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent startIntent = new Intent(context, SignInActivity.class);
            context.startActivity(startIntent);
        }
    }
}
