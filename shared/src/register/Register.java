package network.register;

import com.esotericsoftware.kryo.Kryo;
import messages.TextMsg;
import messages.PeopleCountMsg;
import messages.DTO.PlayerState;
import messages.DTO.Input;

public class Register {
    // This registers objects that are going to be sent over the network.
    public static void  register(Kryo kryo) {
        kryo.register(TextMsg.class);
        kryo.register(PeopleCountMsg.class);
        kryo.register(PlayerState.class);
        kryo.register(Input.class);
    }
}
