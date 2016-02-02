package tma.elitex.server;

import java.io.Serializable;

/**
 * Created by Krum on 2/2/2016.
 */
public interface ServerListener extends Serializable{

    void requestReady (String result);

    void requestFailed (Exception e);
}
