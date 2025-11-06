package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button; // Importe
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView; // Importe
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.bytedeco.opencv.opencv_core.Mat;
import org.example.dao.UsuarioDAO;
import org.example.service.BiometriaService;
import org.example.util.AlertUtils;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class CadastroController implements Initializable {

    // --- NOVOS COMPONENTES FXML ---
    @FXML private ComboBox<String> cmbTipoBiometria;
    @FXML private VBox boxDigital;
    @FXML private VBox boxRosto;
    @FXML private Label lblCaminhoImagemRosto;
    @FXML private ImageView imgWebcamRosto; // Novo
    @FXML private Button btnLigarCamera;    // Novo
    @FXML private Button btnCapturarRosto;  // Novo
    @FXML private Button btnCadastrar;      // Já existia

    // --- COMPONENTES ANTIGOS ---
    @FXML private ComboBox<Integer> cmbNivelAcesso;
    @FXML private Label lblCaminhoImagem;
    @FXML private TextField txtNome;
    @FXML private TextField txtUsuario;

    // --- ARQUIVOS E SERVIÇOS ---
    private File arquivoImagemDigital;
    private File arquivoImagemRosto; // Permanece como File
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
        // Para a webcam, precisamos parar o stream se o campo for ocultado
        if (tipo == null || (!tipo.equals(TIPO_ROSTO) && !tipo.equals(TIPO_AMBOS))) {
            boxRosto.setVisible(false); boxRosto.setManaged(false);
            biometriaService.stopStreamWebcam(); // Garante que a câmera pare
            btnLigarCamera.setDisable(false);
            btnCapturarRosto.setDisable(true);
        } else {
            boxRosto.setVisible(true); boxRosto.setManaged(true);
        }

        boolean mostrarDigital = tipo != null && (tipo.equals(TIPO_DIGITAL) || tipo.equals(TIPO_AMBOS));
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
        // Define um caminho temporário para a imagem
        String caminhoTemp = "temp_face_capture.jpg";

        // Captura o frame e salva no arquivo
        arquivoImagemRosto = biometriaService.capturarFrameEGravar(caminhoTemp);

        if (arquivoImagemRosto != null) {
            lblCaminhoImagemRosto.setText("Rosto capturado: " + arquivoImagemRosto.getName());
            AlertUtils.showSuccessAlert("Rosto capturado com sucesso!");
        } else {
            AlertUtils.showErrorAlert("Não foi possível capturar o rosto.");
        }

        // Para o stream após a captura
        biometriaService.stopStreamWebcam();
        btnLigarCamera.setDisable(false);
        btnCapturarRosto.setDisable(true);
    }

    // --- LÓGICA DE CADASTRO (QUASE IDÊNTICA) ---

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

        // Validação da Digital
        if ((tipoBiometria.equals(TIPO_DIGITAL) || tipoBiometria.equals(TIPO_AMBOS)) && arquivoImagemDigital == null) {
            AlertUtils.showErrorAlert("Nenhuma imagem de digital foi selecionada.");
            return;
        }
        // Validação do Rosto
        if ((tipoBiometria.equals(TIPO_ROSTO) || tipoBiometria.equals(TIPO_AMBOS)) && arquivoImagemRosto == null) {
            AlertUtils.showErrorAlert("Nenhum rosto foi capturado.");
            return;
        }

        // --- CHECKPOINT ---
        // A lógica de salvar no banco de dados (próximo passo) virá aqui.
        // Por enquanto, vamos testar a captura.

        try {
            // Lógica de cadastro (será atualizada no próximo passo)
            // Por enquanto, só testamos se os arquivos existem
            if (arquivoImagemDigital != null) {
                System.out.println("Processando digital: " + arquivoImagemDigital.getName());
                // Mat descDigital = biometriaService.extrairRecursos(arquivoImagemDigital);
            }
            if (arquivoImagemRosto != null) {
                System.out.println("Processando rosto: " + arquivoImagemRosto.getName());
                // Mat descRosto = biometriaService.extrairRecursos(arquivoImagemRosto);
            }

            // NÃO SALVA NO BANCO AINDA (para o checkpoint)
            AlertUtils.showSuccessAlert("Checkpoint: Captura de biometria (rosto/digital) pronta! (Nada foi salvo ainda).");
            limparCampos();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Ocorreu um erro inesperado: " + e.getMessage());
        }
    }

    private void limparCampos() {
        // ... (código de limpeza igual ao da resposta anterior)
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
        imgWebcamRosto.setImage(null); // Limpa o ImageView

        atualizarVisibilidadeCampos(null);
    }
}