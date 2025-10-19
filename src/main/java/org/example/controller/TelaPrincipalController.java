package org.example.controller;

import org.example.service.BiometriaService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TelaPrincipalController {

    @FXML
    private Button btnIniciarCamera;

    @FXML
    void iniciarCamera(ActionEvent event) {
        System.out.println("Botão Iniciar Câmera clicado!");
        BiometriaService biometriaService = new BiometriaService();

        // Para não travar a interface, é ideal rodar isso em uma nova Thread
        new Thread(() -> biometriaService.iniciarCapturaWebcam()).start();
    }
}