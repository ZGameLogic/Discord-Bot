package bot.listeners;

import com.zgamelogic.AdvancedListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Random;

public class DadBot extends AdvancedListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(!event.isFromGuild()) return;
        if(event.getAuthor().getIdLong() == 102923614344482816L) { // Karisas stuff
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
