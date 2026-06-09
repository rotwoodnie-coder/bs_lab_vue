package com.xuanyue.exp.mobile.repository;

import com.xuanyue.exp.mobile.entity.MbAuthRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MbAuthRefreshTokenRepository extends JpaRepository<MbAuthRefreshToken, Long> {

    Optional<MbAuthRefreshToken> findByTokenHashAndRevokedFalse(String tokenHash);

    void deleteByUserIdAndDeviceId(String userId, String deviceId);
}
