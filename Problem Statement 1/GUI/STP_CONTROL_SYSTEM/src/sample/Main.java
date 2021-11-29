package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("stp_layout.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("STP Control Systems");
        primaryStage.getIcons().add(new Image("sample/369.jpg"));
        primaryStage.setScene(new Scene(root,650,400));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
