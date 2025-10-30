package com.gener.qlbh.dtos.response;

import com.gener.qlbh.interfaces.RevenueBucket;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesAnalysisResponse {

    private AnalysisRes analysisRes;
    private List<RevenueBucketRes> buckets;
}
