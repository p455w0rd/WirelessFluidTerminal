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
package p455w0rd.wft.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import p455w0rd.ae2wtlib.api.networking.INetworkInfo;
import p455w0rd.wft.init.ModGuiHandler;
import p455w0rd.wft.sync.WFTPacket;

public class PacketOpenGui extends WFTPacket {

	private final int whichGui;
	private final int slot;
	private final boolean isBauble;

	// automatic.
	public PacketOpenGui(final ByteBuf stream) {
		whichGui = stream.readInt();
		slot = stream.readInt();
		isBauble = stream.readBoolean();
	}

	public PacketOpenGui(int gui, boolean isBauble, int slot) {
		whichGui = gui;
		this.slot = slot;
		this.isBauble = isBauble;
		final ByteBuf data = Unpooled.buffer();
		data.writeInt(getPacketID());
		data.writeInt(gui);
		data.writeInt(slot);
		data.writeBoolean(isBauble);
		configureWrite(data);
		ModGuiHandler.setIsBauble(isBauble);
		ModGuiHandler.setSlot(slot);
	}

	@Override
	public void serverPacketData(final INetworkInfo manager, final WFTPacket packet, final EntityPlayer player) {
		if (slot >= 0) {
			ModGuiHandler.open(whichGui, player, player.getEntityWorld(), player.getPosition(), isBauble, slot);
		}
	}

	@Override
	public void clientPacketData(final INetworkInfo network, final WFTPacket packet, final EntityPlayer player) {
	}
}
