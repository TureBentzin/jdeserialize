import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Retention(value = RetentionPolicy.RUNTIME)
public @interface blobann {
    int id();

    String sfoo();

    String sdefault() default "[unknown]";

    Class cl();
}
