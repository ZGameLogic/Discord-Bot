package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import data.database.devopsData.DevopsData;
import data.database.devopsData.DevopsDataRepository;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class DevopsBot extends AdvancedListenerAdapter {

    private final DevopsDataRepository devopsDataRepository;
    private final GuildDataRepository guildDataRepository;

    public DevopsBot(DevopsDataRepository devopsDataRepository, GuildDataRepository guildDataRepository) {
        this.devopsDataRepository = devopsDataRepository;
        this.guildDataRepository = guildDataRepository;
    }

    @ButtonResponse("enable_devops")
    private void enableDevops(ButtonInteractionEvent event){

    }

    @ButtonResponse("disable_devops")
    private void disableDevops(ButtonInteractionEvent event){
        event.editButton(Button.danger("enable_devops", "Devops bot")).queue();
        Guild guild = event.getGuild();
        GuildData gd = guildDataRepository.getOne(guild.getIdLong());
        DevopsData dd = devopsDataRepository.getOne(guild.getIdLong());
        gd.setDevopsEnabled(false);
        guild.getTextChannelById(dd.getDevopsGeneralChatId()).delete().queue();
        guild.getTextChannelById(dd.getDevopsGeneralTextId()).delete().queue();
        guild.deleteCommandById(dd.getCreateBranchSlashId()).queue();
        guild.getCategoryById(dd.getDevopsCatId()).delete().queue();
        guildDataRepository.save(gd);
        devopsDataRepository.deleteById(guild.getIdLong());
    }
}
