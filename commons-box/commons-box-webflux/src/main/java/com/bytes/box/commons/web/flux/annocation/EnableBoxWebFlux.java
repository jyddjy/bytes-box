package com.bytes.box.commons.web.flux.annocation;


import com.bytes.box.commons.base.annocation.EnableBox;
import com.bytes.box.commons.base.resolve.FeignClientBodyResolve;
import com.bytes.box.commons.web.flux.config.DefaultWebFluxConfig;
import com.bytes.box.commons.web.flux.resolve.WebFluxExceptionResolve;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Inherited
@EnableBox(imports = {
        DefaultWebFluxConfig.class,
        WebFluxExceptionResolve.class,
        FeignClientBodyResolve.class
})
public @interface EnableBoxWebFlux {
}
