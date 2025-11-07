package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Importe
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView; // Importe
import javafx.scene.layout.VBox; // Importe
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.example.dao.UsuarioDAO;
import org.example.model.UsuarioBiometria;
import org.example.service.BiometriaService;
import org.example.util.AlertUtils; // Importe

import java.io.File;
import java.io.IOException; // Importe
import java.net.URL; // Importe
import java.util.ResourceBundle; // Importe

import javafx.fxml.FXMLLoader; // Importe
import javafx.scene.Parent; // Importe
import javafx.scene.layout.BorderPane; // Importe

public class VerificacaoController implements Initializable {

    // --- NOVOS COMPONENTES FXML ---
    @FXML private ComboBox<String> cmbTipoBiometria;
    @FXML private VBox boxDigital;
    @FXML private VBox boxRosto;
    @FXML private Label lblCaminhoImagemRosto;
    @FXML private ImageView imgWebcamRosto;
    @FXML private Button btnLigarCamera;
    @FXML private Button btnCapturarRosto;
    @FXML private Button btnVerificar;

    // --- COMPONENTES ANTIGOS ---
    @FXML private Label lblCaminhoImagem;
    @FXML private TextField txtUsuario;

    // --- ARQUIVOS E SERVIÇOS ---
    private File arquivoImagemDigital;
    private File arquivoImagemRosto;
    private BiometriaService biometriaService;
    private UsuarioDAO usuarioDAO;

    private final String TIPO_DIGITAL = "Impressão Digital";
    private final String TIPO_ROSTO = "Rosto";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Popula o ComboBox
        cmbTipoBiometria.setItems(FXCollections.observableArrayList(TIPO_DIGITAL, TIPO_ROSTO));

        // Inicializa os serviços
        this.biometriaService = new BiometriaService();
        this.usuarioDAO = new UsuarioDAO();

        // Adiciona listener para mostrar/ocultar campos
        cmbTipoBiometria.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> atualizarVisibilidadeCampos(newVal)
        );
    }

    private void atualizarVisibilidadeCampos(String tipo) {
        // Lógica para Rosto/Webcam
        if (tipo == null || !tipo.equals(TIPO_ROSTO)) {
            boxRosto.setVisible(false); boxRosto.setManaged(false);
            biometriaService.stopStreamWebcam();
            btnLigarCamera.setDisable(false);
            btnCapturarRosto.setDisable(true);
        } else {
            boxRosto.setVisible(true); boxRosto.setManaged(true);
        }

        // Lógica para Digital
        boolean mostrarDigital = tipo != null && tipo.equals(TIPO_DIGITAL);
        boxDigital.setVisible(mostrarDigital); boxDigital.setManaged(mostrarDigital);
    }

    // --- LÓGICA DA WEBCAM ---

    @FXML
    void ligarCamera(ActionEvent event) {
        biometriaService.iniciarStreamWebcam(imgWebcamRosto);
        btnLigarCamera.setDisable(true);
        btnCapturarRosto.setDisable(false);
    }

    @FXML
    void capturarRosto(ActionEvent event) {
        String caminhoTemp = "temp_face_verify.jpg";
        arquivoImagemRosto = biometriaService.capturarFrameEGravar(caminhoTemp);

        if (arquivoImagemRosto != null) {
            lblCaminhoImagemRosto.setText("Rosto capturado: " + arquivoImagemRosto.getName());
            AlertUtils.showSuccessAlert("Rosto capturado!");
        } else {
            AlertUtils.showErrorAlert("Não foi possível capturar o rosto.");
        }

        biometriaService.stopStreamWebcam();
        btnLigarCamera.setDisable(false);
        btnCapturarRosto.setDisable(true);
    }

    // --- LÓGICA DE VERIFICAÇÃO ---

    @FXML
    void selecionarImagemDigital(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagem da Biometria");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.tif")
        );
        Stage stage = (Stage) lblCaminhoImagem.getScene().getWindow();
        arquivoImagemDigital = fileChooser.showOpenDialog(stage);

        if (arquivoImagemDigital != null) {
            lblCaminhoImagem.setText(arquivoImagemDigital.getName());
        }
    }

    @FXML
    void verificar(ActionEvent event) {
        // 1. Obter os dados da interface
        String usuario = txtUsuario.getText();
        String tipoBiometria = cmbTipoBiometria.getValue();

        // 2. Validação de campos
        if (usuario.isEmpty() || tipoBiometria == null) {
            AlertUtils.showErrorAlert("Usuário e tipo de verificação devem ser preenchidos.");
            return;
        }

        Mat descritoresNovos = null; // Armazena a biometria para verificação
        boolean acessoPermitido = false;

        try {
            // 3. Buscar o usuário e sua biometria salva no banco
            UsuarioBiometria biometriaSalva = usuarioDAO.getBiometriaPorUsuario(usuario);

            if (biometriaSalva == null) {
                AlertUtils.showErrorAlert("Usuário '" + usuario + "' não encontrado.");
                return;
            }

            // 4. Processar a biometria de login (Digital ou Rosto)
            if (tipoBiometria.equals(TIPO_DIGITAL)) {
                if (arquivoImagemDigital == null) {
                    AlertUtils.showErrorAlert("Selecione a imagem da digital.");
                    return;
                }
                if (biometriaSalva.digital() == null) {
                    AlertUtils.showErrorAlert("Este usuário não possui biometria digital cadastrada.");
                    return;
                }

                descritoresNovos = biometriaService.extrairRecursosDigital(arquivoImagemDigital);
                acessoPermitido = biometriaService.compararBiometria(biometriaSalva.digital(), descritoresNovos);

            } else if (tipoBiometria.equals(TIPO_ROSTO)) {
                if (arquivoImagemRosto == null) {
                    AlertUtils.showErrorAlert("Capture o rosto pela webcam.");
                    return;
                }
                if (biometriaSalva.rosto() == null) {
                    AlertUtils.showErrorAlert("Este usuário não possui biometria facial cadastrada.");
                    return;
                }

                descritoresNovos = biometriaService.extrairRecursosRosto(arquivoImagemRosto);
                if(descritoresNovos.empty()){
                    AlertUtils.showErrorAlert("Não foi possível detectar um rosto na captura. Tente novamente.");
                    return;
                }
                acessoPermitido = biometriaService.compararBiometria(biometriaSalva.rosto(), descritoresNovos);
            }

            // 5. Dar feedback final
            if (acessoPermitido) {
                AlertUtils.showSuccessAlert("Acesso Permitido! Carregando dados...");
                carregarTelaDeDados(biometriaSalva.nivelAcesso());
            } else {
                AlertUtils.showErrorAlert("Acesso Negado. A biometria não confere.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Ocorreu um erro inesperado na verificação: " + e.getMessage());
        }
    }

    /**
     * Carrega a tela de dados restritos após o login.
     */
    private void carregarTelaDeDados(int nivelAcesso) throws IOException {
        // Encontra o BorderPane principal subindo na hierarquia
        BorderPane mainPane = (BorderPane) txtUsuario.getScene().getRoot();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DadosView.fxml"));
        Parent dadosView = loader.load();
        DadosController dadosController = loader.getController();
        dadosController.carregarDados(nivelAcesso);
        mainPane.setCenter(dadosView);
    }

    @FXML
    void voltarParaHome(ActionEvent event) {
        // Para a câmera, se estiver ligada
        biometriaService.stopStreamWebcam();

        try {
            BorderPane mainPane = (BorderPane) txtUsuario.getScene().getRoot();
            Parent tela = FXMLLoader.load(getClass().getResource("/view/HomeView.fxml"));
            mainPane.setCenter(tela);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}