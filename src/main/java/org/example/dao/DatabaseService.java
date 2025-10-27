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

    // Método para criar a tabela de usuários (chame isso na inicialização)
    public static void criarTabelas() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nome_completo TEXT NOT NULL,"
                + " usuario TEXT NOT NULL UNIQUE,"
                + " nivel_acesso INTEGER NOT NULL,"
                + " biometria BLOB NOT NULL" // BLOB é usado para armazenar dados binários (nossa imagem processada)
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}