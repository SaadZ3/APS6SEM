package org.example.service;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import javax.swing.WindowConstants;

public class BiometriaService {

    public void iniciarCapturaWebcam() {
        try {
            OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
            grabber.start();

            CanvasFrame canvas = new CanvasFrame("Webcam", CanvasFrame.getDefaultGamma() / grabber.getGamma());
            // Fechar apenas esta janela, não a aplicação inteira
            canvas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            Frame frame;
            // Loop corrigido: chame o grab() apenas uma vez
            while (canvas.isVisible() && (frame = grabber.grab()) != null) {
                canvas.showImage(frame);
            }

            canvas.dispose();
            grabber.stop();
            grabber.release();

        } catch (Exception e) {
            // É crucial imprimir o erro para saber o que deu errado!
            e.printStackTrace();
        }
    }
}