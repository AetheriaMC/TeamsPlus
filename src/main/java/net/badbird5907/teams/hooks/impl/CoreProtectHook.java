package net.badbird5907.teams.hooks.impl;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import net.badbird5907.blib.util.Logger;
import net.badbird5907.teams.TeamsPlus;
import net.badbird5907.teams.hooks.Hook;
import net.badbird5907.teams.manager.PlayerManager;
import net.badbird5907.teams.manager.TeamsManager;
import net.badbird5907.teams.object.ChatChannel;
import net.badbird5907.teams.object.PlayerData;
import net.badbird5907.teams.object.Team;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.config.Config;
import net.coreprotect.listener.player.PlayerChatListener;
import net.coreprotect.paper.listener.PaperChatListener;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class CoreProtectHook extends Hook {
    private static YamlConfiguration coreProtectConfig;

    @Getter
    private boolean enabled;

    public CoreProtectHook() {
        super("CoreProtect");
    }

    @Override
    public void init(TeamsPlus plugin) {
        reload();
        if (!coreProtectConfig.getBoolean("enable-hook")) return;

        if (Config.getGlobal().PLAYER_MESSAGES && coreProtectConfig.getBoolean("modify-chat-logs")) {
            // Unregister the CoreProtect chat listener, so we can use our own chat listener.
            for (RegisteredListener registeredListener : HandlerList.getRegisteredListeners(CoreProtect.getInstance())) {
                if (registeredListener.getListener().getClass().equals(PaperChatListener.class) || registeredListener.getListener().getClass().equals(PlayerChatListener.class)) {
                    HandlerList.unregisterAll(registeredListener.getListener());
                    enabled = true;
                }
            }
        }
    }

    public void logChat(Player player, String message, ChatChannel chatChannel) {
        Logger.debug("Logging chat message to CoreProtect, enabled: %1", enabled);
        if (!enabled) return;
        String prefix = "";
        if (chatChannel == ChatChannel.TEAM) {
            PlayerData data = PlayerManager.getData(player.getUniqueId());
            prefix = "[Team] [" + data.getPlayerTeam().getName() + "] | ";
        } else if (chatChannel == ChatChannel.GLOBAL) {
            prefix = "[Global] | ";
        } else if (chatChannel == ChatChannel.ALLY) {
            PlayerData data = PlayerManager.getData(player.getUniqueId());
            Team allyTeam = TeamsManager.getInstance().getTeamById(data.getAllyChatTeamId());
            if (allyTeam == null) prefix = "[Ally] [Unknown] | ";
            else prefix = "[Ally] [" + allyTeam.getName() + "] | ";
        }
        String f = prefix + message;
        Logger.debug("Logging final message: %1", f);
        CoreProtect.getInstance().getAPI().logChat(player, f);
    }

    @SneakyThrows
    @Override
    public void reload() {
        super.reload();
        File file = new File(TeamsPlus.getInstance().getDataFolder() + "/hooks/CoreProtect.yml");
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        if (!file.exists()) {
            Files.copy(TeamsPlus.getInstance().getResource("CoreProtect.yml"), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        coreProtectConfig = new YamlConfiguration();
        coreProtectConfig.load(file);
    }

    @Override
    public void disable(TeamsPlus plugin) {

    }
}
