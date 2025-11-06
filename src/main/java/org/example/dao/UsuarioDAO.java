package org.example.dao;

import org.bytedeco.opencv.opencv_core.Mat;
import org.example.model.Propriedade;
import org.example.model.UsuarioBiometria;
import org.example.service.BiometriaService;

import java.sql.*; // Importe
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private BiometriaService biometriaService;

    public UsuarioDAO() {
        this.biometriaService = new BiometriaService();
    }

    /**
     * Salva um novo usuário, aceitando biometria digital e/ou facial.
     */
    public boolean cadastrarUsuario(String nome, String usuario, int nivelAcesso, Mat descritoresDigital, Mat descritoresRosto) {
        String sql = "INSERT INTO usuarios(nome_completo, usuario, nivel_acesso, " +
                "biometria_dados, biometria_rows, biometria_cols, biometria_type, " +
                "face_dados, face_rows, face_cols, face_type) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nome);
            pstmt.setString(2, usuario);
            pstmt.setInt(3, nivelAcesso);

            // --- Lógica para Biometria Digital (pode ser nula) ---
            if (descritoresDigital != null && !descritoresDigital.empty()) {
                byte[] biometriaBytes = biometriaService.converterMatParaBytes(descritoresDigital);
                pstmt.setBytes(4, biometriaBytes);
                pstmt.setInt(5, descritoresDigital.rows());
                pstmt.setInt(6, descritoresDigital.cols());
                pstmt.setInt(7, descritoresDigital.type());
            } else {
                pstmt.setNull(4, Types.BLOB);
                pstmt.setNull(5, Types.INTEGER);
                pstmt.setNull(6, Types.INTEGER);
                pstmt.setNull(7, Types.INTEGER);
            }

            // --- Lógica para Biometria Facial (pode ser nula) ---
            if (descritoresRosto != null && !descritoresRosto.empty()) {
                byte[] faceBytes = biometriaService.converterMatParaBytes(descritoresRosto);
                pstmt.setBytes(8, faceBytes);
                pstmt.setInt(9, descritoresRosto.rows());
                pstmt.setInt(10, descritoresRosto.cols());
                pstmt.setInt(11, descritoresRosto.type());
            } else {
                pstmt.setNull(8, Types.BLOB);
                pstmt.setNull(9, Types.INTEGER);
                pstmt.setNull(10, Types.INTEGER);
                pstmt.setNull(11, Types.INTEGER);
            }

            pstmt.executeUpdate();
            return true; // Sucesso

        } catch (SQLException e) {
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
     * (ATUALIZADO para buscar dados faciais também)
     */
    public UsuarioBiometria getBiometriaPorUsuario(String usuario) {
        // Pede todas as colunas de biometria (digital E facial)
        String sql = "SELECT * FROM usuarios WHERE usuario = ?";

        try (Connection conn = DatabaseService.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Lê nível de acesso
                int nivelAcesso = rs.getInt("nivel_acesso");

                // Lê dados da digital
                byte[] dadosDigital = rs.getBytes("biometria_dados");
                int rowsDigital = rs.getInt("biometria_rows");
                int colsDigital = rs.getInt("biometria_cols");
                int typeDigital = rs.getInt("biometria_type");

                // Lê dados do rosto
                byte[] dadosRosto = rs.getBytes("face_dados");
                int rowsRosto = rs.getInt("face_rows");
                int colsRosto = rs.getInt("face_cols");
                int typeRosto = rs.getInt("face_type");

                // Recria os Mats (eles serão nulos se não houver dados no banco)
                Mat matDigital = (dadosDigital != null) ?
                        biometriaService.converterBytesParaMat(dadosDigital, rowsDigital, colsDigital, typeDigital) : null;

                Mat matRosto = (dadosRosto != null) ?
                        biometriaService.converterBytesParaMat(dadosRosto, rowsRosto, colsRosto, typeRosto) : null;

                // Retorna o objeto com todos os dados
                return new UsuarioBiometria(matDigital, matRosto, nivelAcesso);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // Usuário não encontrado
    }

    /**
     * Busca as propriedades que o usuário tem permissão para ver.
     * (Sem alteração)
     */
    public List<Propriedade> getPropriedadesPorNivel(int nivelUsuario) {
        // ...código idêntico ao anterior...
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
        } catch (SQLException e) { e.printStackTrace(); }
        return propriedades;
    }
}