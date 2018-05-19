package sample;


import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import src.Client;

public class Controller {

    Main main;
    @FXML
    Button send;

    @FXML
    Button start;

    @FXML
    TextArea text;

    @FXML
    TextField sendText;

    @FXML
    TextField host;
    @FXML
    TextField port;
    @FXML
    TextField user;

    Client client;

    public Controller(){

    }
    @FXML
    public void onSend(ActionEvent event){
        System.out.println("Click");
        client.sendTextMessage(sendText.getText());
        sendText.clear();
    }
    @FXML
    public void onStart(){
        client = new Client(this, host.getText(), port.getText(), user.getText());
        client.run();
    }

    public void append(String str){
        javafx.application.Platform.runLater( () -> text.setText(text.getText()+'\n'+str) );

    }
    @FXML
    public void startCall(){
        client.startSendingVoiceMessage();
    }

    @FXML
    public void endCall(){
        client.terminate();
    }
}
