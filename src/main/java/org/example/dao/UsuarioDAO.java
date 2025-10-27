package org.example.dao;

import org.bytedeco.opencv.opencv_core.Mat;
import org.example.service.BiometriaService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UsuarioDAO {

    private BiometriaService biometriaService;

    public UsuarioDAO() {
        this.biometriaService = new BiometriaService();
    }

    /**
     * Salva um novo usuário no banco de dados, incluindo sua biometria.
     */
    public boolean cadastrarUsuario(String nome, String usuario, int nivelAcesso, Mat descritores) {
        String sql = "INSERT INTO usuarios(nome_completo, usuario, nivel_acesso, " +
                "biometria_dados, biometria_rows, biometria_cols, biometria_type) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Converte o Mat para bytes
            byte[] biometriaBytes = biometriaService.converterMatParaBytes(descritores);

            // Define os parâmetros do PreparedStatement
            pstmt.setString(1, nome);
            pstmt.setString(2, usuario);
            pstmt.setInt(3, nivelAcesso);
            pstmt.setBytes(4, biometriaBytes);
            pstmt.setInt(5, descritores.rows()); // Salva metadados: linhas
            pstmt.setInt(6, descritores.cols()); // Salva metadados: colunas
            pstmt.setInt(7, descritores.type()); // Salva metadados: tipo

            // Executa o INSERT
            pstmt.executeUpdate();
            return true; // Sucesso

        } catch (SQLException e) {
            // Trata erros comuns, como "usuário já existe"
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Erro: O nome de usuário '" + usuario + "' já existe.");
            } else {
                e.printStackTrace();
            }
            return false; // Falha
        }
    }
}