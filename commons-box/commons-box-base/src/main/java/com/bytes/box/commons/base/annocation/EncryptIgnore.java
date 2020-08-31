package com.bytes.box.commons.base.annocation;

import java.lang.annotation.*;

/**
 * 标记方法不需要加密
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptIgnore {
}
