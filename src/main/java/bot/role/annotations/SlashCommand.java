package bot.role.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SlashCommand {
    String CommandName();
    boolean KingOnly() default false;
    boolean positiveGold() default false;
    boolean warChannelOnly() default false;
    int activityCheck() default 0;
    boolean validCasteRole() default false;
    boolean isSubCommand() default false;
    String subCommandName() default "";
    boolean isInGuild() default false;
    boolean isNotInGuild() default false;
    boolean isLeaderOfGuild() default false;
}
