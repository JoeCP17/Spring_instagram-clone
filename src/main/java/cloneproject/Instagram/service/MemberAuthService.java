package cloneproject.Instagram.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cloneproject.Instagram.dto.member.JwtDto;
import cloneproject.Instagram.dto.member.LoginRequest;
import cloneproject.Instagram.dto.member.LoginWithCodeRequest;
import cloneproject.Instagram.dto.member.LoginedDevicesDTO;
import cloneproject.Instagram.dto.member.RegisterRequest;
import cloneproject.Instagram.dto.member.ResetPasswordRequest;
import cloneproject.Instagram.dto.member.UpdatePasswordRequest;
import cloneproject.Instagram.entity.member.Member;
import cloneproject.Instagram.entity.redis.RefreshToken;
import cloneproject.Instagram.entity.search.SearchMember;
import cloneproject.Instagram.exception.AccountDoesNotMatchException;
import cloneproject.Instagram.exception.CantResetPasswordException;
import cloneproject.Instagram.exception.InvalidJwtException;
import cloneproject.Instagram.exception.MemberDoesNotExistException;
import cloneproject.Instagram.exception.UseridAlreadyExistException;
import cloneproject.Instagram.repository.MemberRepository;
import cloneproject.Instagram.repository.search.SearchMemberRepository;
import cloneproject.Instagram.util.JwtUtil;
import cloneproject.Instagram.vo.GeoIP;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtil jwtUtil;
    private final EmailCodeService emailCodeService;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;
    private final GeoIPLocationService geoIPLocationService;
    private final SearchMemberRepository searchMemberRepository;
    
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    
    @Transactional(readOnly = true)
    public boolean checkUsername(String username){
        if(memberRepository.existsByUsername(username)){
            return false;
        }
        return true;
    }

    @Transactional
    public boolean register(RegisterRequest registerRequest){
        if(memberRepository.existsByUsername(registerRequest.getUsername())){
            throw new UseridAlreadyExistException();
        }
        String username = registerRequest.getUsername();
        
        if(!emailCodeService.checkEmailCode(username, registerRequest.getEmail(), registerRequest.getCode())){
            return false;
        }
        
        Member member = registerRequest.convert();
        String encryptedPassword = bCryptPasswordEncoder.encode(member.getPassword());
        member.setEncryptedPassword(encryptedPassword);
        memberRepository.save(member);

        SearchMember searchMember = new SearchMember(member);
        searchMemberRepository.save(searchMember);

        return true;
    }

    public void sendEmailConfirmation(String username, String email){
        if(memberRepository.existsByUsername(username)){
            throw new UseridAlreadyExistException();
        }
        emailCodeService.sendEmailConfirmationCode(username, email);
    }

    @Transactional
    public JwtDto login(LoginRequest loginRequest, String device, String ip){
        try{
            UsernamePasswordAuthenticationToken authenticationToken = 
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            JwtDto jwtDto = jwtUtil.generateTokenDto(authentication);
            Member member = memberRepository.findById(Long.valueOf(authentication.getName()))
                                            .orElseThrow(MemberDoesNotExistException::new);
            memberRepository.save(member);
            
            GeoIP geoIP = geoIPLocationService.getLocation(ip);

            refreshTokenService.addRefreshToken(member.getId(), jwtDto.getRefreshToken(), device, geoIP);
            return jwtDto;
        }catch(BadCredentialsException e){
            throw new AccountDoesNotMatchException();
        }
    }

    @Transactional
    public Optional<JwtDto> reisuue(String refreshTokenString){
        if(!jwtUtil.validateRefeshJwt(refreshTokenString)){
            throw new InvalidJwtException();
        }
        Authentication authentication;
        try{
            authentication = jwtUtil.getAuthentication(refreshTokenString, false);
        } catch(JwtException e){
            throw new InvalidJwtException();
        }
        Member member = memberRepository.findById(Long.valueOf(authentication.getName()))
            .orElseThrow(MemberDoesNotExistException::new);
        
        Optional<RefreshToken> refreshToken = refreshTokenService.findRefreshToken(member.getId(), refreshTokenString);
        if(refreshToken.isEmpty()){
            return Optional.empty();
        }
        JwtDto jwtDto = jwtUtil.generateTokenDto(authentication);
        refreshTokenService.updateRefreshToken(refreshToken.get(), jwtDto.getRefreshToken());
        return Optional.of(jwtDto);
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequest updatePasswordRequest){
        final String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findById(Long.valueOf(memberId))
                                    .orElseThrow(MemberDoesNotExistException::new);
        boolean oldPasswordCorrect = bCryptPasswordEncoder.matches(updatePasswordRequest.getOldPassword()
            , member.getPassword());
        if(!oldPasswordCorrect){
            throw new AccountDoesNotMatchException();
        }
        String encryptedPassword = bCryptPasswordEncoder.encode(updatePasswordRequest.getNewPassword());
        member.setEncryptedPassword(encryptedPassword);              
        memberRepository.save(member);
    }

    @Transactional
    public String sendResetPasswordCode(String username){
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(MemberDoesNotExistException::new);
        emailCodeService.sendResetPasswordCode(username);
        return member.getEmail();
    }

    @Transactional
    public JwtDto resetPassword(ResetPasswordRequest resetPasswordRequest, String device, String ip){
        Member member = memberRepository.findByUsername(resetPasswordRequest.getUsername())
                                    .orElseThrow(MemberDoesNotExistException::new);
        if(!emailCodeService.checkResetPasswordCode(resetPasswordRequest.getUsername(), resetPasswordRequest.getCode())){
            throw new CantResetPasswordException();
        }
        String encryptedPassword = bCryptPasswordEncoder.encode(resetPasswordRequest.getNewPassword());
        member.setEncryptedPassword(encryptedPassword);              
        memberRepository.save(member);
        JwtDto jwtDto = login(
            new LoginRequest(resetPasswordRequest.getUsername(), resetPasswordRequest.getNewPassword())
            ,device
            ,ip );
        emailCodeService.deleteResetPasswordCode(resetPasswordRequest.getUsername());
        return jwtDto;
    }

    @Transactional
    public JwtDto loginWithCode(LoginWithCodeRequest loginRequest, String device, String ip){
        Member member = memberRepository.findByUsername(loginRequest.getUsername())
                                    .orElseThrow(MemberDoesNotExistException::new);
        if(!emailCodeService.checkResetPasswordCode(loginRequest.getUsername(), loginRequest.getCode())){
            throw new CantResetPasswordException();
        }
        JwtDto jwtDto = jwtUtil.generateTokenDto(jwtUtil.getAuthenticationWithMember(member.getId().toString()));
        emailCodeService.deleteResetPasswordCode(loginRequest.getUsername());

        memberRepository.save(member);
        
        GeoIP geoIP = geoIPLocationService.getLocation(ip);

        refreshTokenService.addRefreshToken(member.getId(), jwtDto.getRefreshToken(), device, geoIP);
        return jwtDto;
    }

    @Transactional
    public boolean checkResetPasswordCode(String username, String code){
        return emailCodeService.checkResetPasswordCode(username, code);
    }

    @Transactional
    public void logout(String refreshToken){
        final String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        refreshTokenService.deleteRefreshTokenWithValue(Long.valueOf(memberId), refreshToken);
    }

    public List<LoginedDevicesDTO> getLoginedDevices(){
        final String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findById(Long.valueOf(memberId))
            .orElseThrow(MemberDoesNotExistException::new);
        return refreshTokenService.getLoginedDevices(member.getId());
    }

    @Transactional
    public void logoutDevice(String tokenId){
        final String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        refreshTokenService.deleteRefreshTokenWithId(Long.valueOf(memberId), tokenId);
    }
}
