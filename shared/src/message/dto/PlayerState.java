package message.dto;

import lombok.Data;

@Data
public class PlayerState {
    private int id;
    private float x;
    private float y;
    private int lives;
}
