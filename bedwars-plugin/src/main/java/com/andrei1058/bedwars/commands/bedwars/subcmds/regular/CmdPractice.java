package com.andrei1058.bedwars.commands.bedwars.subcmds.regular;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.practice.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;

public final class CmdPractice extends SubCommand {
    public CmdPractice(ParentCommand parent, String name) { super(parent, name); showInList(true); setPriority(19); }
    @Override public boolean execute(String[] args, CommandSender sender) {
        PracticeManager manager = BedWars.getPracticeManager();
        if (args.length == 0) {
            if (sender instanceof Player) manager.openMenu((Player) sender);
            else PracticeManager.message(sender, Messages.PRACTICE_COMMAND_HELP);
            return true;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        try {
            switch (action) {
                case "join":
                    require(args, 2); manager.join(player(sender), PracticeMode.parse(args[1])); return true;
                case "retry": require(args, 1); manager.reset(player(sender)); return true;
                case "leave": require(args, 1); manager.leave(player(sender)); return true;
                case "list":
                    require(args, 1);
                    for (PracticeMode mode : PracticeMode.values()) sender.sendMessage(Language.getMsg(sender instanceof Player ? (Player) sender : null, Messages.PRACTICE_LIST_LINE)
                            .replace("{mode}", Language.getMsg(sender instanceof Player ? (Player) sender : null, mode.getMessagePath()))
                            .replace("{free}", String.valueOf(manager.getFreeCount(mode))).replace("{total}", String.valueOf(manager.getTotalCount(mode))));
                    return true;
                default: break;
            }
            if (!manager.hasPracticeAdmin(sender)) { PracticeManager.message(sender, Messages.PRACTICE_NO_PERMISSION); return true; }
            switch (action) {
                case "create": require(args, 3); manager.create(sender, args[1], PracticeMode.parse(args[2]), player(sender).getLocation()); break;
                case "setspawn": require(args, 2); manager.setSpawn(sender, args[1], player(sender).getLocation()); break;
                case "pos1": case "pos2": require(args, 2); manager.setCorner(sender, args[1], action.equals("pos1") ? 1 : 2, player(sender).getLocation()); break;
                case "faily": require(args, 3); manager.setFailY(sender, args[1], Integer.parseInt(args[2])); break;
                case "enable": case "disable": require(args, 2); manager.setEnabled(sender, args[1], action.equals("enable")); break;
                case "delete": require(args, 2); manager.delete(sender, args[1]); break;
                case "reload": require(args, 1); manager.reload(sender); break;
                case "npc":
                    require(args, 2);
                    if (args[1].equals("set")) manager.getNpc().set(player(sender));
                    else if (args[1].equals("remove")) manager.getNpc().remove(sender);
                    else throw new IllegalArgumentException("Invalid NPC action");
                    break;
                default: PracticeManager.message(sender, Messages.PRACTICE_COMMAND_HELP); PracticeManager.message(sender, Messages.PRACTICE_ADMIN_HELP);
            }
        } catch (PlayerOnly ex) { PracticeManager.message(sender, Messages.PRACTICE_PLAYER_ONLY); }
        catch (IllegalArgumentException ex) { PracticeManager.message(sender, Messages.PRACTICE_INVALID_ARGUMENTS, null, ex.getMessage()); }
        return true;
    }
    private static Player player(CommandSender sender) { if (!(sender instanceof Player)) throw new PlayerOnly(); return (Player) sender; }
    private static void require(String[] args, int length) { if (args.length != length) throw new IllegalArgumentException("Wrong argument count"); }
    private static final class PlayerOnly extends IllegalArgumentException { }
    @Override public List<String> getTabComplete() { return Arrays.asList("join", "retry", "leave", "list"); }
    public List<String> tabComplete(CommandSender sender, String[] args) {
        PracticeManager manager = BedWars.getPracticeManager();
        boolean admin = manager.hasPracticeAdmin(sender);
        List<String> values = new ArrayList<>();
        if (args.length == 2) {
            values.addAll(getTabComplete());
            if (admin) values.addAll(Arrays.asList("create", "setspawn", "pos1", "pos2", "faily", "enable", "disable", "delete", "reload", "npc"));
        } else if ((args.length == 3 && args[1].equalsIgnoreCase("join"))
                || (args.length == 4 && args[1].equalsIgnoreCase("create") && admin)) {
            for (PracticeMode mode : PracticeMode.values()) values.add(mode.getId());
        } else if (args.length == 3 && admin) {
            switch (args[1].toLowerCase(Locale.ROOT)) {
                case "npc": values.addAll(Arrays.asList("set", "remove")); break;
                case "setspawn": case "pos1": case "pos2": case "faily":
                case "enable": case "disable": case "delete":
                    for (PracticePoint point : manager.getPoints()) values.add(point.getId());
                    break;
                default: break;
            }
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        values.removeIf(value -> !value.toLowerCase(Locale.ROOT).startsWith(prefix));
        return values;
    }
}
