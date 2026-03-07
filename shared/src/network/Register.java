package network;

import com.esotericsoftware.kryo.Kryo;
import message.dto.BulletState;
import message.dto.Direction;
import message.dto.PlayerState;

import java.util.ArrayList;

public class Register {

    public static void registerClasses(Kryo kryo) {
        // Register classes sent over the network.
        kryo.register(ArrayList.class);
        kryo.register(message.GameJoinMessage.class);
        kryo.register(message.GameStateMessage.class);
        kryo.register(message.PlayerMovementMessage.class);
        kryo.register(message.PlayerShootingMessage.class);
        kryo.register(message.StartGameMessage.class);
        kryo.register(BulletState.class);
        kryo.register(PlayerState.class);
        kryo.register(Direction.class);
    }
}
