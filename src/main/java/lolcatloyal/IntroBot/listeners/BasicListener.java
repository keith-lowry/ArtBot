package lolcatloyal.IntroBot.listeners;

import lolcatloyal.IntroBot.IntroBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

/**
 * Basic Listener that responds to commands sent
 * by text.
 */
public class BasicListener extends ListenerAdapter {

    //REMEMBER TO ADD .queue() at the end of commands!!!
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){
        Message message = event.getMessage();
        String messageRaw = message.getContentRaw();
        TextChannel channel = event.getChannel();
        User sender = message.getAuthor();
        TextChannel announcements = event.getGuild().getTextChannelById(IntroBot.ANNOUNCEMENT_CHANNELID);


        if (!sender.isBot()){
            if (messageRaw.contains(IntroBot.PREFIX)){ //command

                String command = messageRaw.substring(1);

                if (command.equalsIgnoreCase("hello")){ //hello command
                    channel.sendMessage("Hello!").queue();
                }
                else if (command.equalsIgnoreCase("help")){ //help command
                    channel.sendMessage("Commands:").queue();
                    channel.sendMessage("-help").queue();
                    channel.sendMessage("-announce").queue();
                    channel.sendMessage("-hello").queue();
                }
                else if (command.equalsIgnoreCase("announce")){ //announce command
                    channel.sendMessage("Sending announcement...").queue();

                    if (announcements != null){
                        announcements.sendMessage("Announcement Test").queue();
                    }
                    else{
                        channel.sendMessage("Announcements Channel not found...").queue();
                    }
                }
            }
        }
    }
}
