package com.bytes.box.commons.base.response;

import lombok.Getter;

@Getter
public class DefaultException extends RuntimeException {

    public DefaultException(RestCode restCode) {
        super(restCode.getMessage());
        this.restCode = restCode;
    }

    private RestCode restCode;
}
