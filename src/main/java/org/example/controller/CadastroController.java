package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.bytedeco.opencv.opencv_core.Mat;
import org.example.dao.UsuarioDAO;
import org.example.service.BiometriaService;
import org.example.util.AlertUtils; // Importe o AlertUtils

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class CadastroController implements Initializable {

    @FXML private ComboBox<Integer> cmbNivelAcesso;
    @FXML private Label lblCaminhoImagem;
    @FXML private TextField txtNome;
    @FXML private TextField txtUsuario;

    // Nossos serviços e DAOs
    private File arquivoImagemSelecionada;
    private BiometriaService biometriaService;
    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Popula a ComboBox
        cmbNivelAcesso.setItems(FXCollections.observableArrayList(1, 2, 3));

        // Inicializa os serviços
        this.biometriaService = new BiometriaService();
        this.usuarioDAO = new UsuarioDAO();
    }

    @FXML
    void cadastrar(ActionEvent event) {
        // 1. Obter os dados da interface
        String nome = txtNome.getText();
        String usuario = txtUsuario.getText();
        Integer nivelAcesso = cmbNivelAcesso.getValue();

        // 2. Validação de campos
        if (nome.isEmpty() || usuario.isEmpty() || nivelAcesso == null) {
            AlertUtils.showErrorAlert("Todos os campos devem ser preenchidos.");
            return;
        }
        if (arquivoImagemSelecionada == null) {
            AlertUtils.showErrorAlert("Nenhuma imagem de biometria foi selecionada.");
            return;
        }

        try {
            // 3. Processar a biometria
            System.out.println("Extraindo recursos da imagem...");
            Mat descritores = biometriaService.extrairRecursos(arquivoImagemSelecionada);

            if (descritores.empty()) {
                AlertUtils.showErrorAlert("Não foi possível extrair recursos biométricos da imagem. Tente uma imagem mais nítida.");
                return;
            }

            // 4. Salvar no Banco de Dados
            System.out.println("Salvando usuário no banco de dados...");
            boolean sucesso = usuarioDAO.cadastrarUsuario(nome, usuario, nivelAcesso, descritores);

            // 5. Dar feedback ao usuário
            if (sucesso) {
                AlertUtils.showSuccessAlert("Usuário '" + usuario + "' cadastrado com sucesso!");
                limparCampos();
            } else {
                // O DAO já imprimiu o erro específico (ex: usuário duplicado) no console
                AlertUtils.showErrorAlert("Não foi possível cadastrar o usuário. Verifique o console para mais detalhes (possível usuário duplicado).");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Ocorreu um erro inesperado: " + e.getMessage());
        }
    }

    @FXML
    void selecionarImagem(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagem da Biometria");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tif")
        );

        Stage stage = (Stage) lblCaminhoImagem.getScene().getWindow();
        arquivoImagemSelecionada = fileChooser.showOpenDialog(stage);

        if (arquivoImagemSelecionada != null) {
            lblCaminhoImagem.setText(arquivoImagemSelecionada.getName());
        }
    }

    // Método auxiliar para limpar o formulário após o sucesso
    private void limparCampos() {
        txtNome.clear();
        txtUsuario.clear();
        cmbNivelAcesso.getSelectionModel().clearSelection();
        cmbNivelAcesso.setPromptText("Selecione o nível");
        lblCaminhoImagem.setText("Nenhuma imagem selecionada.");
        arquivoImagemSelecionada = null;
    }
}