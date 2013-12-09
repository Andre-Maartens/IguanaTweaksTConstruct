package iguanaman.iguanatweakstconstruct.modifiers;

import iguanaman.iguanatweakstconstruct.IguanaConfig;
import iguanaman.iguanatweakstconstruct.IguanaLevelingLogic;
import iguanaman.iguanatweakstconstruct.IguanaTweaksTConstruct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import tconstruct.common.BowRecipe;
import tconstruct.common.TContent;
import tconstruct.items.tools.Hammer;
import tconstruct.items.tools.Pickaxe;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.crafting.PatternBuilder;
import tconstruct.library.crafting.ToolBuilder;
import tconstruct.library.crafting.ToolRecipe;
import tconstruct.library.tools.*;
import tconstruct.items.tools.*;

public class IguanaModUpgrade extends ToolMod {


    public IguanaModUpgrade()
    {
        super(new ItemStack[0], 0, "");
    }

    @Override
    public boolean matches (ItemStack[] input, ItemStack tool)
    {
        if (!canModify(tool, input)) return false;
        
        ToolRecipe toolRecipe = GetRecipe((ToolCore)tool.getItem());
        if (toolRecipe == null) return false;
        
        for (ItemStack inputStack : input)
        {
        	if (inputStack != null)
        	{
        		// Make sure all inputs are valid parts
	        	if (!(toolRecipe.validHead(inputStack.getItem()) 
	        			|| toolRecipe.validHandle(inputStack.getItem()) 
	        			|| toolRecipe.validAccessory(inputStack.getItem())
	        			|| toolRecipe.validExtra(inputStack.getItem()))
	        			) 
	        			return false;
	        	
	        	// Check for stone parts
	        	if (inputStack.getItemDamage() == 1)
	        	{
		        	if (!IguanaConfig.allowStoneTools) return false;

		        	int partIndex = IguanaTweaksTConstruct.toolParts.indexOf(inputStack.getItem());
		        	if (IguanaConfig.restrictedFlintParts.contains(partIndex+1)) return false;
	        	}
        	}
        }
        
        return true;
    }
    
    protected ToolRecipe GetRecipe(ToolCore tool)
    {
    	if (tool instanceof Shortbow)
    		return new BowRecipe(TContent.toolRod, TContent.bowstring, TContent.toolRod, TContent.shortbow);
    	else
    		return ToolBuilder.instance.recipeList.get(tool.getToolName());
    }

    @Override
    protected boolean canModify (ItemStack tool, ItemStack[] input)
    {
    	// basic checks
        if (tool == null || tool.getItem() == null || !(tool.getItem() instanceof ToolCore)) return false;
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");
        if (tags.getInteger("Damage") > 0) return false;
        

        // check which parts are changable
        String headAbility = TConstructRegistry.getMaterial(tags.getInteger("Head")).ability;
        boolean headChangable = headAbility.equals("Writable") || headAbility.equals("Thaumic") ? false : true;
        String handleAbility = TConstructRegistry.getMaterial(tags.getInteger("Handle")).ability;
        boolean handleChangable = handleAbility.equals("Writable") || headAbility.equals("Thaumic") ? false : true;
        
        boolean accessoryChangable = true;
        if (tags.hasKey("Accessory"))
        {
            String accessoryAbility = TConstructRegistry.getMaterial(tags.getInteger("Accessory")).ability;
            if (accessoryAbility.equals("Writable") || accessoryAbility.equals("Thaumic")) accessoryChangable = false;
        }

        boolean extraChangable = true;
        if (tags.hasKey("Extra"))
        {
            String extraAbility = TConstructRegistry.getMaterial(tags.getInteger("Extra")).ability;
            if (extraAbility.equals("Writable") || extraAbility.equals("Thaumic")) extraChangable = false;
        }

        ToolRecipe toolRecipe = GetRecipe((ToolCore)tool.getItem());
        if (toolRecipe == null) return false;
        
        // check if trying to replace irreplacable parts
        for (ItemStack inputStack : input)
        {
	        if (inputStack != null)
	        {
		    	if (
		    			(toolRecipe.validHead(inputStack.getItem()) && headChangable == false)
		    			|| (toolRecipe.validHandle(inputStack.getItem()) && handleChangable == false)
		    			|| (toolRecipe.validAccessory(inputStack.getItem()) && accessoryChangable == false)
		    			|| (toolRecipe.validExtra(inputStack.getItem()) && extraChangable == false)
		    			) return false;
	        }
        }
        
        return true;
    }

