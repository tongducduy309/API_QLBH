package com.gener.qlbh.dtos.request;

import com.gener.qlbh.enums.Method;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class CategoryCreateReq {
    private String name;
    private Method method;
    private String defaultBaseUnit;
}
