package ir.kasra_sh.MikroServer.Server.Annotations;

import ir.kasra_sh.MikroServer.HTTPUtils.HTTPMethod;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Methods {
    HTTPMethod[] value() default {HTTPMethod.GET, HTTPMethod.POST, HTTPMethod.HEAD, HTTPMethod.OPTIONS};
}
