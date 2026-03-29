package com.gener.qlbh.dtos.request;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class CategoryCreateReq {
    private String name;
    private String defaultBaseUnit;
}