    @Override
    public void modify (ItemStack[] input, ItemStack tool)
    {
    	// setup variables
        ToolRecipe toolRecipe = GetRecipe((ToolCore)tool.getItem());
        ToolCore toolClass = (ToolCore)tool.getItem();
        NBTTagCompound tags = tool.getTagCompound().getCompoundTag("InfiTool");

        
        //get old tool part materials
        int oldHead = tags.getInteger("Head");
        int oldHandle = tags.getInteger("Handle");
        int oldAccessory = tags.getInteger("Accessory");
        int oldExtra = tags.getInteger("Extra");
        

        //get current item parts
        ItemStack headStack = new ItemStack(toolClass.getHeadItem(), 1, oldHead);
        ItemStack handleStack = new ItemStack(toolClass.getHandleItem(), 1, oldHandle);
    	ItemStack accessoryStack = null;
        if (toolClass.getAccessoryItem() != null) accessoryStack = new ItemStack(toolClass.getAccessoryItem(), 1, oldAccessory);
        ItemStack extraStack = null;
        if (toolClass.getExtraItem() != null) extraStack = new ItemStack(toolClass.getExtraItem(), 1, oldExtra);
        
        
        // check inputs for parts to replace
        List<String> replacing = new ArrayList<String>();
        for (ItemStack inputStack : input)
        {
	        if (inputStack != null)
	        {
		    	if (toolRecipe.validHead(inputStack.getItem()) && !replacing.contains("Head")) 
		    	{
		    		headStack = inputStack;
		    		replacing.add("Head");
		    	}
		    	else if (toolRecipe.validHandle(inputStack.getItem()) && !replacing.contains("Handle"))
		    	{
		    		handleStack = inputStack;
		    		replacing.add("Handle");
				}
		    	else if (toolRecipe.validAccessory(inputStack.getItem()) && !replacing.contains("Accessory"))
		    	{
		    		accessoryStack = inputStack;
		    		replacing.add("Accessory");
				}
		    	else if (toolRecipe.validExtra(inputStack.getItem()) && !replacing.contains("Extra"))
		    	{
		    		extraStack = inputStack;
		    		replacing.add("Extra");
				}
	        }
        }
        
		
        //build new tool and get its tags
    	ItemStack newTool = ToolBuilder.instance.buildTool(headStack, handleStack, accessoryStack, extraStack, "");
        NBTTagCompound newTags = newTool.getTagCompound().getCompoundTag("InfiTool");


        //Override basic tags
        int base = newTags.getInteger("BaseDurability");
        int bonus = tags.getInteger("BonusDurability");
        float modDur = tags.getFloat("ModDurability");

        int total = Math.max((int) ((base + bonus) * (modDur + 1f)), 1);
        
    	tags.setInteger("BaseDurability", base);
    	tags.setInteger("TotalDurability", total);
    	
		if (tags.hasKey("ToolEXP"))
		{
			int requiredXp = IguanaLevelingLogic.getRequiredXp(tool, tags);
			long currentXp = tags.getLong("ToolEXP");
			float xpPercentage = (float)currentXp / (float)requiredXp;
			int newRequiredXp = IguanaLevelingLogic.getRequiredXp(newTool, newTags);
			long newXp = Math.round(newRequiredXp * xpPercentage);
			tags.setLong("ToolEXP", newXp);
		}
		
		if (tags.hasKey("HeadEXP"))
		{
			int requiredXp = IguanaLevelingLogic.getRequiredXp(tool, tags, true);
			long currentXp = tags.getLong("HeadEXP");
			float xpPercentage = (float)currentXp / (float)requiredXp;
			int newRequiredXp = IguanaLevelingLogic.getRequiredXp(newTool, newTags, true);
			long newXp = Math.round(newRequiredXp * xpPercentage);
			tags.setLong("HeadEXP", newXp);
		}
    	
		
		boolean requiresBoost = false;
    	if (tags.hasKey("HarvestLevelModified"))
    	{
    		requiresBoost = true;
    		
    		tags.removeTag("HarvestLevelModified");

        	if (tags.hasKey("MobHead"))
        	{
        		tags.removeTag("MobHead");
        		
	            //Get and remove effects
	            List<Integer> badEffects = Arrays.asList(0, 20, 21, 22, 23, 24, 25);
	            List<Integer> effects = new ArrayList<Integer>();
	            for (int i = 1; i <= 6; ++i)
	            {
	            	if (tags.hasKey("Effect" + i))
	            	{
	            		int effectInt = tags.getInteger("Effect" + i);
	            		if (!badEffects.contains(effectInt)) effects.add(effectInt);
	            		tags.removeTag("Effect" + i);
	            	}
	            }
	            
	            //Re-write effects
	            for (int i = 0; i < effects.size(); ++i)
	            {
	            	tags.setInteger("Effect" + Integer.toString(i+1), effects.get(i));
	            }
        	}

    	}
    	
    	if (newTags.hasKey("Shoddy")) tags.setFloat("Shoddy", newTags.getFloat("Shoddy"));

        int reinforced = tags.getInteger("Unbreaking");
        int reinforcedNew = newTags.getInteger("Unbreaking");
        ToolMaterial oldHeadMat = TConstructRegistry.getMaterial(oldHead);
        ToolMaterial oldHandleMat = TConstructRegistry.getMaterial(oldHandle);
        ToolMaterial oldAccessoryMat = TConstructRegistry.getMaterial(oldAccessory);
        ToolMaterial oldExtraMat = TConstructRegistry.getMaterial(oldExtra);
        int reinforcedDifference = reinforcedNew - buildReinforced(oldHeadMat, oldHandleMat, oldAccessoryMat, oldExtraMat);
        tags.setInteger("Unbreaking", reinforced + reinforcedDifference);
    	if (newTags.hasKey("FlightSpeed")) tags.setFloat("FlightSpeed", newTags.getFloat("FlightSpeed"));
    	
        List<String> replaceTags = new ArrayList<String>(Arrays.asList(
        		"HarvestLevel", "HarvestLevel2", "HarvestLevelHandle", "HarvestLevelExtra", 
        		"MiningSpeed", "MiningSpeed2", "MiningSpeedHandle", "MiningSpeedExtra",
        		"DrawSpeed", "BaseDrawSpeed"
        		));

        for (String replaced : replacing)
        {
        	replaceTags.add(replaced);
        	replaceTags.add("Render" + replaced);
        }
        
        for (String replaceTag : replaceTags)
        {
        	if (newTags.hasKey(replaceTag)) tags.setInteger(replaceTag, newTags.getInteger(replaceTag));
        }

    	//Attack
    	if (tags.hasKey("ModAttack"))
    	{
            int attackAmount = tags.getIntArray("ModAttack")[0];
            int attackBonus = (int)((float)attackAmount / 30f);
        	tags.setInteger("Attack", newTags.getInteger("Attack") + attackBonus);
    	}
    	
    	//Mining speed
    	if (tags.hasKey("Redstone"))
    	{
            int speedAmount = tags.getIntArray("Redstone")[0];
            int speedBoost = speedAmount * 8;

            tags.setInteger("MiningSpeed", newTags.getInteger("MiningSpeed") + speedBoost);

            if (tags.hasKey("MiningSpeed2"))
                tags.setInteger("MiningSpeed2", newTags.getInteger("MiningSpeed2") + speedBoost);

            if (tags.hasKey("DrawSpeed"))
            {
                int baseDrawSpeed = newTags.getInteger("BaseDrawSpeed");
                int drawSpeed = (int) (baseDrawSpeed - (0.1f * baseDrawSpeed * (speedAmount / 50f)));
                tags.setInteger("DrawSpeed", drawSpeed);
            }
    	}
    	
    	//Remove unwanted modifiers
    	int modifiers = tags.getInteger("Modifiers");

        List<String> unwantedModifiers = Arrays.asList("Skeleton Skull", "Zombie Head", "Creeper Head", "Enderman Head", "Wither Skeleton Skull", "Nether Star");
    	for (String unwanted : unwantedModifiers)
    	{
        	if (tags.hasKey(unwanted)) tags.removeTag(unwanted);
    	}

    	//tooltip lists
    	List<String> tips = new ArrayList<String>();
    	List<String> modifierTips = new ArrayList<String>();

        //add mining level tooltip
        if (tool.getItem() instanceof Pickaxe || tool.getItem() instanceof Hammer)
        {
            String mLevel = IguanaTweaksTConstruct.getHarvestLevelName(tags.getInteger("HarvestLevel"));
            tips.add("Mining Level: " + mLevel);
            modifierTips.add("");
        }
    	
        int level = tags.getInteger("ToolLevel");
		int hLevel = tags.hasKey("HarvestLevel") ? hLevel = tags.getInteger("HarvestLevel") : -1;
        tips.add(IguanaLevelingLogic.getLevelTooltip(level));
        modifierTips.add("");

        if (IguanaConfig.showTooltipXP)
        {
        	if (level <= 5)
        	{
            	tips.add(IguanaLevelingLogic.getXpString(tool, false, false));
            	modifierTips.add("");
        	}
        	
        	if (IguanaConfig.levelingPickaxeBoost)
        	{
	        	if (hLevel >= TConstructRegistry.getMaterial("Copper").harvestLevel() && hLevel < TConstructRegistry.getMaterial("Manyullyn").harvestLevel() 
	        			&& !tags.hasKey("HarvestLevelModified") 
	        			&& (tool.getItem() instanceof Pickaxe || tool.getItem() instanceof Hammer))
	        	{
	            	tips.add(IguanaLevelingLogic.getXpString(tool, false, true));
	            	modifierTips.add("");
	        	}
	        	
	        	if (requiresBoost)
	        	{
                	tips.add("Requires boost");
                    modifierTips.add("");
	        	}
        	}
        }
    	
    	//get and remove tooltips
        int tipNum = 0;
        while (true)
        {
            String tip = "Tooltip" + ++tipNum;
            if (tags.hasKey(tip))
            {
            	String tipString = tags.getString(tip);
            	if (!tipString.contains("Mining Level Boost") 
            			&& !tipString.startsWith("Mining Level:")
            			&& !tipString.startsWith("Skill Level:")
            			&& !tipString.startsWith("XP:")
            			&& !tipString.startsWith("Head XP:")
            			&& !tipString.startsWith("Boost XP:")
        				&& !tipString.startsWith("Requires boost"))
            	{
                    tips.add(tipString);
                    modifierTips.add(tags.getString("ModifierTip" + tipNum));
            	}
            	tags.removeTag(tip);
            	tags.removeTag("ModifierTip" + tipNum);
            }
            else break;
        }
        
        //write tips
        for (int i = 1; i <= tips.size(); ++i)
        {
        	if (tips.get(i - 1) != null)
        	{
	        	tags.setString("Tooltip" + i, tips.get(i - 1));
	        	if (modifierTips.get(i - 1) != null)
	        		tags.setString("ModifierTip" + i, modifierTips.get(i - 1));
	        	else
	        		tags.setString("ModifierTip" + i, "");
        	}
        }

    	String materialName = TConstructRegistry.instance.getMaterial(oldHead).displayName;
    	String toolName = ((ToolCore)tool.getItem()).getToolName();
        if (tool.getDisplayName().endsWith(materialName + toolName))
        {
        	materialName = TConstructRegistry.instance.getMaterial(tags.getInteger("Head")).displayName;
        	tool.setItemName("\u00a7r" + materialName + toolName);
        }
    }

    @Override
    public void addMatchingEffect (ItemStack tool) {}

    int buildReinforced (ToolMaterial headMat, ToolMaterial handleMat, ToolMaterial accessoryMat, ToolMaterial extraMat)
    {
        int reinforced = 0;

        int dHead = headMat.reinforced();
        int dHandle = handleMat.reinforced();
        int dAccessory = 0;
        if (accessoryMat != null)
            dAccessory = accessoryMat.reinforced();
        int dExtra = 0;
        if (extraMat != null)
            dExtra = extraMat.reinforced();

        if (dHead > reinforced)
            reinforced = dHead;
        if (dHandle > reinforced)
            reinforced = dHandle;
        if (dAccessory > reinforced)
            reinforced = dAccessory;
        if (dExtra > reinforced)
            reinforced = dExtra;

        return reinforced;
    }

}
