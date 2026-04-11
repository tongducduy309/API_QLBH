package com.gener.qlbh.dtos.response;

import com.gener.qlbh.entities.PrintOptions;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserSettingRes {
    private Long id;
    private Boolean emailNotify;
    private PrintOptions printOptions;
}