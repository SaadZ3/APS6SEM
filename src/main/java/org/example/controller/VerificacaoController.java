package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class VerificacaoController {

    @FXML
    private Button btnSelecionarImagem;

    @FXML
    private Button btnVerificar;

    @FXML
    private Label lblCaminhoImagem;

    @FXML
    private TextField txtUsuario;

    @FXML // Variável para armazenar o arquivo de imagem selecionado
    private File arquivoImagemSelecionada;

    @FXML
    void selecionarImagem(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagem da Biometria");

        // Define filtros para aceitar apenas imagens
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg")
        );

        // Abre a janela de seleção
        Stage stage = (Stage) lblCaminhoImagem.getScene().getWindow();
        arquivoImagemSelecionada = fileChooser.showOpenDialog(stage);

        if (arquivoImagemSelecionada != null) {
            // Atualiza o Label para mostrar o nome do arquivo
            lblCaminhoImagem.setText(arquivoImagemSelecionada.getName());
        }
    }

    @FXML
    void verificar(ActionEvent event) {
        System.out.println("Lógica de verificação aqui...");
    }
}