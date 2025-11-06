package org.example.service;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.JavaFXFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_features2d.BFMatcher;
import org.bytedeco.opencv.opencv_features2d.ORB;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

// Imports de IO e Files
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;

import org.bytedeco.opencv.opencv_core.DMatch;
import org.bytedeco.opencv.opencv_core.DMatchVector;
import org.bytedeco.opencv.opencv_core.DMatchVectorVector;
import org.bytedeco.opencv.opencv_core.KeyPointVector;

import static org.bytedeco.opencv.global.opencv_core.NORM_HAMMING;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

public class BiometriaService {

    // --- Algoritmos ---
    private ORB orb;
    private BFMatcher matcher;
    private CascadeClassifier faceDetector; // Novo
    private static final float RATIO_THRESH = 0.75f;
    private static final int LIMIAR_MINIMO_PONTOS = 15;

    // --- Webcam ---
    private OpenCVFrameGrabber grabber;
    private Thread threadWebcam;
    private volatile boolean stopFlag = false;
    private JavaFXFrameConverter fxConverter;
    private OpenCVFrameConverter.ToMat matConverter;
    private Mat frameAtual;

    public BiometriaService() {
        this.orb = ORB.create();
        this.matcher = new BFMatcher(NORM_HAMMING);
        this.fxConverter = new JavaFXFrameConverter();
        this.matConverter = new OpenCVFrameConverter.ToMat();

        // Carrega o classificador de detecção de rosto
        carregarClassificadorRosto();
    }

    // --- MÉTODO CORRIGIDO ---
    private void carregarClassificadorRosto() {
        try {
            // 1. Pega o recurso como um "stream" da nossa pasta /resources/classifiers
            InputStream is = getClass().getResourceAsStream("/classifiers/haarcascade_frontalface_alt.xml");
            if (is == null) {
                throw new RuntimeException("Não foi possível encontrar 'haarcascade_frontalface_alt.xml' no resources/classifiers.");
            }

            // 2. Cria um arquivo temporário no seu PC
            File tempFile = Files.createTempFile("haarcascade", ".xml").toFile();
            tempFile.deleteOnExit(); // Garante que o arquivo seja deletado ao fechar o app

            // 3. Copia o arquivo do JAR para o arquivo temporário
            try (FileOutputStream os = new FileOutputStream(tempFile)) {
                is.transferTo(os);
            }

            // 4. Carrega o classificador a partir do arquivo temporário
            this.faceDetector = new CascadeClassifier(tempFile.getAbsolutePath());

            if (this.faceDetector.empty()) {
                throw new RuntimeException("Falha ao carregar o classificador de rosto (arquivo vazio ou corrompido).");
            }

            System.out.println("Classificador de rosto carregado com sucesso.");

        } catch (Exception e) {
            // Se falhar, nós *paramos* a aplicação.
            throw new RuntimeException("Erro crítico ao carregar o classificador de rosto", e);
        }
    }


    // --- MÉTODOS DE WEBCAM (Sem alteração) ---

    public void iniciarStreamWebcam(ImageView view) {
        stopFlag = false;
        grabber = new OpenCVFrameGrabber(0);
        threadWebcam = new Thread(() -> {
            try {
                grabber.start();
                Thread.sleep(200); // Warm-up
                while (!stopFlag) {
                    Frame frame = grabber.grab();
                    if (frame == null) break;
                    frameAtual = matConverter.convert(frame);
                    Image fxImage = fxConverter.convert(frame);
                    Platform.runLater(() -> view.setImage(fxImage));
                }
            } catch (Exception e) { e.printStackTrace();
            } finally {
                try { grabber.stop(); grabber.release(); } catch (Exception e) { e.printStackTrace(); }
            }
        });
        threadWebcam.setDaemon(true);
        threadWebcam.start();
    }

    public void stopStreamWebcam() {
        stopFlag = true;
        frameAtual = null;
    }

    public File capturarFrameEGravar(String caminhoArquivo) {
        if (frameAtual != null && !frameAtual.empty()) {
            try {
                imwrite(caminhoArquivo, frameAtual);
                return new File(caminhoArquivo);
            } catch (Exception e) { e.printStackTrace(); return null; }
        }
        return null;
    }

    // --- MÉTODOS DE BIOMETRIA (Sem alteração) ---

    public Mat extrairRecursosDigital(File arquivoImagem) {
        Mat imagem = imread(arquivoImagem.getAbsolutePath(), IMREAD_GRAYSCALE);
        if (imagem.empty()) {
            throw new RuntimeException("Não foi possível carregar a imagem da digital.");
        }
        KeyPointVector keypoints = new KeyPointVector();
        Mat descritores = new Mat();
        orb.detectAndCompute(imagem, new Mat(), keypoints, descritores);
        return descritores;
    }

    public Mat extrairRecursosRosto(File arquivoImagem) {
        Mat imagem = imread(arquivoImagem.getAbsolutePath(), IMREAD_GRAYSCALE);
        if (imagem.empty()) {
            throw new RuntimeException("Não foi possível carregar a imagem do rosto.");
        }

        RectVector rostosDetectados = new RectVector();
        faceDetector.detectMultiScale(imagem, rostosDetectados);

        if (rostosDetectados.size() == 0) {
            System.err.println("Nenhum rosto detectado na imagem.");
            return new Mat();
        }

        Rect dadosRosto = rostosDetectados.get(0);
        Mat rostoRecortado = new Mat(imagem, dadosRosto);
        resize(rostoRecortado, rostoRecortado, new Size(150, 150));

        KeyPointVector keypoints = new KeyPointVector();
        Mat descritores = new Mat();
        orb.detectAndCompute(rostoRecortado, new Mat(), keypoints, descritores);
        return descritores;
    }

    public boolean compararBiometria(Mat descritores1, Mat descritores2) {
        if (descritores1 == null || descritores2 == null || descritores1.empty() || descritores2.empty()) {
            return false;
        }

        DMatchVectorVector matches = new DMatchVectorVector();
        if(descritores1.cols() != descritores2.cols()) {
            System.err.println("Descritores incompatíveis.");
            return false;
        }

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