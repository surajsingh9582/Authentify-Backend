package com.authntify.service;

import com.authntify.entity.UserEntity;
import com.authntify.io.ProfileRequest;
import com.authntify.io.ProfileResponse;
import com.authntify.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        UserEntity user=convertToUserEntity(request);
        if(!userRepository.existsByEmail(request.getEmail())){
            user=userRepository.save(user);
            return convertToProfileResponse(user);
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already exists");
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity existingUser=userRepository.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User not found "+email));
        return convertToProfileResponse(existingUser);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity user=userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found"));
        String otp=String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));
        Long expiryTime=System.currentTimeMillis()+(15*60*1000);

        user.setResetOtp(otp);
        user.setResetOtpExpireAt(expiryTime);

        userRepository.save(user);
        try{
            emailService.sendResetOtpEmail(user.getEmail(), otp);
        }catch (Exception e){
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        UserEntity user=userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found"));
        if(user.getResetOtp()==null || !user.getResetOtp().equals(otp)){
            throw new RuntimeException("OTP invalid");
        }
        if(user.getResetOtpExpireAt()<System.currentTimeMillis()){
            throw new RuntimeException("Expired OTP");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp("");
        user.setResetOtpExpireAt(0L);
        userRepository.save(user);
    }

    @Override
    public void sendOtp(String email) {
        UserEntity user=userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found"));
        if(user.getIsAccountVerified()!=null && user.getIsAccountVerified()){
            return;
        }
        String otp=String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));
        Long expiryTime=System.currentTimeMillis()+(24*60*60*1000);
        user.setVerifyOtp(otp);
        user.setVerifyOtpExpireAt(expiryTime);
        userRepository.save(user);
        try{
            emailService.sentOtpEmail(email,otp);
        }catch (Exception e){
            throw new RuntimeException("Unable to send email");
        }
    }

    @Override
    public void verifyOtp(String email,String otp) {
        UserEntity user=userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found"));
        if(user.getVerifyOtp()==null || !user.getVerifyOtp().equals(otp)){
            throw new RuntimeException("Otp Invalid");
        }
        if(user.getVerifyOtpExpireAt()<System.currentTimeMillis()){
            throw new RuntimeException("Otp expired");
        }
        user.setIsAccountVerified(true);
        user.setVerifyOtp(null);
        user.setVerifyOtpExpireAt(0L);

        userRepository.save(user);
    }


    private ProfileResponse convertToProfileResponse(UserEntity user) {
            return ProfileResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .userId(user.getUserId())
                    .isAccountVerified(user.getIsAccountVerified())
                    .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtp(null)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .build();
    }
}
