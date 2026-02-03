package it.samuconfaa.moderation.commands;

import it.samuconfaa.moderation.Moderation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ModerationCommand implements CommandExecutor {
    private Moderation plugin;
    public ModerationCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if(args.length == 0){
            if(sender instanceof Player p){

            }
            return true;
        }else if(args.length == 1){
            String a = args[0];
            if(a.equalsIgnoreCase("check")){
                //controllo parola
                return true;
            }

            if(sender.hasPermission("moderation.admin")){
                if(a.equalsIgnoreCase("reload")){
                    //reload config
                    return true;
                }else if (a.equalsIgnoreCase("add")){
                    //aggiungi parole
                    return true;
                }else if (a.equalsIgnoreCase("remove")){
                    //rimuovi parole
                    return true;
                }
            }else{
                //non hai i permessi
                return true;
            }
        }
        return false;
    }
}
