package com.sellect.server.auth.application;

import com.sellect.server.auth.controller.response.UserInfoResponse;
import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.repository.user.UserAuthRepository;
import com.sellect.server.auth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    public UserInfoResponse getUserInfo(User user) {
        return new UserInfoResponse(user.getNickname());
    }

    public void leave(User user) {
        user.delete();
        userRepository.save(user);
    }
}
