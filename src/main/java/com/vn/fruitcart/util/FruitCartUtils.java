package com.vn.fruitcart.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

public class FruitCartUtils {

    public static String toSlug(String input) {
        Pattern NONLATIN = Pattern.compile("[^\\w-]");
        Pattern WHITESPACE = Pattern.compile("[\\s]");
        Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        String slug = WHITESPACE.matcher(normalized).replaceAll("-");

        slug = NONLATIN.matcher(slug).replaceAll("");

        slug = EDGESDHASHES.matcher(slug).replaceAll("");

        return slug.toLowerCase(Locale.ENGLISH);
    }

    public static void addPagingAndSortingAttributes(Model model, Pageable pageable) {
        String currentSortField = "";
        String currentSortDir = "asc";

        if (pageable.getSort().isSorted()) {
            currentSortField = pageable.getSort().iterator().next().getProperty();
            currentSortDir = pageable.getSort().iterator().next().getDirection().name().toLowerCase();
        }

        model.addAttribute("currentSortField", currentSortField);
        model.addAttribute("currentSortDir", currentSortDir);
        model.addAttribute("reverseSortDir", currentSortDir.equals("asc") ? "desc" : "asc");
    }
}