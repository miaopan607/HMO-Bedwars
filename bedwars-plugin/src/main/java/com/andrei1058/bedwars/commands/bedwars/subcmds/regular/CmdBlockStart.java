package com.andrei1058.bedwars.commands.bedwars.subcmds.regular;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import com.andrei1058.bedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class CmdBlockStart extends SubCommand {

    public CmdBlockStart(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(16);
        showInList(true);
        setDisplayInfo(createHelp(null));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (!(s instanceof Player)) return false;
        Player p = (Player) s;

        if (args.length != 0) {
            p.spigot().sendMessage(createHelp(p));
            return true;
        }

        IArena arena = Arena.getArenaByPlayer(p);
        if (arena == null || (!arena.isPlayer(p) && !arena.isSpectator(p))) {
            p.sendMessage(getMsg(p, Messages.COMMAND_BLOCK_START_NOT_IN_ARENA));
            return true;
        }
        if (!Permissions.hasPermission(p, Permissions.PERMISSION_ALL, Permissions.PERMISSION_BLOCK_START)) {
            p.sendMessage(getMsg(p, Messages.COMMAND_BLOCK_START_NO_PERMISSION));
            return true;
        }
        if (arena.getStatus() != GameState.waiting && arena.getStatus() != GameState.starting) {
            p.sendMessage(getMsg(p, Messages.COMMAND_BLOCK_START_INVALID_STATE));
            return true;
        }

        boolean blocked = !arena.isStartBlocked();
        if (blocked) {
            ((Arena) arena).setStartBlockedBy(p.getUniqueId());
        } else {
            arena.setStartBlocked(false);
        }
        String message = blocked ? Messages.COMMAND_BLOCK_START_ENABLED : Messages.COMMAND_BLOCK_START_DISABLED;
        for (Player player : arena.getPlayers()) {
            player.sendMessage(getMsg(player, message));
        }
        for (Player spectator : arena.getSpectators()) {
            spectator.sendMessage(getMsg(spectator, message));
        }
        return true;
    }

    private TextComponent createHelp(Player player) {
        String text = getMsg(player, Messages.COMMAND_BLOCK_START_DISPLAY)
                .replace("{command}", getParent().getName())
                .replace("{subcommand}", getSubCommandName());
        String hover = getMsg(player, Messages.COMMAND_BLOCK_START_DISPLAY_HOVER)
                .replace("{permission}", Permissions.PERMISSION_BLOCK_START);
        return MainCommand.createTC(text, "/" + getParent().getName() + " " + getSubCommandName(), hover);
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, BedWars api) {
        if (!(s instanceof Player)) return false;
        Player p = (Player) s;
        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;

        IArena arena = Arena.getArenaByPlayer(p);
        if (arena == null || (!arena.isPlayer(p) && !arena.isSpectator(p))) return false;
        if (arena.getStatus() != GameState.waiting && arena.getStatus() != GameState.starting) return false;
        return Permissions.hasPermission(p, Permissions.PERMISSION_ALL, Permissions.PERMISSION_BLOCK_START);
    }
}
