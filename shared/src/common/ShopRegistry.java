package common;

import constant.Category;
import message.dto.ShopItem;

import java.util.List;

/**
 * Registry for existing shop items.
 */
public class ShopRegistry {
    public static final List<ShopItem> ITEMS = List.of(
            new ShopItem(1, "Püstol", 500, Category.RELVAD, "gun"),
            new ShopItem(2, "Nahk rüü", 150, Category.TARVIKUD, "leather_armor"),
            new ShopItem(3, "Hõbe rüü", 400, Category.TARVIKUD, "silver_armor"),
            new ShopItem(4, "Teemant rüü", 700, Category.TARVIKUD, "diamond_armor"),
            new ShopItem(5, "Tervisejook", 50, Category.TARVIKUD, "health_pot"),
            new ShopItem(6, "Kiirusjook", 75, Category.TARVIKUD, "speed_pot"),
            new ShopItem(7, "Tugevusjook", 100, Category.TARVIKUD, "strength_pot"),
            new ShopItem(8, "Suurtükitorn", 50, Category.EHITISED, "gun_tower"),
            new ShopItem(9, "Müürid", 50, Category.EHITISED, "walls"),
            new ShopItem(
                    10,
                    "Ballistilise raketi baas",
                    200,
                    Category.EHITISED,
                    "ballistic_rocket_base"
            )
    );

    public static ShopItem getItemById(int id) {
        for (ShopItem item : ITEMS) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}