package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.dao.DatabaseService;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseService.criarTabelas();

        Parent root = FXMLLoader.load(getClass().getResource("/view/TelaPrincipal.fxml"));
        primaryStage.setTitle("Sistema de Autenticação Biométrica");
        primaryStage.setScene(new Scene(root, 1300, 900));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}