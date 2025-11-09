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

    // Método para criar as tabelas (sem alteração)
    public static void criarTabelas() {
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

            popularDadosIniciais(conn); // Chama o método atualizado

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODO ATUALIZADO COM MAIS DADOS ---
    private static void popularDadosIniciais(Connection conn) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM propriedades_rurais";
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sqlCheck)) {
            // Só insere os dados se a tabela estiver vazia
            if (rs.getInt(1) == 0) {
                System.out.println("Populando tabela 'propriedades_rurais' com dados iniciais...");
                Statement insertStmt = conn.createStatement();

                // Nível 1 (Público - 5 itens)
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Boa Esperança', 'Glifosato', 'Monitorado', 1)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Sítio Água Limpa', '2,4-D', 'Baixo', 1)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Chácara Ipê Amarelo', 'Piretroides', 'Baixo (Uso Doméstico)', 1)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Alvorada', 'Glifosato', 'Monitorado', 1)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Sítio das Flores', 'Óleo de Neem (Orgânico)', 'Nenhum', 1)");

                // Nível 2 (Restrito - Diretores - 5 itens)
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Rio Turvo', 'Atrazina', 'Alto Risco - Lençóis Freáticos', 2)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Grupo Veredas', 'Metolacloro', 'Médio Risco - Contaminação de Rio', 2)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Agropecuária Matão', 'Atrazina', 'Alto Risco - Lençóis Freáticos', 2)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Santa Clara', 'Clorpirifós', 'Médio Risco - Fauna Local', 2)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Propriedade Córrego Fundo', 'Diuron', 'Alto Risco - Contaminação de Rio', 2)");


                // Nível 3 (Secreto - Ministro - 5 itens)
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Complexo Agro S.A.', 'Endossulfan (Proibido)', 'Dano Crítico - Bacia Hidrográfica', 3)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Reunidas Delta', 'Aldicarbe (Proibido)', 'Dano Crítico - Mortandade de Peixes', 3)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Grupo Terra Forte', 'Paraquate (Proibido)', 'Dano Crítico - Intoxicação de Aplicadores', 3)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Fazenda Três Lagoas', 'Carbofurano (Proibido)', 'Dano Crítico - Mortandade de Aves', 3)");
                insertStmt.execute("INSERT INTO propriedades_rurais (nome_propriedade, agrotoxico_utilizado, impacto_ambiental, nivel_acesso_necessario) " +
                        "VALUES ('Exportadora Vale do Sol', 'Monocrotofós (Proibido)', 'Dano Crítico - Contaminação de Lençol', 3)");


                System.out.println("Dados iniciais (15 registros) inseridos.");
            }
        }
    }
}