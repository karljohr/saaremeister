package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import constant.BuildingInfo;
import constant.Category;
import ee.kool.saaremeister.server.game.object.Player;
import message.dto.ShopItem;

import java.util.Map;

/**
 * Handles incoming shop purchase messages from the client.
 */
public class ShopPurchaseListener extends Listener {

    private final Map<Integer, Player> activePlayers;

    public ShopPurchaseListener(Map<Integer, Player> activePlayers) {
        this.activePlayers = activePlayers;
    }

    /**
     * Handles incoming shop messages from the client.
     */
    @Override
    public void received(com.esotericsoftware.kryonet.Connection conn, Object object) {
        if (object instanceof message.ShopPurchaseMessage msg) {

            Player p = activePlayers.get(conn.getID());
            if (p != null) {
                ShopItem item = common.ShopRegistry.getItemById(msg.getItemId());

                if (item != null) {
                    // Use the price defined in the ShopItem
                    boolean success = p.attemptPurchase(item.getPrice());

                    if (success) {
                        Log.info("Purchased " + item.getName() + " for " + p.getUsername() + ". New balance: " + p.getMoney());
                        // TODO: Give the item/benefit to the player here (to the hotbar)
                        if (item.getCategory() == Category.EHITISED) {
                            // Give player building permit.
                            p.addBuildingPermit(BuildingInfo.getTileId(item.getId()));
                        }

                    } else {
                        Log.info(p.getUsername() + " didn't have enough money for " + item.getName());
                    }
                } else {
                    Log.error("Received purchase request for non-existent item ID: " + msg.getItemId());
                }
            } else {
                Log.error("Player object not found for ID " + conn.getID());
            }
        }
    }
}
