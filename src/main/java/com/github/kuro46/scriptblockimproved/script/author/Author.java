package com.github.kuro46.scriptblockimproved.script.author;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * A Author represents author of script.
 */
public final class Author {

    @NonNull
    @Getter
    private final AuthorData data;

    private Author(@NonNull final AuthorData data) {
        this.data = data;
    }

    public static Author player(final String name, final UUID uniqueId) {
        return new Author(new PlayerAuthorData(name, uniqueId));
    }

    public static Author player(final String name) {
        return new Author(new PlayerAuthorData(name));
    }

    public static Author system() {
        return new Author(SystemAuthorData.getInstance());
    }

    public static Author console() {
        return new Author(ConsoleAuthorData.getInstance());
    }

    public static Author fromCommandSender(final CommandSender sender) {
        if (sender instanceof Player) {
            final Player player = (Player) sender;
            return Author.player(player.getName(), player.getUniqueId());
        } else if (sender instanceof ConsoleCommandSender) {
            return Author.console();
        } else {
            throw new IllegalArgumentException(
                    String.format("Unknown CommandSender: %s", sender.getClass()));
        }
    }

    public static Author fromJson(@NonNull final JsonObject json) {
        final AuthorData data;
        final String type = json.get("type").getAsString();
        switch (type) {
            case "system":
                data = SystemAuthorData.getInstance();
                break;
            case "console":
                data = ConsoleAuthorData.getInstance();
                break;
            case "player":
                final JsonObject jsonData = json.getAsJsonObject("data");
                final String name = jsonData.get("name").getAsString();
                final UUID uniqueId;
                final JsonElement jsonUniqueId = jsonData.get("uniqueId");
                if (jsonUniqueId.isJsonNull()) {
                    uniqueId = null;
                } else {
                    uniqueId = UUID.fromString(jsonUniqueId.getAsString());
                }
                data = new PlayerAuthorData(name, uniqueId);
                break;
            default:
                throw new IllegalArgumentException("Unknown author type: " + type);
        }

        return new Author(data);
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        final String type;
        if (data instanceof SystemAuthorData) {
            type = "system";
        } else if (data instanceof ConsoleAuthorData) {
            type = "console";
        } else if (data instanceof PlayerAuthorData) {
            type = "player";
            final JsonObject data = new JsonObject();
            final PlayerAuthorData playerData = (PlayerAuthorData) this.data;
            data.addProperty("name", playerData.getName());
            data.addProperty("uniqueId", playerData.getUniqueId().map(UUID::toString).orElse(null));
            json.add("data", data);
        } else {
            throw new IllegalArgumentException("Unknown data class: " + data.getClass());
        }

        json.addProperty("type", type);
        return json;
    }

    public String getName() {
        return data.getName();
    }

    public boolean isSystem() {
        return data instanceof SystemAuthorData;
    }

    public boolean isPlayer() {
        return data instanceof PlayerAuthorData;
    }

    public boolean isConsole() {
        return data instanceof ConsoleAuthorData;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
