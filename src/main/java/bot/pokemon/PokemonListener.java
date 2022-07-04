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
        if(event.getName().equals("pokemon")){
            switch(event.getSubcommandName()){
                case "lookup":
                    handlePokemonLookup(event);
                    break;
            }
        }
    }

    private void handlePokemonLookup(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        Optional<Pokemon> pokemon = PokemonAPI.getByName(event.getOption("name").getAsString(), "black");
        if(pokemon.isPresent()){
            event.getHook().sendMessageEmbeds(EmbedMessageGenerator.generate(pokemon.get())).queue();
        } else {
            event.getHook().sendMessage("Unable to find pokemon: " + event.getOption("name").getAsString()).setEphemeral(true).queue();
        }
    }
}
