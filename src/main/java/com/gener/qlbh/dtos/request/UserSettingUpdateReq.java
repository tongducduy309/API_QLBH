package com.gener.qlbh.dtos.request;

import com.gener.qlbh.entities.PrintOptions;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserSettingUpdateReq {


    private Boolean emailNotify;

    private PrintOptions printOptions;
}

