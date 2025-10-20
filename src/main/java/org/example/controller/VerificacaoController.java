package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class VerificacaoController {

    @FXML
    private Button btnSelecionarImagem;

    @FXML
    private Button btnVerificar;

    @FXML
    private Label lblCaminhoImagem;

    @FXML
    private TextField txtUsuario;

    @FXML
    void selecionarImagem(ActionEvent event) {
        System.out.println("Lógica para abrir seletor de arquivo aqui...");
    }

    @FXML
    void verificar(ActionEvent event) {
        System.out.println("Lógica de verificação aqui...");
    }
}