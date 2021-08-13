package lolcatloyal.IntroBot;

//import lolcatloyal.IntroBot.listeners.BasicListener;
import lolcatloyal.IntroBot.listeners.CopeListener;
//import lolcatloyal.IntroBot.listeners.StupidListener;
//import lolcatloyal.IntroBot.listeners.UWUListener;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;

public class IntroBot {

    public static JDABuilder builder;

    public static final String PREFIX = "-";

    public static final String ANNOUNCEMENT_CHANNELID = "875148960975691826";

    public static void main(String[] args) throws LoginException {

        builder = JDABuilder.createDefault(args[0]);

        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);

        builder.setBulkDeleteSplittingEnabled(false);

        builder.setCompression(Compression.NONE);

        builder.setActivity(Activity.playing("cope"));
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);

        registerListeners();

        builder.build();
    }

    public static void registerListeners() {
        //builder.addEventListeners(new BasicListener());
        builder.addEventListeners(new CopeListener());
        //builder.addEventListeners(new UWUListener());
        //builder.addEventListeners(new StupidListener());
    }

}
