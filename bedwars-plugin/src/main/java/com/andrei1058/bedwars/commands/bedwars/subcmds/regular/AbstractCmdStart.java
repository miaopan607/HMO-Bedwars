package com.andrei1058.bedwars.commands.bedwars.subcmds.regular;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.SetupSession;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public abstract class AbstractCmdStart extends SubCommand {

    public AbstractCmdStart(ParentCommand parent, String name) {
        super(parent, name);
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        IArena a = Arena.getArenaByPlayer(p);
        if (a == null){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NOT_IN_GAME));
            return true;
        }
        if (!canStartFromArena(a, p)){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NOT_IN_GAME));
            return true;
        }
        if (!hasStartPermission(p)){
            p.sendMessage(getMsg(p, Messages.COMMAND_FORCESTART_NO_PERM));
            return true;
        }
        if (a.getStatus() == GameState.playing) return true;
        if (a.getStatus() == GameState.restarting) return true;
        return doStart(a, p, args);
    }

    protected boolean canStartFromArena(IArena arena, Player player) {
        return arena.isPlayer(player);
    }

    protected abstract boolean hasStartPermission(CommandSender s);

    protected abstract boolean doStart(IArena a, Player p, String[] args);

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, com.andrei1058.bedwars.api.BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;

        IArena a = Arena.getArenaByPlayer(p);
        if (a != null){
            GameState status = a.getStatus();
            if (status == GameState.waiting || status == GameState.starting){
                if (!canStartFromArena(a, p)) return false;
            } else {
                return false;
            }
        } else {
            return false;
        }

        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;

        return hasStartPermission(s);
    }
}
