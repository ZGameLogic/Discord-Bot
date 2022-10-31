package bot.utils;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * An advanced version of the ListenerAdapter class.
 * Highly annotative allowing automatic method calling.
 * @author Ben Shabowski
 */
public abstract class AdvancedListenerAdapter extends ListenerAdapter {

    /**
     * Annotation for ButtonInteractionEvent
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ButtonResponse {
        /**
         * ID of the button that was pressed
         * @return button Id
         */
        String buttonId();
    }

    /**
     * Annotation for onGenericMessageReaction
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface EmoteResponse {
        /**
         * Name of the reaction emoji
         * @return Name of the reaction emoji
         */
        String reactionName();

        /**
         * Boolean to trigger method if the reaction is being added.
         * @implNote True if method should trigger on reaction add.
         * False if the method should trigger on reaction remove.
         * @return if this method should be called if the reaction is added
         */
        boolean isAdding();
    }

    /**
     * Annotation for ModalInteractionEvent
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ModalResponse {
        /**
         * Name of the modal
         * @return name of the modal
         */
        String modalName();
    }

    /**
     * Annotation for SlashCommandInteractionEvent
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface SlashResponse {
        /**
         * Command name
         * @return Command name
         */
        String commandName();

        /**
         * Optional, use only if there is a subcommand
         * @return Subcommand name
         */
        String subCommandName() default "";
    }

    /**
     * Annotation for EntitySelectInteractionEvent
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface EntitySelectionResponse {
        /**
         * Menu Id for the menu
         * @return menu Id
         */
        String menuId();
    }

    /**
     * Annotation for StringSelectInteractionEvent
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface StringSelectionResponse {
        /**
         * Menu id for the menu
         * @return menu Id
         */
        String menuId();

        /**
         * Optional, use only if there is one option to select
         * @return selected value
         */
        String selectedOptionValue() default "";
    }

    private final static Logger logger = LoggerFactory.getLogger(AdvancedListenerAdapter.class);
    @Getter
    private final LinkedList<CommandData> globalSlashCommands;
    @Getter
    private final HashMap<String, LinkedList<CommandData>> guildSlashCommands;

    public AdvancedListenerAdapter(){
        globalSlashCommands = new LinkedList<>();
        guildSlashCommands = new HashMap<>();
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        logger.info("Registering methods for class: " + this.getClass().getName());
        for(Method m : getAnnotatedMethods()){
            logger.info("\t\t" + m.getName());
        }
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        for(Method m : this.getClass().getDeclaredMethods()){
            if(!m.isAnnotationPresent(StringSelectionResponse.class)) continue;
            StringSelectionResponse ssr = m.getAnnotation(StringSelectionResponse.class);
            if(!ssr.menuId().equals(event.getSelectMenu().getId())) continue;
            if(!ssr.selectedOptionValue().isEmpty() && !
                    ssr.selectedOptionValue().equals(event.getSelectedOptions().get(0).getValue())) continue;
            try {
                m.setAccessible(true);
                m.invoke(this, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onEntitySelectInteraction(@NotNull EntitySelectInteractionEvent event) {
        for(Method m : this.getClass().getDeclaredMethods()){
            if(!m.isAnnotationPresent(EntitySelectionResponse.class)) continue;
            EntitySelectionResponse esr = m.getAnnotation(EntitySelectionResponse.class);
            if(!esr.menuId().equals(event.getSelectMenu().getId())) continue;
            try {
                m.setAccessible(true);
                m.invoke(this, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        for(Method m : this.getClass().getDeclaredMethods()){
            if(!m.isAnnotationPresent(ModalResponse.class)) continue;
            ModalResponse mr = m.getAnnotation(ModalResponse.class);
            if(!mr.modalName().equals(event.getModalId())) continue;
            try {
                m.setAccessible(true);
                m.invoke(this, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        for(Method m : this.getClass().getDeclaredMethods()){
            if(!m.isAnnotationPresent(SlashResponse.class)) continue;
            SlashResponse sr = m.getAnnotation(SlashResponse.class);
            if(!sr.commandName().equals(event.getName())) continue;
            if(!sr.subCommandName().isEmpty() && !sr.subCommandName().equals(event.getSubcommandName())) continue;
            try {
                m.setAccessible(true);
                m.invoke(this, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        for(Method m : this.getClass().getDeclaredMethods()){
            if(!m.isAnnotationPresent(ButtonResponse.class)) continue;
            ButtonResponse br = m.getAnnotation(ButtonResponse.class);
            if(!br.buttonId().equals(event.getButton().getId())) continue;
            try {
                m.setAccessible(true);
                m.invoke(this, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event) {
        for(Method m : this.getClass().getDeclaredMethods()){
            if(!m.isAnnotationPresent(EmoteResponse.class)) continue;
            EmoteResponse er = m.getAnnotation(EmoteResponse.class);
            if(!er.reactionName().equals(event.getReaction().getEmoji().getName())) continue;
            if(er.isAdding() != event.getRawData().get("t").equals("MESSAGE_REACTION_ADD")) continue;
            try {
                m.setAccessible(true);
                m.invoke(this, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private LinkedList<Method> getAnnotatedMethods(){
        LinkedList<Method> methods = new LinkedList<>();
        for(Method m : this.getClass().getDeclaredMethods()){
            if(m.isAnnotationPresent(ButtonResponse.class) ||
                    m.isAnnotationPresent(EmoteResponse.class) ||
                    m.isAnnotationPresent(ModalResponse.class) ||
                    m.isAnnotationPresent(SlashResponse.class) ||
                    m.isAnnotationPresent(EntitySelectionResponse.class) ||
                    m.isAnnotationPresent(StringSelectionResponse.class)
            ){
                methods.add(m);
            }
        }
        return methods;
    }

    public void addGlobalCommands(CommandData ... data){
        Collections.addAll(globalSlashCommands, data);
    }

    public void addGlobalCommand(CommandData data){
        globalSlashCommands.add(data);
    }

    public void addGuildCommands(String id, CommandData ... data){
        for(CommandData d : data){
            addGuildCommand(id, d);
        }
    }

    public void addGuildCommand(String id, CommandData data){
        if(guildSlashCommands.containsKey(id)){
            guildSlashCommands.get(id).add(data);
        } else {
            LinkedList<CommandData> commands = new LinkedList<>();
            commands.add(data);
            guildSlashCommands.put(id, commands);
        }
    }
}
