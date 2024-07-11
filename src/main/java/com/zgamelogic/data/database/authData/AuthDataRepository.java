package com.zgamelogic.data.database.authData;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthDataRepository extends JpaRepository<AuthData, AuthData.AuthDataId> {
    Optional<AuthData> findById_DiscordIdAndId_DeviceIdAndToken(Long id_discordId, String id_deviceId, String token);
    Optional<AuthData> findById_DiscordIdAndToken(Long id_discordId, String token);
    Optional<AuthData> findById_DeviceId(String id_deviceId);
    List<AuthData> findAllById_DiscordId(Long id_discordId);
}