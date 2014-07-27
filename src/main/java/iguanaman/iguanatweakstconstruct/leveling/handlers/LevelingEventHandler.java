package iguanaman.iguanatweakstconstruct.leveling.handlers;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import iguanaman.iguanatweakstconstruct.leveling.LevelingLogic;
import iguanaman.iguanatweakstconstruct.leveling.LevelingTooltips;
import iguanaman.iguanatweakstconstruct.reference.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import tconstruct.items.tools.Hammer;
import tconstruct.items.tools.Pickaxe;
import tconstruct.items.tools.Shortbow;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.event.ToolCraftEvent;
import tconstruct.library.tools.ToolCore;
import tconstruct.library.tools.Weapon;
import tconstruct.tools.TinkerTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LevelingEventHandler {
    @SubscribeEvent
    public void onHurt (LivingHurtEvent event)
    {
        if (event.source.damageType.equals("player") || event.source.damageType.equals("arrow"))
            if (event.source.getEntity() instanceof EntityPlayer)
            {
                EntityPlayer player = (EntityPlayer) event.source.getEntity();
                ItemStack stack = player.getCurrentEquippedItem();
                if (stack != null && stack.hasTagCompound())
                    if (stack.getItem() instanceof Weapon || stack.getItem() instanceof Shortbow && event.source.damageType.equals("arrow"))
                    {
                        long xp = Math.round(event.ammount);
                        if (event.entityLiving instanceof EntityAnimal) xp = Math.round(event.ammount / 4f);

                        if (xp > 0) LevelingLogic.addXP(stack, player, xp);
                    }
            }
    }

    @SubscribeEvent
    public void onUseHoe(UseHoeEvent event) {
        EntityPlayer player = event.entityPlayer;
        ItemStack stack = event.current;
        if (stack != null && stack.hasTagCompound() && stack.getItem() instanceof ToolCore)
            LevelingLogic.addXP(stack, player, 1L);
    }

    @SubscribeEvent
    public void onCraftTool (ToolCraftEvent.NormalTool event) {
        // arrows don't get levels
        if(event.tool == TinkerTools.arrow)
            return;

        NBTTagCompound toolTag = event.toolTag.getCompoundTag("InfiTool");
        int head = toolTag.getInteger("Head");
        int handle = toolTag.getInteger("Handle");
        int accessory = toolTag.getInteger("Accessory");
        int extra = toolTag.hasKey("Extra") ? toolTag.getInteger("Extra") : -1;

        // add tags for tool leveling
        LevelingLogic.addLevelingTags(toolTag);

        // add the initial tooltips for leveling info!
        //IguanaLevelingLogic.addLevelingToolTips(event.tool, toolTag);
/*




        // CREATE TOOLTIP LISTS
        List<String> tips = new ArrayList<String>();
        List<String> modifierTips = new ArrayList<String>();

        // MINING LEVEL TOOLTIP
        if (event.tool instanceof Pickaxe || event.tool instanceof Hammer)
        {
            String mLevel = IguanaTweaksTConstruct.getHarvestLevelName(toolTag.getInteger("HarvestLevel"));
            tips.add("Mining Level: " + mLevel);
            //modifierTips.add("");
        }

        // TOOL LEVELING DATA + TOOLTIP
        if (IguanaConfig.toolLeveling)
        {
            tips.add(IguanaLevelingLogic.getLevelTooltip(1));
            //modifierTips.add("");

            toolTag.setInteger("ToolLevel", 1);

            toolTag.setLong("ToolEXP", 0);
            if (IguanaConfig.showTooltipXP)
            {
                tips.add(IguanaLevelingLogic.getXpString(new ItemStack(event.tool), toolTag));
                //modifierTips.add("");
            }

            if (IguanaConfig.levelingPickaxeBoost && (event.tool instanceof Pickaxe || event.tool instanceof Hammer))
            {
                toolTag.setLong("HeadEXP", 0);

                int hLevel = toolTag.hasKey("HarvestLevel") ? hLevel = toolTag.getInteger("HarvestLevel") : -1;
                if (hLevel >= TConstructRegistry.getMaterial("Copper").harvestLevel() && hLevel < TConstructRegistry.getMaterial("Manyullyn").harvestLevel())
                {
                    if (IguanaConfig.showTooltipXP)
                    {
                        tips.add(IguanaLevelingLogic.getXpToolTip(new ItemStack(event.tool), toolTag, true));
                        //modifierTips.add("");
                    }

                    tips.add("\u00A76Requires boost");
                    //modifierTips.add("");
                }
            }
        }

        // STORE + REMOVE EXISTING TOOLTIPS
        int tipNum = 0;
        while (true)
        {
            String tip = "Tooltip" + ++tipNum;
            if (toolTag.hasKey(tip))
            {
                tips.add(toolTag.getString(tip));
                //modifierTips.add(toolTag.getString("ModifierTip" + tipNum));
                toolTag.removeTag(tip);
                //toolTag.removeTag("ModifierTip" + tipNum);
            }
            else break;
        }

        // WRITE TOOLTIPS
        for (int i = 1; i <= tips.size(); ++i)
            if (tips.get(i - 1) != null)
            {
                toolTag.setString("Tooltip" + i, tips.get(i - 1));
                //if (modifierTips.get(i - 1) != null)
                  //  toolTag.setString("ModifierTip" + i, modifierTips.get(i - 1));
                //else
                  //  toolTag.setString("ModifierTip" + i, "");
            }
*/


        if (Config.toolLeveling && Config.toolLevelingExtraModifiers)
            toolTag.setInteger("Modifiers", Math.max(toolTag.getInteger("Modifiers") - 3, 0));

        if (event.tool == TinkerTools.hammer || event.tool == TinkerTools.excavator || event.tool == TinkerTools.lumberaxe)
        {
            List<String> replaceTags = new ArrayList<String>(Arrays.asList(
                    "MiningSpeed", "MiningSpeed2", "MiningSpeedHandle", "MiningSpeedExtra"
            ));

            for (String replaceTag : replaceTags)
                if (toolTag.hasKey(replaceTag)) toolTag.setInteger(replaceTag, Math.round(toolTag.getInteger(replaceTag) / 2f));
        }
    }



    // Display XP of held tool in debug (F3) if config is set
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        if (Config.toolLeveling && Config.showDebugXP)
        {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.thePlayer;
            if (!player.isDead && mc.gameSettings.showDebugInfo) {
                ItemStack equipped = player.getCurrentEquippedItem();

                if (equipped != null && equipped.getItem() != null && equipped.getItem() instanceof ToolCore)
                {
                    NBTTagCompound tags = equipped.getTagCompound().getCompoundTag("InfiTool");

                    int level = tags.getInteger("ToolLevel");
                    int hLevel = tags.hasKey("HarvestLevel") ? hLevel = tags.getInteger("HarvestLevel") : -1;

                    event.left.add("");

                    if (Config.showTooltipXP)
                    {
                        if (level <= 5)
                            event.left.add(LevelingTooltips.getXpToolTip(equipped, null));

                        if (Config.levelingPickaxeBoost)
                            if (hLevel >= TConstructRegistry.getMaterial("Copper").harvestLevel() && hLevel < TConstructRegistry.getMaterial("Manyullyn").harvestLevel()
                                    && !tags.hasKey("HarvestLevelModified")
                                    && (equipped.getItem() instanceof Pickaxe || equipped.getItem() instanceof Hammer))
                                event.left.add(LevelingTooltips.getBoostXpToolTip(equipped, null));
                    }
                }
            }
        }
    }
}
