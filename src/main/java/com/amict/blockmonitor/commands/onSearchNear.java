package com.amict.blockmonitor.commands;

import com.amict.blockmonitor.utils.SearchHelper;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Created by johnfg10 on 08/06/2017.
 */
public class onSearchNear implements CommandExecutor{
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Optional<CommandSource> commandSourceOptional= src.getCommandSource();
        if (commandSourceOptional.isPresent()){
            CommandSource commandSource = commandSourceOptional.get();
            if (commandSource instanceof Player){
                Player player = (Player) commandSource;
                Location<World> locationWorld = player.getLocation();
                try {
                    SearchHelper.searchArea(locationWorld, 10);
                    PaginationList.Builder builder = PaginationList.builder();

                    List<Text> textList = SearchHelper.searchArea(locationWorld, 10);
                    for (Text text:textList) {
                        text.toBuilder().append(Text.of(""));
                    }

                    builder.contents(SearchHelper.searchArea(locationWorld, 10));
                    builder.title(Text.of("Search Near"));
                    builder.build().sendTo(player);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (commandSource instanceof ConsoleSource){
                commandSource.sendMessage(Text.builder().style(TextStyles.BOLD).color(TextColors.RED).append(Text.of("Only players are able to use this command")).build());
                return CommandResult.empty();
            } else if (commandSource instanceof CommandBlockSource){
                commandSource.sendMessage(Text.builder().style(TextStyles.BOLD).color(TextColors.RED).append(Text.of("Only players are able to use this command")).build());
                return CommandResult.empty();
            }
        }
        return CommandResult.success();
    }
}
