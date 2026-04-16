package com.gener.qlbh.services;

import com.gener.qlbh.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InventoryCodeGeneratorService {

    private final InventoryRepository inventoryRepository;

    private static final DateTimeFormatter YYYYMM_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMM");

    @Transactional(readOnly = true)
    public String generateNextCode() {
        String yearMonth = LocalDate.now().format(YYYYMM_FORMAT);
        String prefix = "INV-" + yearMonth + "-";

        String maxCode = inventoryRepository.findMaxInventoryCodeByPrefix(prefix)
                .orElse(null);

        int nextSequence = 1;

        if (maxCode != null && maxCode.startsWith(prefix)) {
            String sequencePart = maxCode.substring(prefix.length());
            try {
                nextSequence = Integer.parseInt(sequencePart) + 1;
            } catch (NumberFormatException e) {
                nextSequence = 1;
            }
        }

        return prefix + String.format("%04d", nextSequence);
    }

    public String generateUniqueInventoryCode() {
        String code;
        do {
            code = generateNextCode();
        } while (inventoryRepository.existsByInventoryCode(code));
        return code;
    }
}