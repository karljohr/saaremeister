package ee.kool.saaremeister.game;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShopItem {
    private final int id;
    private final String name;
    private final int price;
    private final String category; // For example "Relvad", "Ehitised"
}
