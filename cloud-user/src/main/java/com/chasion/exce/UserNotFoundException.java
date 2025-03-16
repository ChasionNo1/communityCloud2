package com.chasion.exce;

import com.chasion.exc.BaseException;
import com.chasion.resp.ReturnCodeEnum;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String code, String message) {
        super(ReturnCodeEnum.RC500.getCode(), ReturnCodeEnum.RC500.getMessage());
    }
}
