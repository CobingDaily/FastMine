package fastmine.commandos;

import fastmine.FastMine;
import fastmine.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

public class FastMineCommand extends CommandBase {
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("fm");
    }

    @Override
    public String getCommandName() {
        return "fastmine";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/fastmine";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        FastMine.toggled = !FastMine.toggled;
        Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(Reference.MOD_NAME + " is now " + (FastMine.toggled ? (EnumChatFormatting.GREEN + "Enabled") : (EnumChatFormatting.RED + "Disabled"))));
    }
}
