package com.example.Royal_Blueberry.mapper;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.Arrays;
import java.util.Map;

public class SentenceTranslator implements Translator<String[], float[][]> {

    private HuggingFaceTokenizer tokenizer;

    @Override
    public void prepare(TranslatorContext ctx) throws Exception {
        // Load tokenizer từ model
        tokenizer = HuggingFaceTokenizer.newInstance(
                ctx.getModel().getModelPath(),
                Map.of("padding", "true", "truncation", "true")
        );
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String[] sentences) {
        NDManager manager = ctx.getNDManager();
        Encoding encoding = tokenizer.encode(sentences[0]);

        long[] ids  = encoding.getIds();
        long[] mask = encoding.getAttentionMask();

        NDArray inputIds      = manager.create(ids).reshape(1, ids.length);
        NDArray attentionMask = manager.create(mask).reshape(1, mask.length);

        // CHỈ trả về 2 NDArray này. KHÔNG thêm tokenTypeIds.
        return new NDList(inputIds, attentionMask);
    }

    @Override
    public float[][] processOutput(TranslatorContext ctx, NDList list) {
        NDArray tokenEmbeddings = list.get(0); // [1, seq_len, 384]
        NDArray attentionMask   = list.get(1);

        NDArray maskExpanded = attentionMask
                .toType(DataType.FLOAT32, false)
                .reshape(1, attentionMask.size(), 1)  // explicit reshape thay vì expandDims
                .broadcast(tokenEmbeddings.getShape());

        NDArray sumEmbeddings = tokenEmbeddings.mul(maskExpanded).sum(new int[]{1});
        NDArray sumMask       = maskExpanded.sum(new int[]{1}).clip(1e-9f, Float.MAX_VALUE);
        NDArray meanPooled    = sumEmbeddings.div(sumMask);

        NDArray normalized = meanPooled.div(
                meanPooled.norm(new int[]{1}, true)
        );

        // Reshape về 2D
        float[] flat   = normalized.toFloatArray();
        int dims       = (int) normalized.getShape().get(1); // 384
        float[][] result = new float[1][dims];
        System.arraycopy(flat, 0, result[0], 0, dims);
        return result;
    }
}