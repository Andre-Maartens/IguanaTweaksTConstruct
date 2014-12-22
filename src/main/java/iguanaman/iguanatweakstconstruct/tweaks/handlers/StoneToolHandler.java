package iguanaman.iguanatweakstconstruct.tweaks.handlers;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import tconstruct.items.tools.Arrow;
import tconstruct.items.tools.Shortbow;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.event.ToolCraftEvent;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.IToolPart;
import tconstruct.tools.TinkerTools;

public class StoneToolHandler {
    // we can initialize this statically, because it wont be initialized until PostInit, where all materials are already registered
    private static ToolMaterial stoneMaterial = TConstructRegistry.getMaterial("Stone");

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onTooltip(ItemTooltipEvent event)
    {
        if(event.entityPlayer == null)
            return;

        // we're only interested if it's a tool part
        if(!(event.itemStack.getItem() instanceof IToolPart) || event.itemStack.getItem() == TinkerTools.bowstring || event.itemStack.getItem() == TinkerTools.fletching)
            return;

        ItemStack stack = event.itemStack;
        IToolPart part = (IToolPart)stack.getItem();

        // stone parts disabled?
        if(TConstructRegistry.getMaterial(part.getMaterialID(stack)) == stoneMaterial)
        {
            event.toolTip.add(1, "");
            event.toolTip.add(2, EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.part.castonly1"));
            event.toolTip.add(3, EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("tooltip.part.castonly2"));
            // we abuse the fact that the result is not used by anything to signal our other handlers to not add another tooltip
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onToolCraft(ToolCraftEvent event)
    {
        for(int i = 0; i < event.materials.length; i++)
        {
            // ignore bowstring and fletchings
            // todo: add crossbow and longbow
            if(event.tool instanceof Shortbow && i == 1)
                continue;
            if(event.tool instanceof Arrow && i == 2)
                continue;

            // don't allow stone tools
            if(event.materials[i] == stoneMaterial)
                event.setResult(Event.Result.DENY);
        }
    }
}
