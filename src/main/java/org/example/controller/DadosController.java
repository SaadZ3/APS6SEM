package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.dao.UsuarioDAO;
import org.example.model.Propriedade;

import java.util.List;

public class DadosController {

    @FXML private Label lblTitulo;
    @FXML private TableView<Propriedade> tabelaPropriedades;
    @FXML private TableColumn<Propriedade, String> colPropriedade;
    @FXML private TableColumn<Propriedade, String> colAgrotoxico;
    @FXML private TableColumn<Propriedade, String> colImpacto;
    @FXML private TableColumn<Propriedade, Number> colNivel;

    private UsuarioDAO usuarioDAO;

    @FXML
    public void initialize() {
        // Inicializa o DAO
        this.usuarioDAO = new UsuarioDAO();

        // Configura as colunas da tabela para "ler" as propriedades do modelo
        colPropriedade.setCellValueFactory(cellData -> cellData.getValue().nomePropriedadeProperty());
        colAgrotoxico.setCellValueFactory(cellData -> cellData.getValue().agrotoxicoUtilizadoProperty());
        colImpacto.setCellValueFactory(cellData -> cellData.getValue().impactoAmbientalProperty());
        colNivel.setCellValueFactory(cellData -> cellData.getValue().nivelAcessoProperty());
    }

    /**
     * Este método é chamado pelo VerificacaoController após o login.
     * Ele recebe o nível de acesso do usuário e carrega os dados.
     */
    public void carregarDados(int nivelAcessoUsuario) {
        // Atualiza o título da tela
        lblTitulo.setText("Dados Estratégicos (Seu Nível: " + nivelAcessoUsuario + ")");

        // Busca os dados no banco
        List<Propriedade> propriedades = usuarioDAO.getPropriedadesPorNivel(nivelAcessoUsuario);

        // Popula a tabela
        tabelaPropriedades.setItems(FXCollections.observableArrayList(propriedades));
    }
}