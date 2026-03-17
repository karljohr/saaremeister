package ee.kool.saaremeister.network.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.network.ServerConnection;
import message.dto.StaticBodyState;

public class DebugListener extends Listener {
    private final ServerConnection serverConnection;

    public DebugListener(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof StaticBodyState[]) {
            serverConnection.handleStaticBodies((StaticBodyState[]) object);
        }
    }
}
