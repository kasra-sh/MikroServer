package ir.kasra_sh.MikroServer.Utils.eson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by blkr on 4/5/18.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JSONName {
    String name();
    boolean required() default false;
}
