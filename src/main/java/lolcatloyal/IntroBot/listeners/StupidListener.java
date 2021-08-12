package lolcatloyal.IntroBot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class StupidListener extends ListenerAdapter {

    private final String[] TARGET_IDS = {};

    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        Message message = event.getMessage();
        TextChannel channel = event.getChannel();
        User author = message.getAuthor();

        if (!author.isBot() && authorIsTarget(author)){
            channel.sendMessage(message.getAuthor().getAsMention() + " You're Stupid").queue();
            channel.sendMessage("https://tenor.com/view/dumb-huh-stupid-idiot-gif-5042504").queue();
        }
    }

    private boolean authorIsTarget(User author){
        String authorID = author.getId();

        for(String targetID:
             TARGET_IDS) {

            if(targetID.equals(authorID)){
                return true;
            }
        }

        return false;
    }
}
