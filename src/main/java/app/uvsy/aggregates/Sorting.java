package app.uvsy.aggregates;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Sorting {
    private static final Pattern p = Pattern.compile("[+-]");

    private final Map<String, String> fieldMapping;

    public Sorting(Map<String, String> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public Map<String, Boolean> parse(List<String> sort) {
        return sort.stream()
                .filter(this::isValid)
                .collect(
                        Collectors.toMap(
                                this::toField,
                                this::isAscending,
                                (v1, v2) -> v1
                        )
                );
    }

    private String toField(String key) {
        String sort = clean(key);
        return fieldMapping.get(sort);
    }

    private boolean isValid(String key) {
        String sort = clean(key);
        return fieldMapping.containsKey(sort);
    }

    private String clean(String key) {
        return p.matcher(key).replaceAll("");
    }

    private boolean isAscending(String content) {
        return content.charAt(0) != '-';
    }
}
