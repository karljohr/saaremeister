package ee.kool.saaremeister.network.listener;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import ee.kool.saaremeister.network.ServerConnection;
import message.ChatUpdateMessage;

public class ChatListener extends Listener {
    private final ServerConnection serverConnection;

    public ChatListener(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    @Override
    public void received(Connection connection, Object object) {
        if (object instanceof ChatUpdateMessage chatUpdateMessage) {
            System.out.println("[CLIENT] ChatUpdate received! Messages: " + chatUpdateMessage.getMessages().size());
            Gdx.app.postRunnable(() -> {
                serverConnection.handleChatUpdate(chatUpdateMessage);
            });
        }
    }
}
