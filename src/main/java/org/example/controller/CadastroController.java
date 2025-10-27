package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser; // <-- Importe o FileChooser
import javafx.stage.Stage; // <-- Importe o Stage

import java.io.File; // <-- Importe o File
import java.net.URL;
import java.util.ResourceBundle;

public class CadastroController implements Initializable {

    // ... (outras variáveis FXML) ...
    @FXML private Label lblCaminhoImagem;
    @FXML private ComboBox<Integer> cmbNivelAcesso;
    @FXML private TextField txtNome;
    @FXML private TextField txtUsuario;

    // Variável para armazenar o arquivo de imagem selecionado
    private File arquivoImagemSelecionada;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbNivelAcesso.setItems(FXCollections.observableArrayList(1, 2, 3));
    }

    @FXML
    void cadastrar(ActionEvent event) {
        // ... (código de cadastro existente) ...

        // Verificação adicional
        if (arquivoImagemSelecionada == null) {
            System.out.println("Erro: Nenhuma imagem selecionada.");
            return;
        }

        System.out.println("Caminho da Imagem: " + arquivoImagemSelecionada.getAbsolutePath());

        // Chamar a lógica de cadastro no service aqui...
    }

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
}