package com.gener.qlbh.services;

import com.gener.qlbh.dtos.request.UserSettingUpdateReq;
import com.gener.qlbh.dtos.response.UserSettingRes;
import com.gener.qlbh.exception.APIException;
import com.gener.qlbh.models.ResponseObject;
import com.gener.qlbh.models.User;
import com.gener.qlbh.models.UserSetting;
import com.gener.qlbh.repositories.UserRepository;
import com.gener.qlbh.repositories.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final AuthencationService authencationService;

    public ResponseEntity<ResponseObject> getMySettings() throws APIException {
        User user = authencationService.getUserFromToken();

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultSetting(user));

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(200)
                        .message("Lấy cài đặt thành công")
                        .data(mapToRes(setting))
                        .build()
        );
    }

    public ResponseEntity<ResponseObject> updateMySettings(UserSettingUpdateReq req) throws APIException {
        User user = authencationService.getUserFromToken();

        UserSetting setting = userSettingRepository.findByUser(user)
                .orElseGet(() -> createDefaultSetting(user));

        setting.setAppName(req.getAppName() == null || req.getAppName().isBlank()
                ? "Quản lý bán hàng"
                : req.getAppName().trim());

        setting.setAppIcon(req.getAppIcon());

        if (req.getEmailNotify() != null) {
            setting.setEmailNotify(req.getEmailNotify());
        }

        if (req.getDesktopNotify() != null) {
            setting.setDesktopNotify(req.getDesktopNotify());
        }

        userSettingRepository.save(setting);

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .status(200)
                        .message("Cập nhật cài đặt thành công")
                        .data(mapToRes(setting))
                        .build()
        );
    }

    private UserSetting createDefaultSetting(User user) {
        UserSetting setting = UserSetting.builder()
                .user(user)
                .appName("Quản lý bán hàng")
                .appIcon(null)
                .emailNotify(true)
                .desktopNotify(false)
                .build();

        return userSettingRepository.save(setting);
    }

    private UserSettingRes mapToRes(UserSetting setting) {
        return UserSettingRes.builder()
                .id(setting.getId())
                .appName(setting.getAppName())
                .appIcon(setting.getAppIcon())
                .emailNotify(setting.getEmailNotify())
                .desktopNotify(setting.getDesktopNotify())
                .build();
    }
}