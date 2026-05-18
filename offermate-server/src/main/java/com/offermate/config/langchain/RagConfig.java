package com.offermate.config.langchain;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * RAG 配置：把 classpath:documents/ 下的面试题库切分、向量化、入库，
 * 暴露 ContentRetriever 给 @AiService 自动织入。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RagConfig {

    @Value("${offermate.ai.rag.enabled:true}")
    private boolean enabled;

    @Value("${offermate.ai.rag.documents-path:classpath:documents/}")
    private String documentsPath;

    @Value("${offermate.ai.rag.chunk-size:300}")
    private int chunkSize;

    @Value("${offermate.ai.rag.chunk-overlap:30}")
    private int chunkOverlap;

    @Value("${offermate.ai.rag.max-results:3}")
    private int maxResults;

    @Value("${offermate.ai.rag.min-score:0.6}")
    private double minScore;

    private final EmbeddingModel embeddingModel;
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public ContentRetriever ragContentRetriever(EmbeddingStore<TextSegment> embeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }

    /**
     * 应用就绪后再加载文档，避免阻塞启动，并确保 EmbeddingModel 已就绪。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void ingestDocuments() {
        if (!enabled) {
            log.info("RAG 未启用，跳过文档加载");
            return;
        }
        try {
            List<Document> documents = loadDocuments();
            if (documents.isEmpty()) {
                log.warn("RAG 文档目录为空，路径={}", documentsPath);
                return;
            }
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore())
                    .build();
            ingestor.ingest(documents);
            log.info("RAG 向量库加载完成，文档数={}", documents.size());
        } catch (Exception e) {
            log.error("RAG 文档加载失败", e);
        }
    }

    private List<Document> loadDocuments() {
        List<Document> documents = new ArrayList<>();
        ApacheTikaDocumentParser parser = new ApacheTikaDocumentParser();
        String pattern = documentsPath.endsWith("/") ? documentsPath + "**/*" : documentsPath + "/**/*";
        Resource[] resources;
        try {
            resources = resolver.getResources(pattern);
        } catch (Exception e) {
            log.warn("RAG 文档目录读取失败，路径={}，原因={}", pattern, e.getMessage());
            return documents;
        }
        for (Resource resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }
            try (InputStream in = resource.getInputStream()) {
                Document doc = parser.parse(in);
                String filename = resource.getFilename();
                if (filename != null) {
                    doc.metadata().put("filename", filename);
                }
                documents.add(doc);
            } catch (Exception e) {
                log.warn("解析 RAG 文档失败 file={}", resource.getFilename(), e);
            }
        }
        return documents;
    }
}
