package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent; // Importe
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Importe
import javafx.scene.Parent; // Importe
import javafx.scene.control.Button; // Importe
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane; // Importe
import org.example.dao.UsuarioDAO;
import org.example.model.Propriedade;

import java.io.IOException; // Importe
import java.util.List;

public class DadosController {

    @FXML private Label lblTitulo;
    @FXML private TableView<Propriedade> tabelaPropriedades;
    @FXML private TableColumn<Propriedade, String> colPropriedade;
    @FXML private TableColumn<Propriedade, String> colAgrotoxico;
    @FXML private TableColumn<Propriedade, String> colImpacto;
    @FXML private TableColumn<Propriedade, Number> colNivel;
    @FXML private Button btnVoltar; // Novo

    private UsuarioDAO usuarioDAO;

    @FXML
    public void initialize() {
        this.usuarioDAO = new UsuarioDAO();
        colPropriedade.setCellValueFactory(cellData -> cellData.getValue().nomePropriedadeProperty());
        colAgrotoxico.setCellValueFactory(cellData -> cellData.getValue().agrotoxicoUtilizadoProperty());
        colImpacto.setCellValueFactory(cellData -> cellData.getValue().impactoAmbientalProperty());
        colNivel.setCellValueFactory(cellData -> cellData.getValue().nivelAcessoProperty());
    }

    public void carregarDados(int nivelAcessoUsuario) {
        lblTitulo.setText("Dados Estratégicos (Seu Nível: " + nivelAcessoUsuario + ")");
        List<Propriedade> propriedades = usuarioDAO.getPropriedadesPorNivel(nivelAcessoUsuario);
        tabelaPropriedades.setItems(FXCollections.observableArrayList(propriedades));
    }

    // --- NOVO MÉTODO "VOLTAR" ---
    @FXML
    void voltarParaHome(ActionEvent event) {
        try {
            BorderPane mainPane = (BorderPane) btnVoltar.getScene().getRoot();
            Parent tela = FXMLLoader.load(getClass().getResource("/view/HomeView.fxml"));
            mainPane.setCenter(tela);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}