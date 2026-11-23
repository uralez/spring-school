package com.ayakovlev.springschool.service;

import com.ayakovlev.springschool.entity.Textbook;
import com.ayakovlev.springschool.repository.TextbookRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class TextbookFileService {

    private final TextbookRepository textbookRepository;
    private final Path dir;

    public TextbookFileService(TextbookRepository textbookRepository,
                @Value("${app.files.dir}") String dir) {
//    public TextbookFileService(TextbookRepository textbookRepository,
//                               @Value("${app.files.dir:./data/files}") String dir) {
        this.textbookRepository = textbookRepository;
        this.dir = Path.of(dir).toAbsolutePath();
    }

    @Transactional
    public void store(Long textbookId, MultipartFile file) throws IOException {
        Textbook textbook = textbookRepository.findById(textbookId).orElseThrow();
        String original = file.getOriginalFilename();
        if (file.isEmpty() || original == null || !original.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Files.createDirectories(dir);
        String name = UUID.randomUUID().toString();
        Path pdf = dir.resolve(name + ".pdf");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, pdf, StandardCopyOption.REPLACE_EXISTING);
        }
        String preview = null;
        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            BufferedImage image = new PDFRenderer(doc).renderImageWithDPI(0, 100, ImageType.RGB);
            ImageIO.write(image, "png", dir.resolve(name + ".png").toFile());
            preview = name + ".png";
        } catch (IOException e) {
            Files.deleteIfExists(pdf);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        deleteQuietly(textbook.getStoredFile());
        deleteQuietly(textbook.getPreviewFile());
        textbook.setStoredFile(name + ".pdf");
        textbook.setOriginalName(original);
        textbook.setPreviewFile(preview);
    }

    public Path file(String name) {
        return dir.resolve(name).normalize();
    }

    private void deleteQuietly(String name) {
        if (name == null) {
            return;
        }
        try {
            Files.deleteIfExists(dir.resolve(name));
        } catch (IOException ignored) {
        }
    }
}