/*
 * This file is part of Wireless Fluid Terminal. Copyright (c) 2017, p455w0rd
 * (aka TheRealp455w0rd), All rights reserved unless otherwise stated.
 *
 * Wireless Fluid Terminal is free software: you can redistribute it and/or
 * modify it under the terms of the MIT License.
 *
 * Wireless Fluid Terminal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the MIT License for
 * more details.
 *
 * You should have received a copy of the MIT License along with Wireless
 * Fluid Terminal. If not, see <https://opensource.org/licenses/MIT>.
 */
package p455w0rd.wft.items;

import java.util.List;

import appeng.api.config.*;
import appeng.api.util.IConfigManager;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import p455w0rd.ae2wtlib.api.WTApi;
import p455w0rd.ae2wtlib.api.client.*;
import p455w0rd.ae2wtlib.items.ItemWT;
import p455w0rd.wft.api.IWirelessFluidTerminalItem;
import p455w0rd.wft.api.WFTApi;
import p455w0rd.wft.init.ModGlobals;
import p455w0rd.wft.util.WFTUtils;

/**
 * @author p455w0rd
 *
 */
public class ItemWFT extends ItemWT implements IModelHolder, IWirelessFluidTerminalItem, IBaubleItem {

	private EntityPlayer entityPlayer;

	public ItemWFT() {
		this(new ResourceLocation(ModGlobals.MODID, "wft"));
	}

	public ItemWFT(ResourceLocation registryName) {
		super(registryName);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack item = player.getHeldItem(hand);
		if (world.isRemote && hand == EnumHand.MAIN_HAND && !item.isEmpty() && getAECurrentPower(item) > 0) {
			WFTApi.instance().openWFTGui(player, false, player.inventory.currentItem);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
		}
		else if (!world.isRemote) {
			if (getAECurrentPower(item) <= 0) {
				player.sendMessage(PlayerMessages.DeviceNotPowered.get());
				return new ActionResult<ItemStack>(EnumActionResult.FAIL, item);
			}
			if (!WFTApi.instance().isTerminalLinked(item)) {
				player.sendMessage(PlayerMessages.DeviceNotLinked.get());
				return new ActionResult<ItemStack>(EnumActionResult.FAIL, item);
			}
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, item);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addCheckedInformation(final ItemStack is, final World world, final List<String> list, final ITooltipFlag advancedTooltips) {
		if (getPlayer() == null || WTApi.instance().getGUIObject(is, getPlayer()) == null) {
			return;
		}
		String encKey = getEncryptionKey(is);
		//String shift = I18n.format("tooltip.press_shift.desc").replace("Shift", TextFormatting.YELLOW + "" + TextFormatting.BOLD + "" + TextFormatting.ITALIC + "Shift" + TextFormatting.GRAY);
		String pctTxtColor = TextFormatting.WHITE + "";
		double aeCurrPower = getAECurrentPower(is);
		double aeCurrPowerPct = (int) Math.floor(aeCurrPower / getAEMaxPower(is) * 1e4) / 1e2;
		if ((int) aeCurrPowerPct >= 75) {
			pctTxtColor = TextFormatting.GREEN + "";
		}
		if ((int) aeCurrPowerPct <= 5) {
			pctTxtColor = TextFormatting.RED + "";
		}
		list.add(TextFormatting.AQUA + "==============================");
		if (WTApi.instance().isWTCreative(is)) {
			list.add(GuiText.StoredEnergy.getLocal() + ": " + TextFormatting.GREEN + "" + I18n.format(WTApi.instance().getConstants().getTooltips().infinite()));
		}
		else {
			list.add(GuiText.StoredEnergy.getLocal() + ": " + pctTxtColor + (int) aeCurrPower + " AE - " + aeCurrPowerPct + "%");
		}
		String linked = TextFormatting.RED + GuiText.Unlinked.getLocal();
		if (encKey != null && !encKey.isEmpty()) {
			linked = TextFormatting.BLUE + GuiText.Linked.getLocal();
		}
		list.add("Link Status: " + linked);

		if (WTApi.instance().getConfig().isInfinityBoosterCardEnabled()) {
			if (WTApi.instance().getConfig().isOldInfinityMechanicEnabled()) {
				list.add(I18n.format(WTApi.instance().getConstants().boosterCard()) + ": " + (checkForBooster(is) ? TextFormatting.GREEN + "" : TextFormatting.RED + "" + I18n.format(WTApi.instance().getConstants().getTooltips().not())) + " " + I18n.format(WTApi.instance().getConstants().getTooltips().installed()));
			}
			else {
				int infinityEnergyAmount = WTApi.instance().getInfinityEnergy(is);
				String amountColor = infinityEnergyAmount < WTApi.instance().getConfig().getLowInfinityEnergyWarningAmount() ? TextFormatting.RED.toString() : TextFormatting.GREEN.toString();
				String reasonString = "";
				if (infinityEnergyAmount <= 0) {
					reasonString = "(" + I18n.format(WTApi.instance().getConstants().getTooltips().outOf()) + " " + I18n.format(WTApi.instance().getConstants().getTooltips().infinityEnergy()) + ")";
				}
				boolean outsideOfWAPRange = !WTApi.instance().isInRange(is);
				if (!outsideOfWAPRange) {
					reasonString = I18n.format(WTApi.instance().getConstants().getTooltips().inWapRange());
				}
				String activeString = infinityEnergyAmount > 0 && outsideOfWAPRange ? TextFormatting.GREEN + "" + I18n.format(WTApi.instance().getConstants().getTooltips().active()) : TextFormatting.GRAY + "" + I18n.format(WTApi.instance().getConstants().getTooltips().inactive()) + " " + reasonString;
				list.add(I18n.format(WTApi.instance().getConstants().getTooltips().infiniteRange()) + ": " + activeString);
				String infinityEnergyString = WFTUtils.isWFTCreative(is) ? I18n.format(WTApi.instance().getConstants().getTooltips().infinite()) : (isShiftKeyDown() ? "" + infinityEnergyAmount + "" + TextFormatting.GRAY + " " + I18n.format(WTApi.instance().getConstants().getTooltips().units()) : ReadableNumberConverter.INSTANCE.toSlimReadableForm(infinityEnergyAmount));
				list.add(I18n.format(WTApi.instance().getConstants().getTooltips().infinityEnergy()) + ": " + amountColor + "" + infinityEnergyString);
			}
		}
	}

	@Override
	public IConfigManager getConfigManager(final ItemStack target) {
		final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
			final NBTTagCompound data = Platform.openNbtData(target);
			manager.writeToNBT(data);
		});
		out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
		out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
		out.readFromNBT(Platform.openNbtData(target).copy());
		return out;
	}

}