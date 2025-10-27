package org.example.service;

import org.bytedeco.opencv.opencv_core.DMatch;
import org.bytedeco.opencv.opencv_core.DMatchVector;
import org.bytedeco.opencv.opencv_core.DMatchVectorVector;
import org.bytedeco.opencv.opencv_core.KeyPointVector;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_features2d.BFMatcher;
import org.bytedeco.opencv.opencv_features2d.ORB;

import java.io.File;
import java.nio.ByteBuffer;

import static org.bytedeco.opencv.global.opencv_core.NORM_HAMMING;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

public class BiometriaService {

    private ORB orb;
    private BFMatcher matcher;
    private static final float RATIO_THRESH = 0.75f;
    private static final int LIMIAR_MINIMO_PONTOS = 15;

    // Inicializando os objetos do OpenCV
    public BiometriaService() {
        this.orb = ORB.create();
        this.matcher = new BFMatcher(NORM_HAMMING);
    }

    // Extrai os descritores (a "assinatura biométrica") de um arquivo de imagem.
    public Mat extrairRecursos(File arquivoImagem) {
        // Carrega a imagem em escala de cinza
        Mat imagem = imread(arquivoImagem.getAbsolutePath(), IMREAD_GRAYSCALE);
        if (imagem.empty()) {
            throw new RuntimeException("Não foi possível carregar a imagem: " + arquivoImagem.getAbsolutePath());
        }

        KeyPointVector keypoints = new KeyPointVector();
        Mat descritores = new Mat();

        // Detecta os pontos-chave e calcula os descritores
        orb.detectAndCompute(imagem, new Mat(), keypoints, descritores);

        return descritores;
    }

    // Compara dois conjuntos de descritores e retorna true se combinarem.
    public boolean compararBiometria(Mat descritores1, Mat descritores2) {
        if (descritores1.empty() || descritores2.empty()) {
            return false;
        }

        // Encontra os 2 melhores "matches" para cada descritor
        DMatchVectorVector matches = new DMatchVectorVector();
        matcher.knnMatch(descritores1, descritores2, matches, 2);

        int boasCorrelacoes = 0;

        // Filtro de "Lowe's Ratio Test" para encontrar apenas matches robustos
        for (long i = 0; i < matches.size(); i++) {
            DMatchVector match = matches.get(i);
            if (match.size() > 1) { // Garante que temos 2 vizinhos para comparar
                DMatch m = match.get(0); // O melhor match
                DMatch n = match.get(1); // O segundo melhor match
                if (m.distance() < RATIO_THRESH * n.distance()) {
                    boasCorrelacoes++;
                }
            }
        }

        // Se o número de bons matches for maior que o limiar, considera um sucesso
        return boasCorrelacoes >= LIMIAR_MINIMO_PONTOS;
    }


    // ------- Métodos Utilitários para o Banco de Dados -------

    // Converte um Mat do OpenCV para um array de bytes para salvar no BLOB.
    public byte[] converterMatParaBytes(Mat mat) {
        long size = mat.total() * mat.elemSize();
        byte[] bytes = new byte[(int) size];
        mat.data().asByteBuffer().get(bytes);
        return bytes;
    }

    // Reconstrói um Mat a partir dos bytes e metadados salvos no banco.
    public Mat converterBytesParaMat(byte[] bytes, int rows, int cols, int type) {
        Mat mat = new Mat(rows, cols, type);
        mat.data().asByteBuffer().put(bytes);
        return mat;
    }
}