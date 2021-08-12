package lolcatloyal.IntroBot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;

public class UWUListener extends ListenerAdapter {

    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event){

        Message message = event.getMessage();
        String messageRaw = message.getContentRaw();
        TextChannel channel = message.getTextChannel();

        if (messageRaw.contains("uwu")){
            java.lang.String authorMention = message.getAuthor().getAsMention();

            StringBuilder s = new StringBuilder();

            s.append(authorMention);
            s.append(" ");
            s.append("Shut up");

            channel.sendMessage(s.toString()).queue();
        }
    }
}
