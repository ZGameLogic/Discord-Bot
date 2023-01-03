package bot.listeners;

import application.App;
import bot.utils.EmbedMessageGenerator;
import com.zgamelogic.AdvancedListenerAdapter;
import data.database.curseforge.CurseforgeRecord;
import data.database.curseforge.CurseforgeRepository;
import data.database.guildData.GuildData;
import data.database.guildData.GuildDataRepository;
import lombok.Getter;
import lombok.ToString;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

public class CurseForgeBot extends AdvancedListenerAdapter {

    private final CurseforgeRepository checks;
    private final GuildDataRepository guildData;
    private JDA bot;

    public CurseForgeBot(CurseforgeRepository checks, GuildDataRepository guildData) {
        this.checks = checks;
        this.guildData = guildData;
    }

    @Override
    public void onReady(ReadyEvent event) {
        bot = event.getJDA();
    }

    @ButtonResponse("enable_curse")
    private void enableBot(ButtonInteractionEvent event){
        event.deferEdit().queue();
        Guild guild = event.getGuild();
        GuildData gd = guildData.findById(guild.getIdLong()).get();
        gd.setCurseforgeCommandId(
                guild.upsertCommand(Commands.slash("curseforge", "Slash command for curseforge related things")
                        .addSubcommands(
                                new SubcommandData("listen", "Listens to a project")
                                        .addOption(OptionType.STRING, "project", "Project to watch", true)
                        )
                ).complete().getIdLong()
        );
        gd.setCurseforgeEnabled(true);
        guildData.save(gd);
        ActionRow row = event.getMessage().getActionRows().get(0);
        row.updateComponent("enable_curse", Button.success("disable_curse", "Curseforge bot"));
        event.getHook().editOriginalComponents(row).queue();
    }

    @ButtonResponse("disable_curse")
    private void disableBot(ButtonInteractionEvent event){
        event.deferEdit().queue();
        Guild guild = event.getGuild();
        GuildData gd = guildData.findById(guild.getIdLong()).get();
        gd.setCurseforgeEnabled(false);
        gd.setCurseforgeCommandId(0L);
        guildData.save(gd);
        ActionRow row = event.getMessage().getActionRows().get(0);
        row.updateComponent("disable_curse", Button.danger("enable_curse", "Curseforge bot"));
        event.getHook().editOriginalComponents(row).queue();
    }

    @SlashResponse(value = "curseforge", subCommandName = "listen")
    private void follow(SlashCommandInteractionEvent event){
        event.deferReply().queue();
        String project = event.getOption("project").getAsString();
        CurseforgeRecord cfr = new CurseforgeRecord();
        cfr.setChannelId(event.getChannel().getIdLong());
        cfr.setGuildId(event.getGuild().getIdLong());
        cfr.setLastChecked(new Date());
        cfr.setProjectId(project);
        CurseforgeProject response = new CurseforgeProject(project);
        if(!response.isValid()){
            event.getHook().sendMessage("No project with that ID found").queue();
            return;
        }
        cfr.setProjectVersionId(response.fileId);
        event.getHook().sendMessageEmbeds(EmbedMessageGenerator.curseforgeInitial(response)).queue();
        checks.save(cfr);
    }

    public void update(){
        for(CurseforgeRecord check: checks.findAll()){
            CurseforgeProject current = new CurseforgeProject(check.getProjectId());
            if(!check.getProjectVersionId().equals(current.fileId)){
                bot.getGuildById(check.getGuildId()).getTextChannelById(check.getChannelId()).sendMessageEmbeds(
                        EmbedMessageGenerator.curseforgeUpdate(current)
                ).queue();
                check.setProjectId(current.getFileId());
            }
            check.setLastChecked(new Date());
            checks.save(check);
        }
    }

    @Getter
    @ToString
    public class CurseforgeProject {
        private String name;
        private String summary;
        private String downloadCount;
        private String logoUrl;
        private String url;
        private String fileId;
        private String fileName;
        private boolean valid;

        public CurseforgeProject(String project){
            String url = "https://api.curseforge.com/v1/mods/" + project;
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet(url);
            httpget.setHeader("x-api-key", App.config.getCurseforgeApiToken());
            JSONObject json;
            try {
                HttpResponse httpresponse = httpclient.execute(httpget);
                if (httpresponse.getStatusLine().getStatusCode() != 200) return;
                BufferedReader in = new BufferedReader(new InputStreamReader(httpresponse.getEntity().getContent()));
                json = new JSONObject(in.readLine());
            } catch (IOException | JSONException e) {
                return;
            }

            try {
                name = json.getJSONObject("data").getString("name");
                summary = json.getJSONObject("data").getString("summary");
                downloadCount = json.getJSONObject("data").getString("downloadCount");
                logoUrl = json.getJSONObject("data").getJSONObject("logo").getString("url");
                url = json.getJSONObject("data").getJSONObject("links").getString("websiteUrl");
                fileId = json.getJSONObject("data").getString("mainFileId");
                JSONArray files = json.getJSONObject("data").getJSONArray("latestFiles");
                for(int i = 0; i < files.length(); i++){
                    JSONObject file = files.getJSONObject(i);
                    if(file.getLong("id") == Long.parseLong(fileId)){
                        fileName = file.getString("displayName");
                        break;
                    }
                }
            } catch (JSONException ignored) {
            }
            valid = true;
        }
    }
}