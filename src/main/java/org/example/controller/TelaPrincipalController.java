package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class TelaPrincipalController {

    @FXML
    private BorderPane mainPane;

    @FXML
    void abrirTelaCadastro(ActionEvent event) {
        carregarTela("CadastroView");
    }

    @FXML
    void abrirTelaVerificacao(ActionEvent event) {
        carregarTela("VerificacaoView");
    }

    @FXML
    void fecharAplicacao(ActionEvent event) {
        System.exit(0);
    }

    private void carregarTela(String nomeTela) {
        try {
            Parent tela = FXMLLoader.load(getClass().getResource("/view/" + nomeTela + ".fxml"));
            mainPane.setCenter(tela);
        } catch (IOException e) {
            e.printStackTrace();
            // Lide com o erro, por exemplo, mostrando um alerta para o usuário
        }
    }
}