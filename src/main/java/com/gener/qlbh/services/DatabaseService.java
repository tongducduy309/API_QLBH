package com.gener.qlbh.services;

import com.gener.qlbh.configuration.NeonDataSourceFactory;
import com.gener.qlbh.enums.SuccessCode;
import com.gener.qlbh.models.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.postgresql.util.PGobject;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatabaseService {

    // LOCAL: SQLite
    private final JdbcTemplate localJdbc;

    // REMOTE: Postgres (Neon)
    private final NeonDataSourceFactory neonFactory;

    /**
     * Overwrite remote Neon DB tables by local data (SQLite).
     */
    public ResponseEntity<ResponseObject> replaceRemoteWithLocal(
            List<String> tablesToPopulate,
            List<String> allTablesForTruncate,
            boolean dryRun,
            boolean shutdownPoolAfter
    ) {
        if (tablesToPopulate == null || tablesToPopulate.isEmpty()) {
            throw new IllegalArgumentException("tablesToPopulate must not be empty");
        }

        // 1. Đọc toàn bộ dữ liệu từ local (SQLite)
        Map<String, List<Map<String, Object>>> localData = new LinkedHashMap<>();
        for (String table : tablesToPopulate) {
            List<Map<String, Object>> rows = localJdbc.queryForList("SELECT * FROM " + table);
            localData.put(table, rows);
        }

        // 2. Nếu dryRun: chỉ trả về số lượng dòng
        if (dryRun) {
            Map<String, Integer> counts = new LinkedHashMap<>();
            localData.forEach((t, rs) -> counts.put(t, rs == null ? 0 : rs.size()));
            return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode())
                    .body(ResponseObject.builder()
                            .status(SuccessCode.REQUEST.getStatus())
                            .message("Count Row Of Table")
                            .data(counts)
                            .build());
        }

        // 3. Build truncate SQL cho remote
        List<String> truncList = (allTablesForTruncate == null || allTablesForTruncate.isEmpty())
                ? tablesToPopulate
                : allTablesForTruncate;
        String truncateSql = buildTruncateSql(truncList);

        // 4. Transaction trên remote (Postgres)
        TransactionTemplate txTemplate = neonFactory.createTransactionTemplate();
        Map<String, Integer> resultCounts = new LinkedHashMap<>();

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                JdbcTemplate remote = neonFactory.createJdbcTemplate();

                // truncate
                remote.execute(truncateSql);

                // insert từng bảng theo thứ tự
                for (Map.Entry<String, List<Map<String, Object>>> e : localData.entrySet()) {
                    String table = e.getKey();
                    List<Map<String, Object>> rows = e.getValue();
                    if (rows == null || rows.isEmpty()) {
                        resultCounts.put(table, 0);
                        continue;
                    }
                    batchInsert(remote, table, rows);
                    resultCounts.put(table, rows.size());
                }
            }
        });

        if (shutdownPoolAfter) {
            neonFactory.shutdownPool();
        }

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode())
                .body(ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Replace Successfully")
                        .data(resultCounts)
                        .build());
    }

    /**
     * Insert batch từ local (SQLite) sang remote (Postgres).
     * - Xử lý enum (USER-DEFINED)
     * - Xử lý đặc biệt cột roles (text[])
     */
    private void batchInsert(JdbcTemplate remote, String table, List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return;

        // 1) detect enum columns trên remote
        Map<String, String> enumColumns = getEnumColumns(remote, table); // columnName -> udt_name

        Map<String, Set<String>> enumAllowed = new HashMap<>();
        for (String udt : new HashSet<>(enumColumns.values())) {
            enumAllowed.put(udt, fetchEnumLabels(remote, udt));
        }

        Map<String, Object> first = rows.get(0);
        List<String> columns = new ArrayList<>(first.keySet());
        String colList = String.join(", ", columns);
        String placeholders = columns.stream().map(c -> "?").collect(Collectors.joining(", "));
        String sql = "INSERT INTO " + table + " (" + colList + ") VALUES (" + placeholders + ")";

        remote.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map<String, Object> row = rows.get(i);

                for (int j = 0; j < columns.size(); j++) {
                    String col = columns.get(j);
                    Object raw = row.get(col);
                    int paramIndex = j + 1;

                    if (raw == null) {
                        ps.setObject(paramIndex, null);
                        continue;
                    }

                    // ENUM (USER-DEFINED) bên Postgres
                    if (enumColumns.containsKey(col)) {
                        String udt = enumColumns.get(col); // e.g. categories_method_enum
                        String s = raw.toString();

                        String allowed = findMatchingEnumLabel(enumAllowed.get(udt), s);
                        if (allowed == null) {
                            throw new SQLException("Value '" + s + "' is not a valid label for enum type " + udt + " (column " + col + ")");
                        }

                        PGobject pg = new PGobject();
                        pg.setType(udt);
                        pg.setValue(allowed);
                        ps.setObject(paramIndex, pg);
                    }
                    // ✅ Cột roles (text[]) bên Postgres
                    else if ("roles".equalsIgnoreCase(col)) {
                        setTextArray(ps, paramIndex, raw);
                    }
                    // Các cột thường
                    else {
                        Object val = convertValue(col, raw);
                        ps.setObject(paramIndex, val);
                    }
                }
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }

    /**
     * Map value từ SQLite sang Postgres text[] cho cột roles.
     * Hỗ trợ:
     *  - raw là String: "ADMIN", "ADMIN,USER", "[\"ADMIN\",\"USER\"]", "{ADMIN,USER}"
     *  - raw là byte[]: nội dung text UTF-8 như trên
     *  - raw là List<?>: convert từng phần tử -> String
     */
    private void setTextArray(PreparedStatement ps, int paramIndex, Object raw) throws SQLException {
        if (raw == null) {
            ps.setNull(paramIndex, java.sql.Types.ARRAY);
            return;
        }

        String[] arr;

        if (raw instanceof String[]) {
            arr = (String[]) raw;
        } else if (raw instanceof List<?>) {
            arr = ((List<?>) raw).stream()
                    .map(String::valueOf)
                    .toArray(String[]::new);
        } else if (raw instanceof byte[]) {
            arr = parseStringArrayFromBytes((byte[]) raw);
        } else if (raw instanceof String) {
            arr = parseStringArrayFromString((String) raw);
        } else {
            // fallback: 1 phần tử
            arr = new String[]{String.valueOf(raw)};
        }

        java.sql.Connection conn = ps.getConnection();
        java.sql.Array sqlArray = conn.createArrayOf("text", arr);
        ps.setArray(paramIndex, sqlArray);
    }

    private String[] parseStringArrayFromBytes(byte[] bytes) {
        String s = new String(bytes, StandardCharsets.UTF_8).trim();
        return parseStringArrayFromString(s);
    }

    /**
     * Parse String thành String[]:
     *  - "ADMIN"                -> ["ADMIN"]
     *  - "ADMIN,USER"           -> ["ADMIN","USER"]
     *  - "[\"ADMIN\",\"USER\"]" -> ["ADMIN","USER"]
     *  - "{ADMIN,USER}"         -> ["ADMIN","USER"]
     */
    private String[] parseStringArrayFromString(String input) {
        if (input == null) return new String[0];
        String s = input.trim();
        if (s.isEmpty()) return new String[0];

        // loại [] hoặc {} bọc ngoài nếu có
        if ((s.startsWith("[") && s.endsWith("]")) || (s.startsWith("{") && s.endsWith("}"))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        if (s.isEmpty()) return new String[0];

        // tách theo dấu phẩy
        String[] parts = s.split(",");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            String v = p.trim();
            if (v.isEmpty()) continue;
            // bỏ quote "..." hoặc '...'
            if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                v = v.substring(1, v.length() - 1).trim();
            }
            if (!v.isEmpty()) {
                result.add(v);
            }
        }

        if (result.isEmpty()) {
            // không có dấu phẩy, trả về 1 phần tử
            return new String[]{s};
        }
        return result.toArray(new String[0]);
    }

    /**
     * Detect enum columns (USER-DEFINED) trên remote Postgres.
     */
    private Map<String, String> getEnumColumns(JdbcTemplate remote, String table) {
        String sql = "SELECT column_name, udt_name, data_type " +
                "FROM information_schema.columns " +
                "WHERE table_schema = 'public' AND table_name = ?";
        List<Map<String, Object>> rows = remote.queryForList(sql, table);
        Map<String, String> result = new HashMap<>();
        for (Map<String, Object> r : rows) {
            String column = (String) r.get("column_name");
            String dataType = r.get("data_type") == null ? "" : r.get("data_type").toString();
            String udtName = r.get("udt_name") == null ? "" : r.get("udt_name").toString();
            if ("USER-DEFINED".equalsIgnoreCase(dataType) && udtName != null && !udtName.isBlank()) {
                result.put(column, udtName);
            }
        }
        return result;
    }

    private Set<String> fetchEnumLabels(JdbcTemplate remote, String enumType) {
        try {
            String sql = "SELECT enumlabel FROM pg_enum WHERE enumtypid = (?::regtype)";
            List<String> list = remote.queryForList(sql, String.class, enumType);
            return new HashSet<>(list);
        } catch (DataAccessException ex) {
            return Collections.emptySet();
        }
    }

    private String findMatchingEnumLabel(Set<String> allowed, String raw) {
        if (allowed == null || allowed.isEmpty()) return null;
        if (allowed.contains(raw)) return raw;

        for (String a : allowed) {
            if (a.equalsIgnoreCase(raw)) return a;
        }

        String norm = raw.trim().replace(' ', '_');
        for (String a : allowed) {
            if (a.equalsIgnoreCase(norm)) return a;
        }
        return null;
    }

    /**
     * Heuristic convert value (thời gian, boolean) cho các cột thường.
     */
    private Object convertValue(String column, Object raw) {
        if (raw == null) return null;
        String col = column.toLowerCase();

        // timestamp heuristic
        if (col.endsWith("_at") || col.contains("date") || col.endsWith("_time")) {
            if (raw instanceof Timestamp) return raw;
            if (raw instanceof Number) return new Timestamp(((Number) raw).longValue());
            String s = raw.toString();
            try {
                Instant t = Instant.parse(s);
                return Timestamp.from(t);
            } catch (Exception ignored) {}
            try {
                return Timestamp.valueOf(s);
            } catch (Exception ignored) {}
            return s;
        }

        // boolean heuristic
        if (col.startsWith("is_") || col.endsWith("_flag") || col.equals("enabled") || col.equals("active")) {
            if (raw instanceof Number) return ((Number) raw).intValue() != 0;
            String s = raw.toString();
            return "1".equals(s) || "true".equalsIgnoreCase(s);
        }

        return raw;
    }

    private String buildTruncateSql(List<String> tables) {
        StringJoiner sj = new StringJoiner(", ");
        tables.forEach(sj::add);
        return "TRUNCATE TABLE " + sj + " RESTART IDENTITY CASCADE";
    }

    public void resetSequence(JdbcTemplate remote, String table, String pkColumn, String seqName) {
        String sql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 1) FROM %s))",
                seqName, pkColumn, table
        );
        remote.execute(sql);
    }

    public ResponseEntity<ResponseObject> wipeDatabase(String target, boolean dryRun, List<String> exclude) {
        Map<String, Object> result = new LinkedHashMap<>();

        if ("remote".equalsIgnoreCase(target) || "both".equalsIgnoreCase(target)) {
            JdbcTemplate remote = neonFactory.createJdbcTemplate();
            Map<String, Object> r = wipeSingleDb(remote, "remote", dryRun, exclude);
            result.put("remote", r);
        }
        if ("local".equalsIgnoreCase(target) || "both".equalsIgnoreCase(target)) {
            Map<String, Object> l = wipeSingleDb(localJdbc, "local", dryRun, exclude);
            result.put("local", l);
        }

        return ResponseEntity.status(SuccessCode.REQUEST.getHttpStatusCode())
                .body(ResponseObject.builder()
                        .status(SuccessCode.REQUEST.getStatus())
                        .message("Replace Successfully")
                        .data(result)
                        .build());
    }

    private Map<String, Object> wipeSingleDb(JdbcTemplate jdbc, String tag, boolean dryRun, List<String> exclude) {
        Map<String, Object> info = new LinkedHashMap<>();
        try {
            String product = jdbc.queryForObject("select version()", String.class);
            String lower = product == null ? "" : product.toLowerCase();

            if (lower.contains("sqlite")) {
                // SQLite: drop toàn bộ user tables
                List<String> tables = jdbc.queryForList(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';",
                        String.class
                );
                List<String> toDrop = filterExclude(tables, exclude);
                info.put("foundTables", tables.size());
                info.put("tables", toDrop);
                if (!dryRun) {
                    for (String t : toDrop) {
                        jdbc.execute("DROP TABLE IF EXISTS \"" + t + "\";");
                    }
                    info.put("dropped", toDrop.size());
                }
            } else if (lower.contains("postgres")) {
                List<String> tables = jdbc.queryForList(
                        "SELECT table_schema||'.'||table_name FROM information_schema.tables " +
                                "WHERE table_schema='public' AND table_type='BASE TABLE'",
                        String.class
                );
                List<String> toTruncate = filterExclude(tables, exclude);
                info.put("foundTables", tables.size());
                info.put("tables", toTruncate);
                if (!dryRun && !toTruncate.isEmpty()) {
                    String list = toTruncate.stream()
                            .map(s -> {
                                if (s.contains(".")) {
                                    String[] parts = s.split("\\.", 2);
                                    return "\"" + parts[0] + "\".\"" + parts[1] + "\"";
                                } else {
                                    return "\"" + s + "\"";
                                }
                            })
                            .collect(Collectors.joining(", "));
                    String sql = "TRUNCATE TABLE " + list + " RESTART IDENTITY CASCADE";
                    jdbc.execute(sql);
                    info.put("truncated", toTruncate.size());
                }
            } else {
                // fallback
                List<String> tables = jdbc.queryForList(
                        "SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'",
                        String.class
                );
                List<String> toTruncate = filterExclude(tables, exclude);
                info.put("foundTables", tables.size());
                info.put("tables", toTruncate);
                if (!dryRun && !toTruncate.isEmpty()) {
                    String list = toTruncate.stream()
                            .map(t -> "\"" + t + "\"")
                            .collect(Collectors.joining(", "));
                    jdbc.execute("TRUNCATE TABLE " + list + " RESTART IDENTITY CASCADE");
                    info.put("truncated", toTruncate.size());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            info.put("error", ex.getMessage());
        }
        return info;
    }

    private List<String> filterExclude(List<String> tables, List<String> exclude) {
        if (exclude == null || exclude.isEmpty()) return new ArrayList<>(tables);
        Set<String> ex = exclude.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        return tables.stream()
                .filter(t -> !ex.contains(t.toLowerCase()))
                .collect(Collectors.toList());
    }
}
