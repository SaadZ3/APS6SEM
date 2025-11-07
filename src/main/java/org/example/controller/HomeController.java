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

    private void carregarTela(String nomeTela) {
        try {
            BorderPane mainPane = (BorderPane) btnIrParaCadastro.getScene().getRoot();
            Parent tela = FXMLLoader.load(getClass().getResource("/view/" + nomeTela + ".fxml"));
            mainPane.setCenter(tela);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Erro ao carregar a tela: " + nomeTela);
        }
    }
}