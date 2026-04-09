package com.gener.qlbh.dtos.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserSettingRes {
    private Long id;
    private String appName;
    private String appIcon;
    private Boolean emailNotify;
    private Boolean desktopNotify;
}