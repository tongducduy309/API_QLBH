package com.gener.qlbh.services;

import com.gener.qlbh.repositories.OrderSequenceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class OrderNumberService {

    private final OrderSequenceRepository repo;

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("MMyyyy");

    /** Sinh mã đơn dạng MMyyyy/000001, reset mỗi tháng */
    @Transactional
    public String nextOrderCode() {
        String period = LocalDate.now().format(PERIOD_FMT);

        int updated = repo.increment(period);

        if (updated == 0) {
            try {
                repo.insertInitial(period);
            } catch (DataIntegrityViolationException e) {
                repo.increment(period);
            }
        }

        Long val = repo.findNextVal(period);
        String six = String.format("%06d", val);
        return period + "-" + six;
    }
}
