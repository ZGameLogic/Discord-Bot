package bot.pokemon;

import controllers.discord.EmbedMessageGenerator;
import controllers.pokemon.PokemonAPI;
import controllers.pokemon.structures.Pokemon;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PokemonListener extends ListenerAdapter {
    private Logger logger = LoggerFactory.getLogger(PokemonListener.class);

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Pokemon listener ready to go!");
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        try{
            if(event.getName().equals("pokemon")){
                switch(event.getSubcommandName()){
                    case "lookup":
                        handlePokemonLookup(event);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("Unable to complete the command. If this continues to happen submit a bug report with the /bug-report command").setEphemeral(true).queue();
        }
    }

    private void handlePokemonLookup(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        Optional<Pokemon> pokemon = PokemonAPI.getByName(event.getOption("name").getAsString());
        if(pokemon.isPresent()){
            event.getHook().sendMessageEmbeds(EmbedMessageGenerator.generate(pokemon.get())).queue();
        } else {
            event.getHook().sendMessage("Unable to find pokemon: " + event.getOption("name").getAsString()).setEphemeral(true).queue();
        }
    }
}
