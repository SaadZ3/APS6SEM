package org.example.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable; // Importe
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bytedeco.opencv.opencv_core.Mat;
import org.example.dao.UsuarioDAO;
import org.example.model.UsuarioBiometria;
import org.example.service.BiometriaService;
import org.example.util.AlertUtils; // Importe

import java.io.File;
import java.net.URL; // Importe
import java.util.ResourceBundle; // Importe

public class VerificacaoController implements Initializable {

    @FXML private Label lblCaminhoImagem;
    @FXML private TextField txtUsuario;

    // Variável para armazenar o arquivo de imagem selecionado
    private File arquivoImagemSelecionada;

    // Nossos serviços
    private BiometriaService biometriaService;
    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa os serviços
        this.biometriaService = new BiometriaService();
        this.usuarioDAO = new UsuarioDAO();
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

    @FXML
    void verificar(ActionEvent event) {
        // 1. Obter os dados da interface
        String usuario = txtUsuario.getText();

        // 2. Validação de campos
        if (usuario.isEmpty()) {
            AlertUtils.showErrorAlert("O campo 'usuário' deve ser preenchido.");
            return;
        }
        if (arquivoImagemSelecionada == null) {
            AlertUtils.showErrorAlert("Nenhuma imagem de biometria foi selecionada.");
            return;
        }

        try {
            // 3. Buscar o usuário no banco
            UsuarioBiometria biometriaSalva = usuarioDAO.getBiometriaPorUsuario(usuario);

            if (biometriaSalva == null) {
                AlertUtils.showErrorAlert("Usuário '" + usuario + "' não encontrado.");
                return;
            }

            // 4. Extrair recursos da nova imagem (a imagem de login)
            System.out.println("Extraindo recursos da imagem de login...");
            Mat descritoresNovos = biometriaService.extrairRecursos(arquivoImagemSelecionada);

            // 5. Reconstruir o Mat da biometria salva no banco
            System.out.println("Reconstruindo biometria do banco...");
            Mat descritoresSalvos = biometriaService.converterBytesParaMat(
                    biometriaSalva.dados(),
                    biometriaSalva.rows(),
                    biometriaSalva.cols(),
                    biometriaSalva.type()
            );

            // 6. Comparar as duas biometrias
            System.out.println("Comparando biometrias...");
            boolean acessoPermitido = biometriaService.compararBiometria(descritoresSalvos, descritoresNovos);

            // 7. Dar feedback final
            if (acessoPermitido) {
                String mensagem = "Acesso Permitido! Nível de acesso: " + biometriaSalva.nivelAcesso();
                AlertUtils.showSuccessAlert(mensagem);

                // Aqui você pode adicionar a lógica para
                // o que acontece após o login, de acordo com o nível.
                // Ex: carregarTelasRestritas(biometriaSalva.nivelAcesso());

            } else {
                AlertUtils.showErrorAlert("Acesso Negado. A biometria não confere.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showErrorAlert("Ocorreu um erro inesperado na verificação: " + e.getMessage());
        }
    }
}