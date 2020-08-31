package com.bytes.box.commons.base.annocation;


import com.bytes.box.commons.base.BoxImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 标记开启公共包组件信息
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import(value = BoxImportSelector.class)
public @interface EnableBox {

    boolean encrypt() default false;

    boolean feignEncrypt() default false;

    Class<?>[] imports() default {};
}