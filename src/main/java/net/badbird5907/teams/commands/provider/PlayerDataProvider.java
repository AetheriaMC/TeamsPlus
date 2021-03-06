package net.badbird5907.teams.commands.provider;

import net.badbird5907.blib.util.Logger;
import net.badbird5907.teams.commands.CommandManager;
import net.badbird5907.teams.commands.annotation.AllowOffline;
import net.badbird5907.teams.hooks.impl.VanishHook;
import net.badbird5907.teams.manager.PlayerManager;
import net.badbird5907.teams.object.PlayerData;
import net.octopvp.commander.command.CommandContext;
import net.octopvp.commander.command.CommandInfo;
import net.octopvp.commander.command.ParameterInfo;
import net.octopvp.commander.provider.Provider;
import net.octopvp.commander.sender.CoreCommandSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Deque;
import java.util.List;

public class PlayerDataProvider implements Provider<PlayerData> {
    @Override
    public PlayerData provide(CommandContext context, CommandInfo commandInfo, ParameterInfo parameterInfo, Deque<String> args) {
        try {
            if (parameterInfo.getCommander().getPlatform().isSenderParameter(parameterInfo)) {
                return PlayerManager.getData(context.getCommandSender().getIdentifier());
            } else {
                String s = args.pop();
                Player target = Bukkit.getPlayer(s);
                if (target == null) {
                    if (parameterInfo.getParameter().isAnnotationPresent(AllowOffline.class)) {
                        return PlayerManager.getDataLoadIfNeedTo(s);
                    }
                }
                if (VanishHook.isVanished(target)) {
                    return null;
                }
                return PlayerManager.getData(target);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public PlayerData provideDefault(CommandContext context, CommandInfo commandInfo, ParameterInfo parameterInfo, Deque<String> args) {
        Player target = Bukkit.getPlayer(args.pop());
        if (target == null) {
            if (parameterInfo.getParameter().isAnnotationPresent(AllowOffline.class)) {
                return PlayerManager.getDataLoadIfNeedTo(args.pop());
            }
        }
        return Provider.super.provideDefault(context, commandInfo, parameterInfo, args);
    }

    @Override
    public List<String> provideSuggestions(String input, String lastArg, CoreCommandSender sender) {
        return CommandManager.getCommander().getArgumentProviders().get(Player.class).provideSuggestions(input, lastArg, sender);
    }
}
