package org.example.model;

/**
 * Um Record (classe de dados) simples para guardar
 * as informações de biometria e acesso vindas do banco.
 */
public record UsuarioBiometria(
        byte[] dados,
        int rows,
        int cols,
        int type,
        int nivelAcesso
) {
}