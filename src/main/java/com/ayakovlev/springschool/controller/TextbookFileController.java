package com.ayakovlev.springschool.controller;

import com.ayakovlev.springschool.entity.Textbook;
import com.ayakovlev.springschool.repository.TextbookRepository;
import com.ayakovlev.springschool.service.TextbookFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;

@Controller
@RequiredArgsConstructor
public class TextbookFileController {

    private final TextbookRepository textbookRepository;
    private final TextbookFileService textbookFileService;

    @GetMapping("/textbooks/{id}/file")
    @PreAuthorize("@access.canReadTextbook(authentication, #id)")
    @ResponseBody
    public ResponseEntity<Resource> file(@PathVariable Long id) {
        Textbook t = textbookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (t.getStoredFile() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path path = textbookFileService.file(t.getStoredFile());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename("textbook.pdf").build().toString())
                .body(new FileSystemResource(path));
    }

    @GetMapping("/textbooks/{id}/preview")
    @ResponseBody
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        Textbook t = textbookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (t.getPreviewFile() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path path = textbookFileService.file(t.getPreviewFile());
        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new FileSystemResource(path));
    }
}