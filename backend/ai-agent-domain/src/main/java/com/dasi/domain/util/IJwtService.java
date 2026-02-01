package com.dasi.domain.util;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.dasi.domain.auth.model.vo.UserVO;

public interface IJwtService {

    String generateToken(UserVO userVO);

    UserVO parseToken(String token);

    DecodedJWT verifyToken(String token);

}
