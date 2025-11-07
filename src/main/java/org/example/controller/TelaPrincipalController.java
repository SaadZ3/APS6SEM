package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable; // Importe
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL; // Importe
import java.util.ResourceBundle; // Importe

public class TelaPrincipalController implements Initializable {

    @FXML
    private BorderPane mainPane;

    /**
     * Chamado automaticamente para carregar a tela Home por padrão.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        carregarTela("HomeView");
    }

    /**
     * Método público para carregar uma tela no centro do BorderPane.
     */
    public void carregarTela(String nomeTela) {
        try {
            Parent tela = FXMLLoader.load(getClass().getResource("/view/" + nomeTela + ".fxml"));
            mainPane.setCenter(tela);
        } catch (IOException e) {
            e.printStackTrace();
            // Lide com o erro
        }
    }
}