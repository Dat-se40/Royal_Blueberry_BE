package com.example.Royal_Blueberry.service.impl;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
public class EmbeddingService {

    private OrtEnvironment env;
    private OrtSession session;
    private HuggingFaceTokenizer tokenizer;
    private boolean isInitialized = false;

    @EventListener(ApplicationReadyEvent.class)
    public void init() throws Exception {
        try {
            log.info("[EmbeddingService] Loading ONNX model...");
            long startTime = System.currentTimeMillis();

            // Load ONNX model
            log.info("[EmbeddingService] Creating OrtEnvironment...");
            env = OrtEnvironment.getEnvironment();

            log.info("[EmbeddingService] Reading model.onnx from classpath...");
            try (InputStream is = getClass().getResourceAsStream("/models/model.onnx")) {
                if (is == null) {
                    throw new RuntimeException("model.onnx not found in classpath!");
                }
                byte[] modelBytes = is.readAllBytes();
                log.info("[EmbeddingService] model.onnx size: {} bytes", modelBytes.length);

                log.info("[EmbeddingService] Creating OrtSession...");
                session = env.createSession(modelBytes, new OrtSession.SessionOptions());
            }

            // Load tokenizer từ local file
            log.info("[EmbeddingService] Loading tokenizer.json...");
            Path tokenizerPath = Paths.get(
                    getClass().getResource("/models/tokenizer.json").toURI()
            );
            log.info("[EmbeddingService] Tokenizer path: {}", tokenizerPath);
            tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);

            isInitialized = true;
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("[EmbeddingService] ✅ ONNX model loaded successfully in {}ms", elapsed);

        } catch (Exception e) {
            log.error("[EmbeddingService] ❌ Failed to initialize ONNX model", e);
            throw new RuntimeException("Failed to initialize EmbeddingService: " + e.getMessage(), e);
        }
    }

    public float[] embed(String text) {
        if (!isInitialized) {
            log.warn("[EmbeddingService] Not initialized, returning zero vector");
            return new float[384];
        }

        try {
            Encoding encoding = tokenizer.encode(text);

            long[] inputIds      = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] tokenTypeIds  = encoding.getTypeIds();

            // Shape: [1, seq_len]
            long[] shape = {1, inputIds.length};

            Map<String, OnnxTensor> inputs = Map.of(
                    "input_ids",      OnnxTensor.createTensor(env, LongBuffer.wrap(inputIds), shape),
                    "attention_mask", OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), shape),
                    "token_type_ids", OnnxTensor.createTensor(env, LongBuffer.wrap(tokenTypeIds), shape)
            );

            try (OrtSession.Result result = session.run(inputs)) {
                // Output shape: [1, seq_len, 384]
                float[][][] tokenEmbeddings = (float[][][]) result.get(0).getValue();
                return meanPooling(tokenEmbeddings[0], attentionMask);
            }

        } catch (Exception e) {
            log.error("[EmbeddingService] Embed error: {}", e.getMessage(), e);
            return new float[384];
        }
    }

    // Mean pooling — lấy trung bình có trọng số theo attention mask
    private float[] meanPooling(float[][] tokenEmbeddings, long[] attentionMask) {
        int seqLen = tokenEmbeddings.length;
        int dims   = tokenEmbeddings[0].length;  // 384
        float[] result = new float[dims];
        float maskSum  = 0;

        for (int i = 0; i < seqLen; i++) {
            float m = attentionMask[i];
            maskSum += m;
            for (int j = 0; j < dims; j++) {
                result[j] += tokenEmbeddings[i][j] * m;
            }
        }

        // Normalize
        float norm = 0;
        for (int j = 0; j < dims; j++) {
            result[j] /= Math.max(maskSum, 1e-9f);
            norm += result[j] * result[j];
        }
        norm = (float) Math.sqrt(norm);
        for (int j = 0; j < dims; j++) {
            result[j] /= norm;
        }

        return result;
    }
}