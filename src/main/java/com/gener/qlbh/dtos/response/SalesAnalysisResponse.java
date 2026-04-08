package com.gener.qlbh.dtos.response;

import lombok.*;

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