package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import ee.kool.saaremeister.server.game.object.Player;
import message.ItemUsedMessage;
import java.util.Map;

public class ItemUsedListener extends Listener {
    private final Map<Integer, Player> activePlayers;

    public ItemUsedListener(Map<Integer, Player> activePlayers) {
        this.activePlayers = activePlayers;
    }

    @Override
    public void received(Connection conn, Object object) {
        if (object instanceof ItemUsedMessage) {
            Player p = activePlayers.get(conn.getID());
            if (p != null) {
                // Check if hotbar contains anything
                if (p.getHotbarItemCount() > 0) {
                    p.removeHotbarItem();
                } else {
                    Log.warn("Mängija " + p.getUsername() + " üritas kasutada eset tühjast hotbarist!");
                }
            }
        }
    }
}