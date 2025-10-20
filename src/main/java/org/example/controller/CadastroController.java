package org.example.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CadastroController implements Initializable {

    @FXML
    private Button btnCadastrar;

    @FXML
    private Button btnSelecionarImagem;

    @FXML
    private Label lblCaminhoImagem;

    // A ComboBox para o nível de acesso. Note o tipo <Integer>.
    @FXML
    private ComboBox<Integer> cmbNivelAcesso;

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtUsuario;

    /**
     * Este método é chamado automaticamente depois que o FXML é carregado.
     * É o lugar perfeito para configurar os componentes.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Adiciona os números 1, 2 e 3 como opções na ComboBox
        cmbNivelAcesso.setItems(FXCollections.observableArrayList(1, 2, 3));
    }

    @FXML
    void cadastrar(ActionEvent event) {
        // Exemplo de como pegar os valores dos campos
        String nome = txtNome.getText();
        String usuario = txtUsuario.getText();
        Integer nivelAcesso = cmbNivelAcesso.getValue(); // Pega o valor selecionado

        // Validação simples
        if (nome.isEmpty() || usuario.isEmpty() || nivelAcesso == null) {
            System.out.println("Erro: Todos os campos devem ser preenchidos.");
            // No futuro, mostre um alerta para o usuário aqui
            return;
        }

        System.out.println("Cadastrando usuário:");
        System.out.println("Nome: " + nome);
        System.out.println("Usuário: " + usuario);
        System.out.println("Nível de Acesso: " + nivelAcesso);

        // Chamar a lógica de cadastro no service aqui...
    }

    @FXML
    void selecionarImagem(ActionEvent event) {
        System.out.println("Lógica para abrir seletor de arquivo aqui...");
    }
}