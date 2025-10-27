package org.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    private static final String URL = "jdbc:sqlite:biometria.db";

    // Método para criar a conexão com o banco
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para criar a tabela de usuários
    public static void criarTabelas() {
        // ATENÇÃO: A tabela foi atualizada para guardar os metadados da biometria
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nome_completo TEXT NOT NULL,"
                + " usuario TEXT NOT NULL UNIQUE,"
                + " nivel_acesso INTEGER NOT NULL,"
                + " biometria_dados BLOB NOT NULL," // Os bytes dos descritores
                + " biometria_rows INTEGER NOT NULL," // Metadados: linhas
                + " biometria_cols INTEGER NOT NULL," // Metadados: colunas
                + " biometria_type INTEGER NOT NULL"  // Metadados: tipo (ex: CV_8U)
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Banco de dados e tabelas verificados com sucesso.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}