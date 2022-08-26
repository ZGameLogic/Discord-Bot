package bot.role.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EmoteCommand {
    String categoryFrom();
    String channelFrom() default "none";
    boolean isFromGuildOwner() default false;
}
