package org.example.model;

/**
 * Representa os dados da tabela propriedades_rurais.
 * Usaremos Propriedades JavaFX para ligar com a TableView.
 */
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Propriedade {

    private final StringProperty nomePropriedade;
    private final StringProperty agrotoxicoUtilizado;
    private final StringProperty impactoAmbiental;
    private final IntegerProperty nivelAcesso;

    public Propriedade(String nome, String agrotoxico, String impacto, int nivel) {
        this.nomePropriedade = new SimpleStringProperty(nome);
        this.agrotoxicoUtilizado = new SimpleStringProperty(agrotoxico);
        this.impactoAmbiental = new SimpleStringProperty(impacto);
        this.nivelAcesso = new SimpleIntegerProperty(nivel);
    }

    // Getters
    public String getNomePropriedade() { return nomePropriedade.get(); }
    public String getAgrotoxicoUtilizado() { return agrotoxicoUtilizado.get(); }
    public String getImpactoAmbiental() { return impactoAmbiental.get(); }
    public int getNivelAcesso() { return nivelAcesso.get(); }

    // Property Getters (para o JavaFX)
    public StringProperty nomePropriedadeProperty() { return nomePropriedade; }
    public StringProperty agrotoxicoUtilizadoProperty() { return agrotoxicoUtilizado; }
    public StringProperty impactoAmbientalProperty() { return impactoAmbiental; }
    public IntegerProperty nivelAcessoProperty() { return nivelAcesso; }
}