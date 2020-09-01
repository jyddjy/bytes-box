package com.bytes.box.commons.web.mvc.resolve;

import com.bytes.box.commons.base.annocation.EnableBox;
import com.bytes.box.commons.base.response.DefaultException;
import com.bytes.box.commons.base.response.DefineRestCode;
import com.bytes.box.commons.base.response.RestCode;
import com.bytes.box.commons.base.response.RestResponse;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;

/**
 * {@link EnableBox#imports()} 导入当前异常处理
 */
@Getter
@Slf4j
@Import(value = {WebMvcExceptionResolve.ExceptionResolve.class})
public class WebMvcExceptionResolve {


    @ControllerAdvice
    @Order(-1)
    public static class ExceptionResolve implements EnvironmentAware {

        public static final String RESP_EXCEPTION = "RESP-EXCEPTION";

        public ExceptionResolve() {
            log.info("init web mvc exception resolve...");
        }

        private Environment environment;

        private String getService() {
            return environment.getProperty("spring.application.name");
        }

        private RestResponse fetch(DefineRestCode defineRestCode, HttpServletRequest request, HttpServletResponse response, Exception e) {
            Pair<String, String> pair = defineRestCode.getPair();
            response.setHeader(RESP_EXCEPTION, RESP_EXCEPTION);
            log.error("exception: ", e);
            return RestResponse.error(pair.getKey(), StringUtils.firstNonBlank(pair.getValue(), ExceptionUtils.getMessage(e)), request.getServletPath(), getService());
        }

        @ExceptionHandler(DefaultException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public RestResponse businessExceptionHandler(HttpServletRequest request,
                                                     HttpServletResponse response,
                                                     DefaultException e) {
            return fetch(e.getRestCode(), request, response, e);
        }

        @ExceptionHandler(Exception.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public RestResponse defaultExceptionHandler(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    Exception e) {
            return fetch(RestCode.ERROR, request, response, e);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.BAD_REQUEST)
        public RestResponse illegalArgumentExceptionHandler(HttpServletRequest request,
                                                            HttpServletResponse response,
                                                            IllegalArgumentException e) {
            String errorMsg = Strings.isNullOrEmpty(e.getMessage()) ? "参数错误" : e.getMessage();
            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder().code(RestCode.ERROR.getCode()).message(errorMsg).build();
            return fetch(restCode, request, response, e);
        }

        //不支持的请求方法. 例如要求的是Post请求. 调用者发起的是Get请求
        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
        public RestResponse requestMethodNotSupported(HttpServletRequest request,
                                                      HttpServletResponse response,
                                                      HttpRequestMethodNotSupportedException e) {

            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.METHOD_NOT_ALLOWED.getCode())
                            .message("不支持" + e.getMethod() + "请求").build();

            return fetch(restCode, request, response, e);
        }

        //不支持的mediaType, 如请求期望的是@ReqeustBody. 但是传入的Context-Type是text/plain
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        public RestResponse mediaTypeNotSupported(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  HttpMediaTypeNotSupportedException e) {

            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.UNSUPPORTED_MEDIA_TYPE.getCode())
                            .message("不支持Type为 " + e.getContentType() + " 的请求").build();

            return fetch(restCode, request, response, e);
        }

        //404 请求path无效
        @ExceptionHandler(value = {NoHandlerFoundException.class})
        @ResponseBody
        //@ResponseStatus(HttpStatus.NOT_FOUND)
        public RestResponse noHandlerExceptionHandler(HttpServletRequest request,
                                                      HttpServletResponse response, NoHandlerFoundException e) {
            //不打印日志. 经常有访问首页的请求
            return fetch(RestCode.NOT_FOUND, request, response, e);
        }

        //请求body转换错误, JSONObject.parse参数类型失败
        @ExceptionHandler(HttpMessageNotReadableException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.BAD_REQUEST)
        public RestResponse httpMessageNotReadableExceptionHandler(HttpServletRequest request,
                                                                   HttpServletResponse response,
                                                                   HttpMessageNotReadableException e) {

            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.BAD_REQUEST.getCode())
                            .message("请求无效, 参数错误").build();

