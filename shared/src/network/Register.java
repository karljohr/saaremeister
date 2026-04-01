package network;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import message.dto.*;
import constant.Category;

import java.util.ArrayList;

public class Register {

    /**
     // Register classes sent over the network.
     */
    public static void registerClasses(Kryo kryo) {
        kryo.register(ArrayList.class);
        kryo.register(String.class);
        kryo.register(message.GameJoinMessage.class);
        kryo.register(message.GameStateMessage.class);
        kryo.register(message.PlayerMovementMessage.class);
        kryo.register(message.PlayerShootingMessage.class);
        kryo.register(message.StartGameMessage.class);
        kryo.register(BulletState.class);
        kryo.register(PlayerState.class);
        kryo.register(Direction.class);
        kryo.register(message.LobbyUpdateMessage.class);
        kryo.register(message.LobbyExitMessage.class);
        kryo.register(message.ChatMessage.class);
        kryo.register(message.ChatUpdateMessage.class);
        kryo.register(CaptureZoneState.class);
        kryo.register(message.ShopPurchaseMessage.class, 50);
        kryo.register(ShopItem.class);
        kryo.register(Category.class);

        // Box2D debug
        kryo.register(StaticBodyState.class);
        kryo.register(StaticBodyState[].class);
        kryo.register(float[].class);
        kryo.register(Vector2.class);
    }
}
