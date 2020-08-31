package com.bytes.box.commons.base.utils;


import com.bytes.box.commons.base.response.RestCode;

import java.util.Objects;

public class ExceptionUtils {

    public static void checkNonNull(Object object, String message) {
        if (Objects.isNull(object)) {
            throw RestCode.ERROR.fetchException();
        }
    }

}
