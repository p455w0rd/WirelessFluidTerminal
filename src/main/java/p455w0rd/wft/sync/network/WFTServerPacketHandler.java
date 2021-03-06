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
package p455w0rd.wft.sync.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketThreadUtil;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import p455w0rd.ae2wtlib.api.networking.INetworkInfo;
import p455w0rd.ae2wtlib.api.networking.IPacketHandler;
import p455w0rd.wft.sync.*;

public final class WFTServerPacketHandler extends WFTPacketHandlerBase implements IPacketHandler {

	private static final WFTServerPacketHandler INSTANCE = new WFTServerPacketHandler();

	public static final WFTServerPacketHandler instance() {
		return INSTANCE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPacketData(final INetworkInfo manager, final INetHandler handler, final FMLProxyPacket packet, final EntityPlayer player) {
		final ByteBuf stream = packet.payload();

		try {
			final int packetType = stream.readInt();
			final WFTPacket pack = PacketTypes.getPacket(packetType).parsePacket(stream);

			final PacketCallState callState = new PacketCallState() {

				@Override
				public void call(final WFTPacket appEngPacket) {
					appEngPacket.serverPacketData(manager, appEngPacket, player);
				}
			};

			pack.setCallParam(callState);
			PacketThreadUtil.checkThreadAndEnqueue(pack, handler, ((EntityPlayerMP) player).getServer());
			callState.call(pack);
		}
		catch (final Exception e) {
		}
	}
}
