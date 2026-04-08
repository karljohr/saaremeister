package ee.kool.saaremeister.component;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
    private final Skin skin;
    private final Table messagesTable;
    private final ScrollPane scrollPane;
    private final TextField inputField;

    /**
     * Creates a new ChatTable instance used in LobbyScreen and GameScreen.
     */
    public ChatTable(int lobbyId, String username, Skin skin) {
        this.lobbyId = lobbyId;
        this.skin = skin;

        messagesTable = new Table();
        messagesTable.top().left();

        scrollPane = new ScrollPane(messagesTable, skin);
        scrollPane.setFadeScrollBars(false);

        TextField.TextFieldStyle inputStyle =
                new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        inputStyle.font = skin.getFont("button");
        inputField = new TextField("", inputStyle);

        TextButton sendButton = new TextButton("->", skin);

        // Listens for the Send button to be clicked to send the message with the correct username.
        sendButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendMessage(username);
            }
        });

        add(scrollPane).width(450).height(300).colspan(2);
        row();
        add(inputField).width(350).padTop(8).padBottom(8);
        add(sendButton).width(80).padTop(8).padBottom(8);
    }

    /**
     * Sends a chat message to the server, when written in the designated input field and pressed the send button.
     */
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

    /**
     * Updates the chat messages displayed in the table.
     */
    public void updateMessages(List<String> messages) {
        messagesTable.clearChildren();
        for (String message : messages) {
            Label label = new Label(message, skin);
            label.setWrap(true);
            messagesTable.add(label).width(350).left().padBottom(4);
            label.setFontScale(2f);
            messagesTable.row();
        }
        messagesTable.layout();
        scrollPane.layout();
        scrollPane.setScrollPercentY(1f);
    }

    /**
     * Checks if the input field is focused and determines if the user can type a message.
     */
    public boolean isInputFocused() {
        Stage stage = getStage();
        return stage != null && stage.getKeyboardFocus() == inputField;
    }

    /**
     * Clears the focus from the input field, aka moves the focus back to movement.
     */
    public void clearInputFocus() {
        Stage stage = getStage();
        if (stage != null && stage.getKeyboardFocus() == inputField) {
            stage.setKeyboardFocus(null);
        }
    }

    /**
     * Checks if the user clicked inside the input field.
     */
    public boolean clickedInsideInput(Actor target) {
        return target == inputField;
    }
}