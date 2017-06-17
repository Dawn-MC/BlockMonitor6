package com.amict.blockmonitor.commands;

import com.amict.blockmonitor.utils.RestoreHelper;
import org.h2.tools.Restore;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Optional;

/**
 * Created by johnfg10 on 13/06/2017.
 */
public class onRestoreNear implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<CommandSource> commandSourceOptional = src.getCommandSource();
        if (commandSourceOptional.isPresent()){
            CommandSource commandSource = commandSourceOptional.get();
            if (commandSource instanceof Player){
                Player player = (Player) commandSource;
                System.out.println("running restore!");
                Text text = RestoreHelper.restoreArea(player.getLocation(), 10);
                player.sendMessage(text);
            }else {
                commandSource.sendMessage(Text.builder().style(TextStyles.BOLD).color(TextColors.RED).append(Text.of("Only players are able to use this command")).build());
                return CommandResult.empty();
            }
        }
        return CommandResult.success();
    }
}
