package com.ayakovlev.springschool.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TopicListParser {

    private static final Pattern BLANK_LINE = Pattern.compile("\\n\\s*\\n");
    private static final Pattern NUMBERING = Pattern.compile("^\\s*\\d+\\s*(?:[.)]|[-\u2013\u2014:])\\s*");

    public List<String> parse(String text) {
        List<String> topics = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return topics;
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').strip();
        String[] blocks = BLANK_LINE.split(normalized);
        if (blocks.length > 1) {
            for (String block : blocks) {
                String[] lines = block.strip().split("\n");
                lines[0] = NUMBERING.matcher(lines[0]).replaceFirst("");
                String topic = Arrays.stream(lines).map(String::strip)
                        .filter(s -> !s.isEmpty()).collect(Collectors.joining(" "));
                if (!topic.isEmpty()) {
                    topics.add(topic);
                }
            }
        } else {
            for (String line : normalized.split("\n")) {
                String topic = NUMBERING.matcher(line).replaceFirst("").strip();
                if (!topic.isEmpty()) {
                    topics.add(topic);
                }
            }
        }
        return topics;
    }
}