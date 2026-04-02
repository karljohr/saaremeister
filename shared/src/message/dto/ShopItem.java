package message.dto;

import constant.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ShopItem {
    private int id;
    private String name;
    private int price;
    private Category category;
    private String textureName;
}
