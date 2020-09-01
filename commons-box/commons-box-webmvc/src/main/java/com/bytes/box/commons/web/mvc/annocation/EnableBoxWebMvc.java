package com.bytes.box.commons.web.mvc.annocation;

import com.bytes.box.commons.base.annocation.EnableBox;
import com.bytes.box.commons.base.resolve.FeignClientBodyResolve;
import com.bytes.box.commons.web.mvc.config.DefaultWebMvcConfig;
import com.bytes.box.commons.web.mvc.resolve.WebMvcClientBodyResolve;
import com.bytes.box.commons.web.mvc.resolve.WebMvcExceptionResolve;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Inherited
@EnableBox(imports = {
        DefaultWebMvcConfig.class,
        WebMvcExceptionResolve.class,
        WebMvcClientBodyResolve.class,
        FeignClientBodyResolve.class
})
public @interface EnableBoxWebMvc {
}
