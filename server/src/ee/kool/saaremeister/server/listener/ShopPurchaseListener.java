package ee.kool.saaremeister.server.listener;

import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import constant.BuildingInfo;
import constant.Category;
import ee.kool.saaremeister.server.game.object.player.Officer;
import ee.kool.saaremeister.server.game.object.player.Player;
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
                    common.ArmorType itemArmor = common.ArmorType.fromName(item.getName());
                    boolean goesToHotbar = (itemArmor == common.ArmorType.NONE && item.getCategory() != Category.EHITISED);

                    if (goesToHotbar && p.isHotbarFull()) {
                        Log.info(p.getUsername() + " hotbar on täis! Ost tühistatud.");
                        return;
                    }
                    // Use the price defined in the ShopItem
                    if (item.getCategory() == Category.EHITISED && p instanceof Officer) {
                        Log.info(p.getUsername() + " doesn't have the right to buy items from the EHITISED category");
                    } else {
                        boolean success = p.attemptPurchase(item.getPrice());

                    if (success) {
                        Log.info("Purchased " + item.getName() + " for " + p.getUsername() + ". New balance: " + p.getMoney());
                        if (itemArmor != common.ArmorType.NONE) {
                            p.applyArmor(itemArmor.getLevel(), itemArmor.getDefenseValue());
                        } else if (item.getCategory() == Category.EHITISED) {
                            // Give player building permit.
                            p.addBuildingPermit(BuildingInfo.getTileId(item.getId()));
                        } else if (item.getName().equals("Püstol")) {
                            // Don't increase hotbar item count for gun
                            Log.info(p.getUsername() + " sai püstoli.");
                        } else {
                            // Increase hotbar item count for potions
                            p.addHotbarItem();
                        }

                        conn.sendTCP(msg);

                        } else {
                            Log.info(p.getUsername() + " didn't have enough money for " + item.getName());
                        }
                    }
                } else {
                    Log.error("Received purchase request for non-existent item ID: " + msg.getItemId());
                }
            } else {
                Log.error("Player object not found for ID " + conn.getID());
            }
        }

        if (object instanceof message.RefundMessage msg) {
            Player p = activePlayers.get(conn.getID());
            if (p != null) {
                p.refundBuildingPermit(msg.getId());
            }
        }
    }
}
