import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.http.HttpEntity;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class discord_bot extends ListenerAdapter {
    public static void main(String[] args) throws LoginException, IOException, URISyntaxException, InterruptedException {
        // Connect bot
        JDA jda = JDABuilder.createDefault(System.getenv("DISCORD_BOT_TOKEN"))
                .addEventListeners(new discord_bot())
                .build()
                .awaitReady();

        // Get Guild information
        Guild guild = jda.getGuildById("1007874152952254544");
        if(guild == null) System.exit(1);
        TextChannel general = guild.getTextChannelById("1007874153472340071");
        if(general == null) System.exit(1);
        general.sendMessage("WE ONLINE!!!").queue();

        // Connect to Twitter API
        Map<String, String> rules = new HashMap<>();    // contains rules for filtered stream
        rules.put("context:71.* lang:en -is:retweet", "");
        TwitterStream twitterAPI = new TwitterStream(rules);
        HttpEntity entity = twitterAPI.connectStream(System.getenv("TWITTER_BEARER_TOKEN"));


        // Print every line from Twitter Stream
        if (null != entity) {
            BufferedReader reader = new BufferedReader(new InputStreamReader((entity.getContent())));
            String line = reader.readLine();
            Gson g = new Gson();
            JsonObject jsonObject = new JsonObject();
            while (line != null) {
                try {
                    String link = streamDataToLink(g, jsonObject, line);
                    general.sendMessage(link).queue();
                } catch(Exception ignored) {}
                line = reader.readLine();
                Thread.sleep(3000);
            }
        }
    }

    public static String streamDataToLink(Gson g, JsonObject jsonObject, String line) {
        try {
            jsonObject = g.fromJson(line, JsonElement.class).getAsJsonObject();
            JsonObject data = (JsonObject) jsonObject.get("data");
            String id = data.get("id").toString().substring(1,20);
            return "https://twitter.com/anyuser/status/" + id;
        } catch(Exception e) {
            return "Error";
        }
    }

    public static MongoDatabase connectToMongoDB(String connectionStr) {
        ConnectionString connectionString = new ConnectionString(connectionStr);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        System.out.println("Connected to MongoDB!");
        return mongoClient.getDatabase("test");
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(!event.getAuthor().isBot()) {
            String s = event.getMessage().getContentRaw();
            event.getChannel().sendMessage("hello").queue();
        }
    }
}
