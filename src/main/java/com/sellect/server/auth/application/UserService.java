package com.sellect.server.auth.application;

import com.sellect.server.auth.controller.response.UserInfoResponse;
import com.sellect.server.auth.domain.User;
import com.sellect.server.auth.domain.UserAuth;
import com.sellect.server.auth.repository.user.UserAuthRepository;
import com.sellect.server.auth.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    public UserInfoResponse getUserInfo(User user) {
        return new UserInfoResponse(user.getNickname());
    }

    @Transactional
    public void leave(User user) {
        User deletedUser = user.delete();
        UserAuth byUser = userAuthRepository.findByUser(deletedUser);
        UserAuth deletedUserAuth = byUser.delete();
        userRepository.save(deletedUser);
        userAuthRepository.save(deletedUserAuth);
    }
}