            return fetch(restCode, request, response, e);
        }

        //LocalDateTime类型错误
        @ExceptionHandler(TypeMismatchException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.BAD_REQUEST)
        public RestResponse typeMismatchExceptionHandler(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         TypeMismatchException e) {

            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.BAD_REQUEST.getCode())
                            .message("请求无效, 参数类型错误").build();

            return fetch(restCode, request, response, e);
        }

        //包装类型参数缺失
        @ExceptionHandler(ConstraintViolationException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.BAD_REQUEST)
        public RestResponse constraintViolationExceptionHandler(HttpServletRequest request,
                                                                HttpServletResponse response,
                                                                ConstraintViolationException e) {
            String errorMsg = e.getConstraintViolations().stream()
                    .findFirst()
                    .map(ex -> buildPrettyMessage(shortPropertyPath(ex.getPropertyPath()) + " " + ex.getMessage()))
                    .orElse(e.getMessage());

            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.BAD_REQUEST.getCode())
                            .message(errorMsg).build();

            return fetch(restCode, request, response, e);
        }

        private String buildPrettyMessage(String message) {
            return StringUtils.replace(message, "null", "空");
        }

        private String shortPropertyPath(Path path) {
            String s = StringUtils.substringAfterLast(path.toString(), ".");
            if (StringUtils.isEmpty(s)) {
                // 如果没有".", 返回原始路径
                return path.toString();
            }
            return s;
        }

        //对象类型参数错误
        @ExceptionHandler(BindException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.BAD_REQUEST)
        public RestResponse bindExceptionExceptionHandler(HttpServletRequest request,
                                                          HttpServletResponse response,
                                                          BindException e) {
            FieldError fieldError = e.getBindingResult().getFieldError();

            DefineRestCode.NewRestCode.NewRestCodeBuilder restCodeBuilder =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.BAD_REQUEST.getCode());

            if (fieldError.contains(TypeMismatchException.class)) {
                String errorMsg = String.format("参数类型错误, 参数名: %s", fieldError.getField());
                restCodeBuilder.message(errorMsg);
            } else {
                String errorMsg = fieldError.getField() + " " + fieldError.getDefaultMessage();
                restCodeBuilder.message(errorMsg);
            }

            return fetch(restCodeBuilder.build(), request, response, e);
        }

        //包装类型参数绑定失败
        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        @ResponseBody
        public RestResponse methodArgumentTypeMismatchExceptionNotReadable(HttpServletRequest request,
                                                                           HttpServletResponse response,
                                                                           MethodArgumentTypeMismatchException e) {

            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.BAD_REQUEST.getCode())
                            .message(String.format("参数类型错误, 参数名: %s", e.getName())).build();

            return fetch(restCode, request, response, e);
        }

        //包装类型参数校验失败
        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.BAD_REQUEST)
        public RestResponse methodArgumentNotValidExceptionExceptionNotReadable(HttpServletRequest request,
                                                                                HttpServletResponse response,
                                                                                MethodArgumentNotValidException e) {
            String errorMsg = e.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .orElse("参数校验失败");


            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.BAD_REQUEST.getCode())
                            .message(errorMsg).build();

            return fetch(restCode, request, response, e);
        }

        /**
         * MissingServletRequestParameterException 处理&#064;RequestParam注解参数缺失的异常
         *
         * @param request
         * @param e
         * @return ApiResult
         */
        @ExceptionHandler(MissingServletRequestParameterException.class)
        @ResponseBody
        //@ResponseStatus(HttpStatus.BAD_REQUEST)
        public RestResponse missingServletRequestParameterException(HttpServletRequest request,
                                                                    HttpServletResponse response,
                                                                    MissingServletRequestParameterException e) {
            String errorMsg = String.format("参数缺失[%s - %s]", e.getParameterName(), e.getParameterType());


            DefineRestCode.NewRestCode restCode =
                    DefineRestCode.NewRestCode.builder()
                            .code(RestCode.BAD_REQUEST.getCode())
                            .message(errorMsg).build();

            return fetch(restCode, request, response, e);
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }

    }

}
