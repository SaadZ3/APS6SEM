package org.example.util;

import javafx.scene.control.Alert;

public class AlertUtils {

    public static void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showSuccessAlert(String message) {
        showAlert("Sucesso", null, message, Alert.AlertType.INFORMATION);
    }

    public static void showErrorAlert(String message) {
        showAlert("Erro", null, message, Alert.AlertType.ERROR);
    }
}