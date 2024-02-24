package bot.listeners;

import com.zgamelogic.annotations.DiscordController;
import com.zgamelogic.annotations.DiscordMapping;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@DiscordController
public class DadBot {

    @DiscordMapping
    public void messageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getAuthor().getIdLong() != 102923614344482816L) return;
        String message = event.getMessage().getContentRaw().toLowerCase().replaceAll("'", "").replaceAll("’", "");
        if(message.startsWith("im ") || message.contains(" im ")){
            String[] messageArray = message.split(" ");
            String dad = "";
            boolean adding = false;
            for(int i = 0; i < messageArray.length; i++){
                if(adding){
                    String word = messageArray[i];
                    dad += word.replace(".", "") + " ";
                    if(word.contains(".")){
                        break;
                    }
                }
                if(messageArray[i].equals("im")){
                    adding = true;
                }
            }
            dad = dad.trim();
            if(!dad.equals("")) event.getMessage().reply("Hi " + dad + ", I'm Dad").queue();
        }
        if(event.getAuthor().getIdLong() == 195174230281814016L){
            if(event.getMessage().getContentRaw().contains("...")){
                try {
                    int meme = new Random().nextInt(1, 16);
                    event.getChannel().sendFiles(FileUpload.fromData(new ClassPathResource("assets/Amrit/amritmeme0" + meme + ".jpg").getFile())).queue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
