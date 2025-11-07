package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.example.util.AlertUtils;

import java.io.IOException;

public class HomeController {

    @FXML
    private Button btnIrParaCadastro;

    @FXML
    private Button btnIrParaVerificacao;

    @FXML
    void irParaCadastro(ActionEvent event) {
        carregarTela("CadastroView");
    }

    @FXML
    void irParaVerificacao(ActionEvent event) {
        carregarTela("VerificacaoView");
    }

    /**
     * Método auxiliar para navegar entre as telas.
     * Ele encontra o BorderPane principal e carrega o FXML no centro dele.
     */
    private void carregarTela(String nomeTela) {
        try {
            // Encontra o BorderPane principal (a "casca")
            BorderPane mainPane = (BorderPane) btnIrParaCadastro.getScene().getRoot();

            // Carrega o novo FXML
            Parent tela = FXMLLoader.load(getClass().getResource("/view/" + nomeTela + ".fxml"));

            // Define o FXML no centro do BorderPane
            mainPane.setCenter(tela);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erro ao carregar a tela: " + nomeTela);
        }
    }
}