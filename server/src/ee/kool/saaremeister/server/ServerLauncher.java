package ee.kool.saaremeister.server;

import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.listener.ServerListener;

import java.io.IOException;

import static constant.Constants.TCP_PORT;
import static constant.Constants.UDP_PORT;
import static network.Register.registerClasses;

/**
 * Launches the server.
 */
public class ServerLauncher {

    public static void main(String[] args) {
        try {
            Server server = new Server();
            registerClasses(server.getKryo());
            server.start();
            server.bind(TCP_PORT, UDP_PORT);
            server.addListener(new ServerListener());

        } catch (IOException e) {
            Log.error("Server failed to start.", e);
        }
    }
}
