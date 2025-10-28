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

    // Função para criar tabelas
    public static void criarTabelas() {
        // SQL para criar a tabela de usuários (você já tem)
        String sqlUsuarios = "CREATE TABLE IF NOT EXISTS usuarios ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nome_completo TEXT NOT NULL,"
                + " usuario TEXT NOT NULL UNIQUE,"
                + " nivel_acesso INTEGER NOT NULL,"
                + " biometria_dados BLOB NOT NULL,"
                + " biometria_rows INTEGER NOT NULL,"
                + " biometria_cols INTEGER NOT NULL,"
                + " biometria_type INTEGER NOT NULL"
                + ");";

        // SQL para a NOVA tabela de propriedades
        String sqlPropriedades = "CREATE TABLE IF NOT EXISTS propriedades_rurais ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nome_propriedade TEXT NOT NULL,"
                + " agrotoxico_utilizado TEXT NOT NULL,"
                + " impacto_ambiental TEXT,"
                + " nivel_acesso_necessario INTEGER NOT NULL" // Nível 1, 2 ou 3
                + ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Executa a criação das duas tabelas
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlPropriedades);
            System.out.println("Banco de dados e tabelas verificados com sucesso.");

            // Popula os dados iniciais
            popularDadosIniciais(conn);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para popular a tabela com dados fictícios
    private static void popularDadosIniciais(Connection conn) throws SQLException {
        // Verifica se a tabela já tem dados para não inserir de novo
        String sqlCheck = "SELECT COUNT(*) FROM propriedades_rurais";
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sqlCheck)) {
            if (rs.getInt(1) == 0) {
                System.out.println("Populando tabela 'propriedades_rurais' com dados iniciais...");
                Statement insertStmt = conn.createStatement();

                // Nível 1 (Público)
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Boa Esperança', 'Glifosato', 'Monitorado', 1)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Sítio Água Limpa', '2,4-D', 'Baixo', 1)");

                // Nível 2 (Restrito - Diretores)
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Rio Turvo', 'Atrazina', 'Alto Risco - Lençóis Freáticos', 2)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Grupo Veredas', 'Metolacloro', 'Médio Risco - Contaminação de Rio', 2)");

                // Nível 3 (Secreto - Ministro)
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Complexo Agro S.A.', 'Endossulfan (Proibido)', 'Dano Crítico - Bacia Hidrográfica', 3)");

                System.out.println("Dados iniciais inseridos.");
            }
        }
    }
}