package org.example.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    private static final String URL = "jdbc:sqlite:biometria.db";

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metodo para criar as tabelas
    public static void criarTabelas() {
        // Adicionamos colunas para biometria facial e permitimos que sejam NULL
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nome_completo TEXT NOT NULL,"
                + " usuario TEXT NOT NULL UNIQUE,"
                + " nivel_acesso INTEGER NOT NULL,"
                + " biometria_dados BLOB NULL," // Digital
                + " biometria_rows INTEGER NULL,"
                + " biometria_cols INTEGER NULL,"
                + " biometria_type INTEGER NULL,"
                + " face_dados BLOB NULL," // Rosto
                + " face_rows INTEGER NULL,"
                + " face_cols INTEGER NULL,"
                + " face_type INTEGER NULL"
                + ");";

        // SQL para a tabela de propriedades
        String sqlPropriedades = "CREATE TABLE IF NOT EXISTS propriedades_rurais ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nome_propriedade TEXT NOT NULL,"
                + " agrotoxico_utilizado TEXT NOT NULL,"
                + " impacto_ambiental TEXT,"
                + " nivel_acesso_necessario INTEGER NOT NULL"
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlPropriedades);
            System.out.println("Banco de dados e tabelas (com suporte a face) verificados.");

            popularDadosIniciais(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para popular a tabela com dados fictícios (sem alteração)
    private static void popularDadosIniciais(Connection conn) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM propriedades_rurais";
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sqlCheck)) {
            if (rs.getInt(1) == 0) {
                System.out.println("Populando tabela 'propriedades_rurais' com dados iniciais...");
                Statement insertStmt = conn.createStatement();

                // Nível 1
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Boa Esperança', 'Glifosato', 'Monitorado', 1)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Sítio Água Limpa', '2,4-D', 'Baixo', 1)");

                // Nível 2
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Rio Turvo', 'Atrazina', 'Alto Risco - Lençóis Freáticos', 2)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Grupo Veredas', 'Metolacloro', 'Médio Risco - Contaminação de Rio', 2)");

                // Nível 3
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Complexo Agro S.A.', 'Endossulfan (Proibido)', 'Dano Crítico - Bacia Hidrográfica', 3)");

                System.out.println("Dados iniciais inseridos.");
            }
        }
    }
}