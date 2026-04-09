package com.gener.qlbh.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserSettingUpdateReq {

    @NotBlank(message = "Tên ứng dụng không được để trống")
    private String appName;

    private String appIcon;

    private Boolean emailNotify;

    private Boolean desktopNotify;
}