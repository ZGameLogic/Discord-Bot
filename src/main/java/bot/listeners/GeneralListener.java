package bot.listeners;

import bot.utils.AdvancedListenerAdapter;
import data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;

public class GeneralListener extends AdvancedListenerAdapter {

    private GuildDataRepository guildData;
    public GeneralListener(GuildDataRepository guildData){
        this.guildData = guildData;
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {

    }
}
