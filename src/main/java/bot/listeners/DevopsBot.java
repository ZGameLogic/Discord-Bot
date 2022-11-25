package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import data.database.devopsData.DevopsData;
import data.database.devopsData.DevopsDataRepository;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import interfaces.atlassian.BambooInterface;
import interfaces.atlassian.BitbucketInterface;
import interfaces.atlassian.JiraInterface;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class DevopsBot extends AdvancedListenerAdapter {

    private final DevopsDataRepository devopsDataRepository;
    private final GuildDataRepository guildDataRepository;

    public DevopsBot(DevopsDataRepository devopsDataRepository, GuildDataRepository guildDataRepository) {
        this.devopsDataRepository = devopsDataRepository;
        this.guildDataRepository = guildDataRepository;
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

    @ButtonResponse("enable_devops")
    private void enableDevops(ButtonInteractionEvent event){
        event.replyComponents(ActionRow.of(
                StringSelectMenu.create("devops_selection")
                        .addOption("Github", "github")
                        .addOption("Atlassian", "atlassian")
                        .build())
        ).setEphemeral(true).queue();
    }

    @StringSelectionResponse(value = "devops_selection", selectedOptionValue = "github")
    private void devopsSelectionGithub(StringSelectInteractionEvent event){
        event.reply("Github coming soon").setEphemeral(true).queue();
    }

    @StringSelectionResponse(value = "devops_selection", selectedOptionValue = "atlassian")
    private void devopsSelectionAtlassian(StringSelectInteractionEvent event){
        TextInput jira = TextInput.create("jira", "Jira base URL", TextInputStyle.SHORT).build();
        TextInput bitbucket = TextInput.create("bitbucket", "Bitbucket base URL", TextInputStyle.SHORT).build();
        TextInput bamboo = TextInput.create("bamboo", "Bamboo base URL", TextInputStyle.SHORT).build();
        event.replyModal(
                Modal.create("atlassian_URLS", "Atlassian URLS")
                        .addActionRow(jira)
                        .addActionRow(bitbucket)
                        .addActionRow(bamboo)
                        .build()
        ).queue();
    }

    @ModalResponse("atlassian_URLS")
    private void atlassianURLS(ModalInteractionEvent event){
        String jira = event.getValue("jira").getAsString();
        String bitbucket = event.getValue("bitbucket").getAsString();
        String bamboo = event.getValue("bamboo").getAsString();
        int jiraCheck = 0;
        int bambooCheck = 0;
        int bitbucketCheck = 0;
        InteractionHook message = event.reply(
                    atlassianCheckMessage(jiraCheck, bambooCheck, bitbucketCheck, "URLs")
                ).setEphemeral(true).complete();
        jiraCheck = JiraInterface.checkURL(jira) ? 1 : -1;
        message.editOriginal(atlassianCheckMessage(jiraCheck, bambooCheck, bitbucketCheck, "URLs")).complete();
        bambooCheck = BambooInterface.checkURL(bamboo) ? 1 : -1;
        message.editOriginal(atlassianCheckMessage(jiraCheck, bambooCheck, bitbucketCheck, "URLs")).complete();
        bitbucketCheck = BitbucketInterface.checkURL(bitbucket) ? 1 : -1;
        message.editOriginal(atlassianCheckMessage(jiraCheck, bambooCheck, bitbucketCheck, "URLs")).complete();
        if(jiraCheck == 1 && bambooCheck == 1 && bitbucketCheck == 1){
            DevopsData dd = new DevopsData()
                    .setId(event.getGuild().getIdLong())
                    .setJiraURL(jira)
                    .setBitbucketURL(bitbucket)
                    .setBambooURL(bamboo);
            devopsDataRepository.save(dd);
            message.sendMessage("Please create a personal access token in Jira, Bitbucket and Bamboo.")
                    .addActionRow(Button.primary("enter_PATs", "Enter PATs"))
                    .setEphemeral(true).queue();
        } else {
            message.sendMessage("Not all URLs were successful in creating a connection. Check your URLs and try again.").setEphemeral(true).queue();
        }
    }

    @ButtonResponse("enter_PATs")
    private void enterPatsButton(ButtonInteractionEvent event){
        TextInput jira = TextInput.create("jira", "Jira PAT", TextInputStyle.SHORT).build();
        TextInput bamboo = TextInput.create("bamboo", "Bamboo PAT", TextInputStyle.SHORT).build();
        TextInput bitbucket = TextInput.create("bitbucket", "Bitbucket PAT", TextInputStyle.SHORT).build();
        event.replyModal(Modal.create("atlassian_PATs", "Atlassian PATs")
                .addActionRow(jira)
                .addActionRow(bamboo)
                .addActionRow(bitbucket)
                .build()
        ).queue();
    }

    @ModalResponse("atlassian_PATs")
    private void PATsModalResponse(ModalInteractionEvent event){
        String jira = event.getValue("jira").getAsString();
        String bitbucket = event.getValue("bitbucket").getAsString();
        String bamboo = event.getValue("bamboo").getAsString();
        DevopsData dd = devopsDataRepository.getOne(event.getGuild().getIdLong());
        int jiraCheck = 0;
        int bambooCheck = 0;
        int bitbucketCheck = 0;
        InteractionHook message = event.reply(
                atlassianCheckMessage(jiraCheck, bambooCheck, bitbucketCheck, "PATs")
        ).setEphemeral(true).complete();
        jiraCheck = JiraInterface.checkPAT(jira, dd.getJiraURL()) ? 1 : -1;
        message.editOriginal(atlassianCheckMessage(jiraCheck, bambooCheck, bitbucketCheck, "PATs")).complete();
        bambooCheck = BambooInterface.checkPAT(bamboo, dd.getBambooURL()) ? 1 : -1;
        message.editOriginal(atlassianCheckMessage(jiraCheck, bambooCheck, bitbucketCheck, "PATs")).complete();
        bitbucketCheck = BitbucketInterface.checkPAT(bitbucket, dd.getBitbucketURL()) ? 1 : -1;
        message.editOriginal(atlassianCheckMessage(jiraCheck, bambooCheck, bitbucketCheck, "PATs")).complete();
        if(jiraCheck == 1 && bambooCheck == 1 && bitbucketCheck == 1){
            dd.setBambooPAT(bamboo)
                .setJiraPAT(jira)
                .setBitbucketPAT(bitbucket);
            devopsDataRepository.save(dd);
            message.sendMessage("Holy cow you did it right. Lets get your discord server setup.").setEphemeral(true).queue();

        } else {
            message.sendMessage("Not all PATs were successful in validating. Check your PATs and try again.").setEphemeral(true).queue();
        }
    }

    private String atlassianCheckMessage(int jira, int bamboo, int bitbucket, String step){
        return "Checking atlassian " + step + "...\n" +
                "Jira: " + atlassianCheckMessageEmoji(jira) + "\n" +
                "Bamboo: " + atlassianCheckMessageEmoji(bamboo) + "\n" +
                "Bitbucket:" + atlassianCheckMessageEmoji(bitbucket);
    }

    private String atlassianCheckMessageEmoji(int status){
        switch (status){
            case 1:
                return ":white_check_mark:";
            case -1:
                return ":no_entry_sign:";
            default:
                return ":hourglass_flowing_sand:";
        }
    }
}
