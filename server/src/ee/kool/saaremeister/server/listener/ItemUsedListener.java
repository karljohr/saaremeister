package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.player.Player;
import message.ItemUsedMessage;
import java.util.Map;

public class ItemUsedListener extends Listener {
    private final Map<Integer, Player> activePlayers;

    public ItemUsedListener(Map<Integer, Player> activePlayers) {
        this.activePlayers = activePlayers;
    }

    @Override
    public void received(Connection conn, Object object) {
        if (object instanceof ItemUsedMessage msg) {
            Player p = activePlayers.get(conn.getID());
            if (p != null) {
                Integer itemId = p.getHotbarItemId(msg.getSelectedSlot());
                if (itemId == null) {
                    Log.warn("Mängija " + p.getUsername() + " üritas kasutada eset tühjast hotbarist!");
                    return;
                }

                switch (itemId) {
                    case 5 -> p.consumeHealthPotion();
                    case 6 -> p.consumeSpeedPotion();
                    case 7 -> p.consumeStrengthPotion();
                    default -> {
                        Log.warn("Mängija " + p.getUsername() + " üritas kasutada mittetarbitavat eset slotist " + msg.getSelectedSlot());
                        return;
                    }
                }

                p.clearHotbarSlot(msg.getSelectedSlot());
            }
        }
    }
}
