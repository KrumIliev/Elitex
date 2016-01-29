package tma.elitex;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Krum on 1/17/2016.
 */
public class ServerConnectionService extends IntentService {

    public ServerConnectionService() {
        super(ServerConnectionService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch ((ServerRequests) intent.getSerializableExtra(getString(R.string.key_request))) {
            case LOGIN_USER:
                break;
            case LOGIN_TOKEN:
                break;
        }
    }
}
