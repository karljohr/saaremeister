package message;

import lombok.Data;

@Data
public class PlayerBuildMessage {
    public int id;
    public int tileX;
    public int tileY;
}
