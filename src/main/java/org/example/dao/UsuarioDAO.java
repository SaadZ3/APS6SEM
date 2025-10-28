package org.example.dao;

import org.bytedeco.opencv.opencv_core.Mat;
import org.example.service.BiometriaService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.example.model.Propriedade;
import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;

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

    /**
     * Busca os dados de biometria e nível de acesso de um usuário.
     */
    public org.example.model.UsuarioBiometria getBiometriaPorUsuario(String usuario) {
        String sql = "SELECT nivel_acesso, biometria_dados, biometria_rows, biometria_cols, biometria_type "
                + "FROM usuarios WHERE usuario = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            java.sql.ResultSet rs = pstmt.executeQuery();

            // Verifica se o usuário foi encontrado
            if (rs.next()) {
                // Lê os dados do ResultSet
                int nivelAcesso = rs.getInt("nivel_acesso");
                byte[] dados = rs.getBytes("biometria_dados");
                int rows = rs.getInt("biometria_rows");
                int cols = rs.getInt("biometria_cols");
                int type = rs.getInt("biometria_type");

                // Retorna o objeto com todos os dados
                return new org.example.model.UsuarioBiometria(dados, rows, cols, type, nivelAcesso);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Retorna nulo se o usuário não for encontrado ou se der erro
        return null;
    }


    /**
        * Busca as propriedades que o usuário tem permissão para ver.
        * A lógica é: um usuário de nível N pode ver todos os dados de nível <= N.
     */
    public List<Propriedade> getPropriedadesPorNivel(int nivelUsuario) {
        List<Propriedade> propriedades = new ArrayList<>();
        String sql = "SELECT nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario "
                + "FROM propriedades_rurais WHERE nivel_acesso_necessario <= ? "
                + "ORDER BY nivel_acesso_necessario";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, nivelUsuario);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Propriedade p = new Propriedade(
                        rs.getString("nome_propriedade"),
                        rs.getString("agrotoxico_utilizado"),
                        rs.getString("impacto_ambiental"),
                        rs.getInt("nivel_acesso_necessario")
                );
                propriedades.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return propriedades;
    }
}