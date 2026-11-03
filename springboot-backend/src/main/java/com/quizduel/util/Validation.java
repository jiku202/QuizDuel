package com.quizduel.util;

import java.util.ArrayList;
import java.util.List;

import com.quizduel.exception.ApiException;

public final class Validation {
    private Validation() {}

    /** Pass alternating field-name / value pairs: required("name", name, "email", email). */
    public static void required(Object... pairs) {
        List<String> missing = new ArrayList<>();
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            Object value = pairs[i + 1];
            if (value == null || (value instanceof String s && s.isBlank())) missing.add((String) pairs[i]);
        }
        if (!missing.isEmpty()) {
            throw ApiException.badRequest("Missing required field(s): " + String.join(", ", missing));
        }
    }
}
