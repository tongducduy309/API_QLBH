package com.gener.qlbh.repositories;

import com.gener.qlbh.models.User;
import com.gener.qlbh.models.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    Optional<UserSetting> findByUser(User user);
}