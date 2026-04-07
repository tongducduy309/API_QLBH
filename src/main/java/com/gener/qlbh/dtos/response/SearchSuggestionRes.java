package com.gener.qlbh.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionRes {
    private String entityId;
    private String entityType;
    private String entityLabel;
    private String title;
    private String subtitle;
    private String meta;
}