package network;

import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.kryo.Kryo;
import constant.Category;
import message.dto.BulletState;
import message.dto.Direction;
import message.dto.PlayerState;
import message.dto.StaticBodyState;
import message.dto.ShopItem;

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
        kryo.register(message.LobbyUpdateMessage.class);
        kryo.register(message.LobbyExitMessage.class);
        kryo.register(message.ChatMessage.class);
        kryo.register(message.ChatUpdateMessage.class);
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
