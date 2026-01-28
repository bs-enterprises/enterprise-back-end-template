package com.bs_enterprises.enterprise_backend_template.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bs_enterprises.enterprise_backend_template.constants.MongoDBConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ============================================================
 * 🔍 UNIVERSAL QUERY BUILDER
 * ============================================================
 * Utility class for building flexible and dynamic MongoDB queries.
 * <p>
 * ✅ Supports:
 * - Include / Exclude by IDs
 * - Text search (regex across multiple fields)
 * - AND / OR logical filters
 * - Exact match, IN, and Regex filters
 * - Dynamic date range filtering
 * - Automatic combination using AND logic at top level
 * <p>
 * ------------------------------------------------------------
 * 💡 Typical Usage:
 * ------------------------------------------------------------
 * <pre>{@code
 * Map<String, Object> searchParams = new HashMap<>();
 *
 * searchParams.put("idsList", List.of("123", "456"));
 * searchParams.put("notIdsList", List.of("789"));
 *
 * searchParams.put("searchText", "john");
 * searchParams.put("searchFields", List.of("firstName", "lastName"));
 *
 * Map<String, Object> filters = new HashMap<>();
 * filters.put("and", Map.of("status", "ACTIVE"));
 * filters.put("or", Map.of("role", List.of("ADMIN", "MANAGER")));
 * searchParams.put("filters", filters);
 *
 * Map<String, Object> dateFilter = new HashMap<>();
 * dateFilter.put("type", "between");
 * dateFilter.put("field", "createdAt");
 * dateFilter.put("startDate", "2025-10-01T00:00:00Z");
 * dateFilter.put("endDate", "2025-10-31T23:59:59Z");
 * searchParams.put("dateFilter", dateFilter);
 *
 * Query query = QueryBuilderUtil.buildQuery(searchParams);
 * }</pre>
 * <p>
 * ------------------------------------------------------------
 * ⚙️ Parameter Structure:
 * ------------------------------------------------------------
 * {
 * "idsList": ["id1", "id2"],
 * "notIdsList": ["id3"],
 * "searchText": "keyword",
 * "searchFields": ["name", "email"],
 * "filters": {
 * "and": { "status": "ACTIVE" },
 * "or": { "role": ["ADMIN", "MANAGER"], "city": "regex:NY" }
 * },
 * "dateFilter": {
 * "type": "between",
 * "field": "createdAt",
 * "startDate": "2025-10-01T00:00:00Z",
 * "endDate": "2025-10-31T23:59:59Z"
 * }
 * }
 * <p>
 * ------------------------------------------------------------
 * 🧩 Date Filter Types:
 * - "on": single date (requires "onDate")
 * - ">=": start date only
 * - "<=": end date only
 * - "today": current day
 * - "between": range (requires startDate & endDate)
 * ------------------------------------------------------------
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryBuilderUtil {

    public static final String IDS_LIST = "idsList";
    public static final String NOT_IDS_LIST = "notIdsList";
    public static final String OR = "or";
    public static final String AND = "and";
    public static final String FILTERS = "filters";
    public static final String SEARCH_TEXT = "searchText";
    public static final String SEARCH_FIELDS = "searchFields";
    public static final String START_DATE_TIME = "startDateTime";
    public static final String END_DATE_TIME = "endDateTime";
    public static final String DATE_FIELD = "dateField"; // which field to apply date filter on
    public static final String DATE_FILTER = "dateFilter";
    public static final String SORT = "sort";

    /**
     * Builds a dynamic MongoDB Query object from flexible parameters.
     */
    @SuppressWarnings("unchecked")
    public static Query buildQuery(Map<String, Object> searchParams) {
        List<Criteria> allCriteria = new ArrayList<>();

        if (searchParams.containsKey(IDS_LIST))
            addIncludeIdsCriteria(searchParams, allCriteria);
        if (searchParams.containsKey(NOT_IDS_LIST))
            addExcludeIdsCriteria(searchParams, allCriteria);
        if (searchParams.containsKey(SEARCH_TEXT) && searchParams.containsKey(SEARCH_FIELDS))
            addSearchTextCriteria(searchParams, allCriteria);
        if (searchParams.containsKey(FILTERS))
            addFilterCriteria(searchParams, allCriteria);
        if (searchParams.containsKey(DATE_FILTER))
            addDateRangeCriteria(searchParams, allCriteria);

        Criteria finalCriteria = allCriteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(allCriteria.toArray(new Criteria[0]));


        Query query = Query.query(finalCriteria);
        if (searchParams.containsKey(SORT))
            addSortCriteria(searchParams, query);
        return query;
    }

    private static void addIncludeIdsCriteria(Map<String, Object> searchParams, List<Criteria> criteriaList) {
        List<String> ids = castToStringList(searchParams.get(IDS_LIST));
        if (!ids.isEmpty()) {
            criteriaList.add(Criteria.where(MongoDBConstants.FIELD_ID).in(ids));
        }
    }

    private static void addExcludeIdsCriteria(Map<String, Object> searchParams, List<Criteria> criteriaList) {
        List<String> notIds = castToStringList(searchParams.get(NOT_IDS_LIST));
        if (!notIds.isEmpty()) {
            criteriaList.add(Criteria.where(MongoDBConstants.FIELD_ID).nin(notIds));
        }
    }

    private static void addSearchTextCriteria(Map<String, Object> searchParams, List<Criteria> criteriaList) {
        String searchText = Objects.toString(searchParams.get(SEARCH_TEXT), "").trim();
        List<String> fields = castToStringList(searchParams.get(SEARCH_FIELDS));

        if (!searchText.isEmpty() && !fields.isEmpty()) {
            criteriaList.add(buildSearchTextCriteria(searchText, fields));
        }
    }

    @SuppressWarnings("unchecked")
    private static void addFilterCriteria(Map<String, Object> searchParams, List<Criteria> criteriaList) {
        Map<String, Object> filters = (Map<String, Object>) searchParams.get(FILTERS);
        Criteria filterCriteria = buildFilterCriteria(filters);
        if (filterCriteria != null) {
            criteriaList.add(filterCriteria);
        }
    }

    private static void addDateRangeCriteria(Map<String, Object> searchParams, List<Criteria> criteriaList) {
        Criteria dateCriteria = buildDateRangeCriteria(searchParams);
        criteriaList.add(dateCriteria);
    }

    /**
     * Builds a regex OR operator across multiple fields for text search.
     */
    private static Criteria buildSearchTextCriteria(String searchText, List<String> fields) {
        Pattern pattern = Pattern.compile(Pattern.quote(searchText), Pattern.CASE_INSENSITIVE);
        return new Criteria().orOperator(fields.stream()
                .map(field -> Criteria.where(field).regex(pattern))
                .toArray(Criteria[]::new));
    }

    /**
     * Builds AND/OR filter criteria from provided map.
     */
    @SuppressWarnings("unchecked")
    private static Criteria buildFilterCriteria(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) return null;

        List<Criteria> orCriteria = new ArrayList<>();
        List<Criteria> andCriteria = new ArrayList<>();

        // Process OR conditions
        if (filters.containsKey(OR)) {
            Map<String, Object> orMap = (Map<String, Object>) filters.get(OR);
            orMap.forEach((key, value) -> orCriteria.add(buildFieldCriteria(key, value)));
        }

        // Process AND conditions
        if (filters.containsKey(AND)) {
            Map<String, Object> andMap = (Map<String, Object>) filters.get(AND);
            andMap.forEach((key, value) -> andCriteria.add(buildFieldCriteria(key, value)));
        }

        // Combine
        if (!orCriteria.isEmpty() && !andCriteria.isEmpty()) {
            return new Criteria().andOperator(
                    new Criteria().orOperator(orCriteria.toArray(new Criteria[0])),
                    new Criteria().andOperator(andCriteria.toArray(new Criteria[0]))
            );
        } else if (!orCriteria.isEmpty()) {
            return new Criteria().orOperator(orCriteria.toArray(new Criteria[0]));
        } else if (!andCriteria.isEmpty()) {
            return new Criteria().andOperator(andCriteria.toArray(new Criteria[0]));
        }
        return null;
    }

    /**
     * Builds field-level criteria based on type (List, String regex, exact match, etc.)
     */
    @SuppressWarnings("unchecked")
    private static Criteria buildFieldCriteria(String key, Object value) {
        // 1) Structured operator map: { "op": "all", "values": [...] } or { "op": "size", "value": 3 }
        if (value instanceof Map<?, ?>) {
            Map<String, Object> opMap = (Map<String, Object>) value;
            String op = Objects.toString(opMap.getOrDefault("op", "in")).toLowerCase();

            switch (op) {
                case "all": {
                    Object vals = opMap.get("values");
                    if (vals instanceof Collection<?> coll) {
                        return Criteria.where(key).all(coll);
                    }
                    throw new IllegalArgumentException("Operator 'all' requires a collection in 'values'.");
                }
                case "in": {
                    Object vals = opMap.get("values");
                    if (vals instanceof Collection<?> coll) {
                        return Criteria.where(key).in(coll);
                    }
                    // also allow legacy key "values" absent but raw list passed
                    if (opMap.get("value") instanceof Collection<?> coll2) {
                        return Criteria.where(key).in(coll2);
                    }
                    throw new IllegalArgumentException("Operator 'in' requires a collection in 'values'.");
                }
                case "nin": {
                    Object vals = opMap.get("values");
                    if (vals instanceof Collection<?> coll) {
                        return Criteria.where(key).nin(coll);
                    }
                    throw new IllegalArgumentException("Operator 'nin' requires a collection in 'values'.");
                }
                case "size": {
                    Object v = opMap.get("value");
                    if (v instanceof Number n) {
                        return Criteria.where(key).size(n.intValue());
                    }
                    throw new IllegalArgumentException("Operator 'size' requires numeric 'value'.");
                }
                case "exists": {
                    Object v = opMap.get("value");
                    if (v instanceof Boolean b) {
                        return Criteria.where(key).exists(b);
                    }
                    throw new IllegalArgumentException("Operator 'exists' requires boolean 'value'.");
                }
                case "regex": {
                    // support pattern + options, or "pattern" only
                    String pattern = Objects.toString(opMap.getOrDefault("pattern", ""), "");
                    String options = Objects.toString(opMap.getOrDefault("options", "i")); // default ignore-case
                    if (!pattern.isEmpty()) {
                        return Criteria.where(key).regex(pattern, options);
                    }
                    throw new IllegalArgumentException("Operator 'regex' requires 'pattern'.");
                }
                case "eq": {
                    return Criteria.where(key).is(opMap.get("value"));
                }
                default:
                    throw new IllegalArgumentException("Unsupported filter operator: " + op);
            }
        }

        // 2) Collection -> default to .in(...) for backward compatibility
        if (value instanceof Collection<?>) {
            return Criteria.where(key).in((Collection<?>) value);
        }

        // 3) String -> allow the regex: prefix as before
        if (value instanceof String str) {
            if (str.startsWith("regex:")) {
                String pattern = str.substring("regex:".length());
                return Criteria.where(key).regex(pattern, "i");
            }
            // plain string exact match
            return Criteria.where(key).is(str);
        }

        // 4) Fallback to exact match for other types (Number, Boolean...)
        return Criteria.where(key).is(value);
    }


    @SuppressWarnings("unchecked")
    private static Criteria buildDateRangeCriteria(Map<String, Object> searchParams) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> dateFilter = objectMapper.convertValue(
                    searchParams.get(DATE_FILTER), new TypeReference<>() {
                    }
            );

            String filterType = Objects.toString(dateFilter.getOrDefault("type", "between"));
            String dateField = Objects.toString(dateFilter.getOrDefault("field", "createdAt"));

            Instant startDate = dateFilter.containsKey("startDate")
                    ? Instant.parse(dateFilter.get("startDate").toString()) : null;
            Instant endDate = dateFilter.containsKey("endDate")
                    ? Instant.parse(dateFilter.get("endDate").toString()) : null;
            Instant onDate = dateFilter.containsKey("onDate")
                    ? Instant.parse(dateFilter.get("onDate").toString()) : null;

            Criteria criteria = Criteria.where(dateField);

            // ✅ Normalize to midnight UTC
            java.time.ZoneId zone = java.time.ZoneOffset.UTC;

            switch (filterType.toLowerCase()) {
                case "on": {
                    if (onDate == null)
                        throw new IllegalArgumentException("Missing 'onDate' for 'on' date filter type.");
                    Instant dayStart = onDate.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
                    Instant dayEnd = dayStart.plus(1, java.time.temporal.ChronoUnit.DAYS).minusMillis(1);
                    return criteria.gte(dayStart).lte(dayEnd);
                }
                case ">=": {
                    if (startDate == null)
                        throw new IllegalArgumentException("Missing 'startDate' for '>=' date filter type.");
                    Instant dayStart = startDate.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
                    return criteria.gte(dayStart);
                }
                case "<=": {
                    if (endDate == null)
                        throw new IllegalArgumentException("Missing 'endDate' for '<=' date filter type.");
                    Instant dayEnd = endDate.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1);
                    return criteria.lte(dayEnd);
                }
                case "today": {
                    Instant todayStart = Instant.now().atZone(zone)
                            .toLocalDate().atStartOfDay(zone).toInstant();
                    Instant todayEnd = todayStart.plus(1, java.time.temporal.ChronoUnit.DAYS).minusMillis(1);
                    return criteria.gte(todayStart).lte(todayEnd);
                }
                case "between": {
                    if (startDate == null || endDate == null)
                        throw new IllegalArgumentException("Missing 'startDate' or 'endDate' for 'between' date filter type.");
                    Instant dayStart = startDate.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant();
                    Instant dayEnd = endDate.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant().minusMillis(1);
                    return criteria.gte(dayStart).lte(dayEnd);
                }
                default:
                    throw new IllegalArgumentException("Invalid date filter type. Allowed: on, >=, <=, today, between.");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid date filter structure or format. Expected ISO 8601 (e.g. 2024-08-10T00:00:00Z)", e);
        }
    }

    private static List<String> castToStringList(Object input) {
        if (input instanceof List<?>) {
            return ((List<?>) input).stream()
                    .map(Objects::toString)
                    .toList(); // returns an unmodifiable list (Java 16+)
        }
        return List.of(); // returns an immutable empty list
    }

    /**
     * Adds sorting logic to the query.
     * <p>
     * Expected format:
     * "sort": {
     * "fieldName": 1 | -1
     * }
     */
    @SuppressWarnings("unchecked")
    private static void addSortCriteria(
            Map<String, Object> searchParams,
            Query query
    ) {
        Object sortObj = searchParams.get(SORT);

        if (!(sortObj instanceof Map<?, ?> sortMap) || sortMap.isEmpty()) {
            return;
        }

        sortMap.forEach((field, direction) -> {
            if (!(direction instanceof Number)) {
                throw new IllegalArgumentException(
                        "Sort direction must be numeric (1 or -1) for field: " + field
                );
            }

            int dir = ((Number) direction).intValue();
            if (dir != 1 && dir != -1) {
                throw new IllegalArgumentException(
                        "Invalid sort direction for field '" + field + "'. Use 1 (ASC) or -1 (DESC)"
                );
            }

            query.with(
                    Sort.by(
                            dir == 1
                                    ? Sort.Direction.ASC
                                    : Sort.Direction.DESC,
                            field.toString()
                    )
            );
        });
    }

}
