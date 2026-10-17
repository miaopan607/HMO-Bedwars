package com.andrei1058.bedwars.commands.bedwars.subcmds.regular;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.spectator.SpectatorVisibilityService;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public final class CmdSpectators extends SubCommand {
    public CmdSpectators(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(20);
        showInList(true);
        setDisplayInfo(MainCommand.createTC(getMsg(null, Messages.COMMAND_SPECTATORS_DISPLAY)
                .replace("{command}", parent.getName()).replace("{subcommand}", name),
                "/" + parent.getName() + " " + name, ""));
    }

    @Override
    public boolean execute(String[] args, CommandSender sender) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isSpectator(player)) {
            player.sendMessage(getMsg(player, Messages.COMMAND_SPECTATORS_ONLY));
            return true;
        }
        if (args.length != 0) return false;
        boolean visible = SpectatorVisibilityService.getInstance().toggle(player);
        ((Arena) arena).updateSpectatorVisibilityItem(player);
        player.sendMessage(getMsg(player, visible ? Messages.SPECTATOR_VISIBILITY_SHOWN : Messages.SPECTATOR_VISIBILITY_HIDDEN));
        return true;
    }

    @Override
    public List<String> getTabComplete() { return Collections.emptyList(); }

    @Override
    public boolean canSee(CommandSender sender, com.andrei1058.bedwars.api.BedWars api) {
        if (!(sender instanceof Player)) return false;
        IArena arena = Arena.getArenaByPlayer((Player) sender);
        return arena != null && arena.isSpectator((Player) sender);
    }
}
