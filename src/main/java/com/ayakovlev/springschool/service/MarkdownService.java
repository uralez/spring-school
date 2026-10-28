package com.ayakovlev.springschool.service;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MarkdownService {

    private static final Pattern MATH = Pattern.compile("\\$\\$.+?\\$\\$|\\$[^$\\n]+?\\$", Pattern.DOTALL);

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)
            .sanitizeUrls(true)
            .build();

    public String toHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        List<String> formulas = new ArrayList<>();
        Matcher matcher = MATH.matcher(markdown);
        StringBuilder text = new StringBuilder();
        while (matcher.find()) {
            formulas.add(matcher.group());
            matcher.appendReplacement(text, Matcher.quoteReplacement("MATHPLACEHOLDER" + (formulas.size() - 1) + "X"));
        }
        matcher.appendTail(text);

        String html = renderer.render(parser.parse(text.toString()));
        for (int i = 0; i < formulas.size(); i++) {
            html = html.replace("MATHPLACEHOLDER" + i + "X", HtmlUtils.htmlEscape(formulas.get(i)));
        }
        return html;
    }
}