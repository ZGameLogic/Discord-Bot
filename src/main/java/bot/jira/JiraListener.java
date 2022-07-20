package bot.jira;

import controllers.atlassian.JiraInterfacer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

public class JiraListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch(event.getName()) {
            case "bug-report":
                submitBugReport(event);
                break;
        }
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        switch(event.getModalId()){
            case "support":
                String title = event.getValue("title").getAsString();
                String body = event.getValue("body").getAsString();
                String strc = event.getValue("strc").getAsString();
                String username = event.getMember().getEffectiveName();
                long userId = event.getMember().getUser().getIdLong();
                MessageEmbed message = JiraInterfacer.submitBug(title, body, strc, username, userId, "true");
                event.replyEmbeds(message).setEphemeral(true).queue();
                String issueNumber = message.getFooter().getText().replace("Issue: ", "");
                break;
            case "add_comment":
                String issue = event.getValues().get(0).getId();
                String comment = event.getValue(issue).getAsString();
                String user = event.getUser().getName();
                JiraInterfacer.addComment(issue, comment, user);
                event.reply("Comment has been added to the issue.").queue();
                break;
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if(event.getButton().getId().equals("comment_issue")){
            String issue = event.getMessage().getEmbeds().get(0).getTitle().split(" ")[1];
            TextInput comment = TextInput.create(issue, "Comment", TextInputStyle.PARAGRAPH)
                    .setRequired(true)
                    .build();
            Modal modal = Modal.create("add_comment", "Add comment")
                    .addActionRows(ActionRow.of(comment))
                    .build();
            event.replyModal(modal).queue();
        } else if(event.getButton().getId().equals("opt_out_issue_notif")){
            String issue = event.getMessage().getEmbeds().get(0).getFooter().getText().split(" ")[2];
            JiraInterfacer.optOut(issue);
            event.editButton(Button.primary("opt_in_issue_notif", "Opt in to notifications")).queue();
        } else if(event.getButton().getId().equals("opt_in_issue_notif")){
            String issue = event.getMessage().getEmbeds().get(0).getFooter().getText().split(" ")[2];
            JiraInterfacer.optIn(issue);
            event.editButton(Button.primary("opt_out_issue_notif", "Opt out of notifications")).queue();
        }
    }

    private void submitBugReport(SlashCommandInteractionEvent event) {
        TextInput title = TextInput.create("title", "Title of report", TextInputStyle.SHORT)
                .setPlaceholder("Title for the bug report")
                .setRequired(true)
                .build();
        TextInput body = TextInput.create("body", "Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Description of bug")
                .setMaxLength(1000)
                .setRequired(true)
                .build();
        TextInput strc = TextInput.create("strc", "Steps to recreate", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Description of what you were doing when you found the bug")
                .setMaxLength(1000)
                .setRequired(true)
                .build();

        Modal modal = Modal.create("support", "Support")
                .addActionRows(ActionRow.of(title), ActionRow.of(body), ActionRow.of(strc))
                .build();

        event.replyModal(modal).queue();
    }
}
