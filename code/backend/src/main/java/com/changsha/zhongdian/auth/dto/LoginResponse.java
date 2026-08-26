package com.changsha.zhongdian.auth.dto;

import com.changsha.zhongdian.auth.model.LoginUser;

public record LoginResponse(
        String token,
        LoginUser user
) {
}
