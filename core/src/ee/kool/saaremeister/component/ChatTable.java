package ee.kool.saaremeister.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import ee.kool.saaremeister.network.ServerConnection;
import message.ChatMessage;
import java.util.List;

/**
 * A table that displays and allows the user to send chat messages. Used in LobbyScreen and GameScreen.
 */
public class ChatTable extends Table {

    private final int lobbyId;
    private final Table messagesTable;
    private final ScrollPane scrollPane;
    private final TextField inputField;
    private final Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.BLACK);

    public ChatTable(int lobbyId, String username) {
        this.lobbyId = lobbyId;

        BitmapFont font = new BitmapFont();
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.BLACK;
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.BLACK;

        messagesTable = new Table();
        messagesTable.top().right();

        scrollPane = new ScrollPane(messagesTable);
        scrollPane.setFadeScrollBars(false);

        inputField = new TextField("", textFieldStyle);

        TextButton sendButton = new TextButton("Send", buttonStyle);
        sendButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendMessage(username);
            }
        });

        add(scrollPane).width(300).height(200).colspan(2);
        row();
        add(inputField).width(240).padTop(5);
        add(sendButton).width(55).padTop(5);
    }

    private void sendMessage(String username) {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        ChatMessage msg = new ChatMessage();
        msg.setLobbyId(lobbyId);
        msg.setUsername(username);
        msg.setText(text);
        ServerConnection.getInstance().getClient().sendTCP(msg);
        System.out.println("Sending message to lobby " + lobbyId + ": " + text);
        inputField.setText("");
    }

    public void updateMessages(List<String> messages) {
        messagesTable.clearChildren();
        for (String message : messages) {
            Label label = new Label(message, labelStyle);
            label.setWrap(true);
            messagesTable.add(label).width(280).left().padBottom(2);
            messagesTable.row();
        }
        messagesTable.layout();
        scrollPane.layout();
        scrollPane.setScrollPercentY(1f);
    }
}