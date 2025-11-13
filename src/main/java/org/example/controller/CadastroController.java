package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.bytedeco.opencv.opencv_core.Mat;
import org.example.dao.UsuarioDAO;
import org.example.service.BiometriaService;
import org.example.util.AlertUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class CadastroController implements Initializable {

    @FXML private ComboBox<String> cmbTipoBiometria;
    @FXML private VBox boxDigital;
    @FXML private VBox boxRosto;
    @FXML private Label lblCaminhoImagemRosto;
    @FXML private ImageView imgWebcamRosto;
    @FXML private Button btnLigarCamera;
    @FXML private Button btnCapturarRosto;
    @FXML private Button btnCadastrar;
    @FXML private ComboBox<Integer> cmbNivelAcesso;
    @FXML private Label lblCaminhoImagem;
    @FXML private TextField txtNome;
    @FXML private TextField txtUsuario;

    private File arquivoImagemDigital;
    private File arquivoImagemRosto;
    private BiometriaService biometriaService;
    private UsuarioDAO usuarioDAO;

    private final String TIPO_DIGITAL = "Impressão Digital";
    private final String TIPO_ROSTO = "Rosto";
    private final String TIPO_AMBOS = "Ambos";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cmbNivelAcesso.setItems(FXCollections.observableArrayList(1, 2, 3));
        cmbTipoBiometria.setItems(FXCollections.observableArrayList(TIPO_DIGITAL, TIPO_ROSTO, TIPO_AMBOS));

        this.biometriaService = new BiometriaService();
        this.usuarioDAO = new UsuarioDAO();

        cmbTipoBiometria.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> atualizarVisibilidadeCampos(newVal)
        );
    }

    private void atualizarVisibilidadeCampos(String tipo) {
        if (tipo == null || (!tipo.equals(TIPO_ROSTO) && !tipo.equals(TIPO_AMBOS))) {
            boxRosto.setVisible(false); boxRosto.setManaged(false);
            biometriaService.stopStreamWebcam();
            btnLigarCamera.setDisable(false);
            btnCapturarRosto.setDisable(true);
        } else {
            boxRosto.setVisible(true); boxRosto.setManaged(true);
        }

        boolean mostrarDigital = tipo != null && (tipo.equals(TIPO_DIGITAL) || tipo.equals(TIPO_AMBOS));
        boxDigital.setVisible(mostrarDigital); boxDigital.setManaged(mostrarDigital);
    }

    @FXML
    void ligarCamera(ActionEvent event) {
        biometriaService.iniciarStreamWebcam(imgWebcamRosto);
        btnLigarCamera.setDisable(true);
        btnCapturarRosto.setDisable(false);
    }

    @FXML
    void capturarRosto(ActionEvent event) {
        String caminhoTemp = "temp_face_capture.jpg";
        arquivoImagemRosto = biometriaService.capturarFrameEGravar(caminhoTemp);
        if (arquivoImagemRosto != null) {
            lblCaminhoImagemRosto.setText("Rosto capturado: " + arquivoImagemRosto.getName());
            AlertUtils.showSuccessAlert("Rosto capturado com sucesso!");
        } else {
            AlertUtils.showErrorAlert("Não foi possível capturar o rosto.");
        }
        biometriaService.stopStreamWebcam();
        btnLigarCamera.setDisable(false);
        btnCapturarRosto.setDisable(true);
    }

    @FXML
    void selecionarImagemDigital(ActionEvent event) {
        arquivoImagemDigital = selecionarArquivo("Selecionar Imagem da Digital");
        if (arquivoImagemDigital != null) {
            lblCaminhoImagem.setText(arquivoImagemDigital.getName());
        }
    }

    private File selecionarArquivo(String titulo) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(titulo);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tif")
        );
        Stage stage = (Stage) lblCaminhoImagem.getScene().getWindow();
        return fileChooser.showOpenDialog(stage);
    }

    @FXML
    void cadastrar(ActionEvent event) {
        String nome = txtNome.getText();
        String usuario = txtUsuario.getText();
        Integer nivelAcesso = cmbNivelAcesso.getValue();
        String tipoBiometria = cmbTipoBiometria.getValue();

        if (nome.isEmpty() || usuario.isEmpty() || nivelAcesso == null || tipoBiometria == null) {
            AlertUtils.showErrorAlert("Todos os campos principais devem ser preenchidos.");
            return;
        }

        Mat descritoresDigital = null;
        Mat descritoresRosto = null;

        try {
            if (tipoBiometria.equals(TIPO_DIGITAL) || tipoBiometria.equals(TIPO_AMBOS)) {
                if (arquivoImagemDigital == null) {
                    AlertUtils.showErrorAlert("Nenhuma imagem de digital foi selecionada.");
                    return;
                }
                System.out.println("Extraindo recursos da digital...");
                descritoresDigital = biometriaService.extrairRecursosDigital(arquivoImagemDigital);
            }
            if (tipoBiometria.equals(TIPO_ROSTO) || tipoBiometria.equals(TIPO_AMBOS)) {
                if (arquivoImagemRosto == null) {
                    AlertUtils.showErrorAlert("Nenhum rosto foi capturado.");
                    return;
                }
                System.out.println("Extraindo recursos do rosto...");
                descritoresRosto = biometriaService.extrairRecursosRosto(arquivoImagemRosto);

                if (descritoresRosto.empty()) {
                    AlertUtils.showErrorAlert("Não foi possível detectar um rosto na imagem capturada. Tente novamente com melhor iluminação.");
                    return;
                }
            }
            System.out.println("Salvando usuário no banco de dados...");
            boolean sucesso = usuarioDAO.cadastrarUsuario(nome, usuario, nivelAcesso, descritoresDigital, descritoresRosto);
            if (sucesso) {
                AlertUtils.showSuccessAlert("Usuário '" + usuario + "' cadastrado com sucesso!");
                limparCampos();
            } else {
                AlertUtils.showErrorAlert("Não foi possível cadastrar o usuário. (Possível usuário duplicado).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Ocorreu um erro inesperado: " + e.getMessage());
        }
    }

    private void limparCampos() {
        txtNome.clear();
        txtUsuario.clear();
        cmbNivelAcesso.getSelectionModel().clearSelection();
        cmbNivelAcesso.setPromptText("Selecione o nível");
        cmbTipoBiometria.getSelectionModel().clearSelection();
        cmbTipoBiometria.setPromptText("Selecione o tipo");
        lblCaminhoImagem.setText("Nenhuma imagem selecionada.");
        lblCaminhoImagemRosto.setText("Nenhum rosto capturado.");
        arquivoImagemDigital = null;
        arquivoImagemRosto = null;
        if (imgWebcamRosto != null) imgWebcamRosto.setImage(null);
        atualizarVisibilidadeCampos(null);
    }

    @FXML
    void voltarParaHome(ActionEvent event) {
        // Para a câmera, se estiver ligada
        biometriaService.stopStreamWebcam();

        try {
            // Encontra o BorderPane principal
            BorderPane mainPane = (BorderPane) txtNome.getScene().getRoot();
            Parent tela = FXMLLoader.load(getClass().getResource("/view/HomeView.fxml"));
            mainPane.setCenter(tela);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}