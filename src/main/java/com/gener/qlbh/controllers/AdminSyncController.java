package com.gener.qlbh.controllers;

import com.gener.qlbh.enums.ErrorCode;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.services.DatabaseService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/admin/sync")
@RequiredArgsConstructor
public class AdminSyncController {

    private final DatabaseService databaseService;

    @PostMapping("/replace")
    public ResponseEntity<ResponseObject> replaceRemoteWithLocal(@RequestBody ReplaceRequest body,
                                                    @RequestParam(name = "dryRun", defaultValue = "true") boolean dryRun,
                                                    @RequestParam(name = "shutdownPool", defaultValue = "false") boolean shutdownPool) {
        List<String> tables = (body.getTables() == null || body.getTables().isEmpty())
                ? List.of("categories", "customer", "products", "inventory")
                : body.getTables();

        List<String> truncate = (body.getTruncate() == null || body.getTruncate().isEmpty())
                ? tables
                : body.getTruncate();

        return databaseService.replaceRemoteWithLocal(tables, truncate, dryRun, shutdownPool);
    }

    public static class ReplaceRequest {
        private List<String> tables;
        private List<String> truncate;
        public List<String> getTables() { return tables; }
        public void setTables(List<String> tables) { this.tables = tables; }
        public List<String> getTruncate() { return truncate; }
        public void setTruncate(List<String> truncate) { this.truncate = truncate; }
    }

    @PostMapping("/wipe")
    public ResponseEntity<ResponseObject> wipe(@RequestBody WipeRequest body) {
        // safety: require explicit confirm token
        if (body == null || !"DELETE_ALL".equals(body.getConfirm())) {
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                            .status(ErrorCode.BAD_REQUEST.getStatus())
                            .message("Missing or wrong confirm token. To proceed set confirm = 'DELETE_ALL'")
                    .build());
        }

        String target = body.getTarget() == null ? "remote" : body.getTarget();
        boolean dryRun = Boolean.TRUE.equals(body.getDryRun());
        List<String> exclude = body.getExclude();

        return databaseService.wipeDatabase(target, dryRun, exclude);
    }

    @Data
    public static class WipeRequest {
        private String target;
        private String confirm;
        private Boolean dryRun;
        private List<String> exclude;
    }
}
