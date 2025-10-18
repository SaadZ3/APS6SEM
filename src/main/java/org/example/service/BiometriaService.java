package org.example.service;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.Frame;
import javax.swing.WindowConstants;

public class BiometriaService {

    public void iniciarCapturaWebcam() {
        try {
            // Inicia o dispositivo da câmera
            OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
            grabber.start();

            // Cria uma janela para exibir a imagem
            CanvasFrame canvas = new CanvasFrame("Webcam");
            canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Loop para capturar e exibir frames continuamente
            while (canvas.isVisible() && (grabber.grab()) != null) {
                Frame frame = grabber.grab();
                canvas.showImage(frame);
            }

            // Libera os recursos
            canvas.dispose();
            grabber.stop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}