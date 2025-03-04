package cloneproject.Instagram.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cloneproject.Instagram.dto.member.LoginedDevicesDTO;
import cloneproject.Instagram.entity.redis.RefreshToken;
import cloneproject.Instagram.exception.InvalidJwtException;
import cloneproject.Instagram.repository.RefreshTokenRedisRepository;
import cloneproject.Instagram.vo.GeoIP;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    
    @Value("${max-login-device}")
    private long MAX_LOGIN_DEVICE;

    @Value("${refresh-token-expires}")
    private long REFRESH_TOKEN_EXPIRES;
    
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    @Transactional
    public void addRefreshToken(Long memberId, String tokenValue, String device, GeoIP geoIP){
        List<RefreshToken> refreshTokens = refreshTokenRedisRepository.findByMemberId(memberId)
            .stream()
            .sorted(Comparator.comparing(RefreshToken::getCreatedAt))
            .collect(Collectors.toList());

        for(int i = 0; i <= refreshTokens.size()-MAX_LOGIN_DEVICE; ++i){
            refreshTokenRedisRepository.deleteById(refreshTokens.get(i).getId());
            refreshTokens.remove(i);
        }

        RefreshToken refreshToken = RefreshToken.builder()
            .memberId(memberId)
            .value(tokenValue)
            .device(device)
            .geoip(geoIP)
            .build();

        refreshTokenRedisRepository.save(refreshToken);
    }
    
    @Transactional
    public Optional<RefreshToken> findRefreshToken(Long memberId, String value){
        return refreshTokenRedisRepository.findByMemberIdAndValue(memberId, value);
    }

    @Transactional
    public void updateRefreshToken(RefreshToken refreshToken, String newToken){
        RefreshToken newRefreshToken = RefreshToken.builder()
            .memberId(refreshToken.getMemberId())
            .value(newToken)
            .device(refreshToken.getDevice())
            .geoip(refreshToken.getGeoIP())
            .build();
        refreshTokenRedisRepository.delete(refreshToken);
        refreshTokenRedisRepository.save(newRefreshToken);
    }

    @Transactional
    public void deleteRefreshTokenWithValue(Long memberId, String value){
        RefreshToken refreshToken = refreshTokenRedisRepository.findByMemberIdAndValue(memberId, value)
            .orElseThrow(InvalidJwtException::new);
        refreshTokenRedisRepository.delete(refreshToken);
    }

    @Transactional
    public void deleteRefreshTokenWithId(Long memberId, String id){
        RefreshToken refreshToken = refreshTokenRedisRepository.findByMemberIdAndId(memberId, id)
            .orElseThrow(InvalidJwtException::new);
        refreshTokenRedisRepository.delete(refreshToken);
    }

    @Transactional(readOnly = true)
    public List<LoginedDevicesDTO> getLoginedDevices(Long memberId){
        List<RefreshToken> refreshTokens = refreshTokenRedisRepository.findByMemberId(memberId)
            .stream()
            .sorted(Comparator.comparing(RefreshToken::getCreatedAt).reversed())
            .collect(Collectors.toList());
        List<LoginedDevicesDTO> loginedDevicesDTOs = refreshTokens.stream()
            .map(this::convertRefreshTokenToLoginedDevicesDTO)
            .collect(Collectors.toList());
        return loginedDevicesDTOs;
    }
    
    private LoginedDevicesDTO convertRefreshTokenToLoginedDevicesDTO(RefreshToken refreshToken){
        return LoginedDevicesDTO.builder()
            .tokenId(refreshToken.getId())
            .device(refreshToken.getDevice())
            .location(refreshToken.getGeoIP())
            .build();
    }

}
