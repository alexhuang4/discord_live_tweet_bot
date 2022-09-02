import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class test {
    public static void main(String[] args) {

        String s = "{\"data\":{\"id\":123}}";

        Gson g = new Gson();
        JsonElement jsonElement = g.fromJson(s, JsonElement.class);
        JsonObject jsonObject = g.fromJson(s, JsonElement.class).getAsJsonObject();
        JsonObject jsonObject1 = (JsonObject)jsonObject.get("data");
        System.out.println(jsonObject1.get("id"));
    }
}
