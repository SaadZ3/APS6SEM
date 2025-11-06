package org.example.model;

import org.bytedeco.opencv.opencv_core.Mat;

/**
 * Um Record (classe de dados) simples para guardar
 * as informações de biometria e acesso vindas do banco.
 * (Atualizado para carregar Mat da digital e do rosto)
 */
public record UsuarioBiometria(
        Mat digital,
        Mat rosto,
        int nivelAcesso
) {
}