package lolcatloyal.IntroBot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class StupidListener extends ListenerAdapter {

    private static final String TARGET_ID = "274915920982310912";

    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        Message message = event.getMessage();
        TextChannel channel = event.getChannel();

        if (!message.getAuthor().isBot() && message.getAuthor().getId().equals(TARGET_ID)){
            channel.sendMessage(message.getAuthor().getAsMention() + " You're Stupid").queue();
            channel.sendMessage("https://tenor.com/view/dumb-huh-stupid-idiot-gif-5042504").queue();
        }





    }
}
