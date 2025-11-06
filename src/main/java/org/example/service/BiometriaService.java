package org.example.service;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter; // Importe
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;

import java.io.File;

// Imports do ORB e de comparação
import org.bytedeco.opencv.opencv_core.DMatch;
import org.bytedeco.opencv.opencv_core.DMatchVector;
import org.bytedeco.opencv.opencv_core.DMatchVectorVector;
import org.bytedeco.opencv.opencv_core.KeyPointVector;
import org.bytedeco.opencv.opencv_features2d.BFMatcher;
import org.bytedeco.opencv.opencv_features2d.ORB;
import static org.bytedeco.opencv.global.opencv_core.NORM_HAMMING;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

public class BiometriaService {

    // --- LÓGICA ORB (Impressão Digital) - Sem alteração ---
    private ORB orb;
    private BFMatcher matcher;
    private static final float RATIO_THRESH = 0.75f;
    private static final int LIMIAR_MINIMO_PONTOS = 15;

    // --- NOVOS ATRIBUTOS PARA WEBCAM ---
    private OpenCVFrameGrabber grabber;
    private Thread threadWebcam;
    private volatile boolean stopFlag = false;
    private JavaFXFrameConverter fxConverter;
    private OpenCVFrameConverter.ToMat matConverter;
    private Mat frameAtual;

    // Construtor
    public BiometriaService() {
        this.orb = ORB.create();
        this.matcher = new BFMatcher(NORM_HAMMING);
        this.fxConverter = new JavaFXFrameConverter();
        this.matConverter = new OpenCVFrameConverter.ToMat();
    }

    // --- NOVOS MÉTODOS PARA WEBCAM ---

    /**
     * Inicia a captura da webcam e exibe o stream em um ImageView do JavaFX.
     */
    public void iniciarStreamWebcam(ImageView view) {
        stopFlag = false;
        grabber = new OpenCVFrameGrabber(0); // 0 para a câmera padrão

        threadWebcam = new Thread(() -> {
            try {
                grabber.start();
                while (!stopFlag) {
                    Frame frame = grabber.grab();
                    if (frame == null) {
                        break;
                    }

                    // 1. Converte o Frame para Mat (para salvar)
                    frameAtual = matConverter.convert(frame);

                    // --- CORREÇÃO AQUI ---
                    // 2. Converte o Frame para Imagem JavaFX (para exibir)
                    Image fxImage = fxConverter.convert(frame); // Usamos o 'frame' original

                    // Atualiza a UI na Thread principal do JavaFX
                    Platform.runLater(() -> {
                        view.setImage(fxImage);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                // Garante que a câmera seja liberada ao parar
                try {
                    grabber.stop();
                    grabber.release();
                } catch (Exception e) { e.printStackTrace(); }
            }
        });
        threadWebcam.setDaemon(true);
        threadWebcam.start();
    }

    /**
     * Para o stream da webcam e libera os recursos.
     */
    public void stopStreamWebcam() {
        stopFlag = true;
        frameAtual = null; // Limpa o frame
    }

    /**
     * Captura o frame atual da webcam e o salva em um arquivo.
     */
    public File capturarFrameEGravar(String caminhoArquivo) {
        if (frameAtual != null && !frameAtual.empty()) {
            try {
                // Salva o Mat como um arquivo de imagem
                imwrite(caminhoArquivo, frameAtual);
                return new File(caminhoArquivo);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    // --- MÉTODOS DE BIOMETRIA (Sem alteração) ---

    public Mat extrairRecursos(File arquivoImagem) {
        Mat imagem = imread(arquivoImagem.getAbsolutePath(), IMREAD_GRAYSCALE);
        if (imagem.empty()) {
            throw new RuntimeException("Não foi possível carregar a imagem: " + arquivoImagem.getAbsolutePath());
        }
        KeyPointVector keypoints = new KeyPointVector();
        Mat descritores = new Mat();
        orb.detectAndCompute(imagem, new Mat(), keypoints, descritores);
        return descritores;
    }

    public boolean compararBiometria(Mat descritores1, Mat descritores2) {
        if (descritores1.empty() || descritores2.empty()) {
            return false;
        }
        DMatchVectorVector matches = new DMatchVectorVector();
        matcher.knnMatch(descritores1, descritores2, matches, 2);

        int boasCorrelacoes = 0;
        for (long i = 0; i < matches.size(); i++) {
            DMatchVector match = matches.get(i);
            if (match.size() > 1) {
                DMatch m = match.get(0);
                DMatch n = match.get(1);
                if (m.distance() < RATIO_THRESH * n.distance()) {
                    boasCorrelacoes++;
                }
            }
        }
        return boasCorrelacoes >= LIMIAR_MINIMO_PONTOS;
    }

    // Métodos de conversão (Sem alteração)
    public byte[] converterMatParaBytes(Mat mat) {
        long size = mat.total() * mat.elemSize();
        byte[] bytes = new byte[(int) size];
        mat.data().get(bytes);
        return bytes;
    }

    public Mat converterBytesParaMat(byte[] bytes, int rows, int cols, int type) {
        Mat mat = new Mat(rows, cols, type);
        mat.data().put(bytes);
        return mat;
    }
}