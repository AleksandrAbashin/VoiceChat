package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import src.Client;

public class Main extends Application {
    public Client client;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));

        Parent root = loader.load();//FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("AbashinGramm");
        primaryStage.setScene(new Scene(root, 600, 380));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
