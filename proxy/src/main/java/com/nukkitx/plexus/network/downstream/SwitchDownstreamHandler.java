package com.nukkitx.plexus.network.downstream;

import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.plexus.network.session.ProxyPlayerSession;
import com.nukkitx.protocol.bedrock.packet.MovePlayerPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackClientResponsePacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePackStackPacket;
import com.nukkitx.protocol.bedrock.packet.ResourcePacksInfoPacket;
import com.nukkitx.protocol.bedrock.packet.SetLocalPlayerAsInitializedPacket;
import com.nukkitx.protocol.bedrock.packet.SetPlayerGameTypePacket;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;

public class SwitchDownstreamHandler extends InitialDownstreamHandler {

    public SwitchDownstreamHandler(ProxyPlayerSession player) {
        super(player);
    }

    @Override
    public boolean handle(ResourcePacksInfoPacket packet) {
        ResourcePackClientResponsePacket clientResponsePacket = new ResourcePackClientResponsePacket();
        clientResponsePacket.setStatus(ResourcePackClientResponsePacket.Status.HAVE_ALL_PACKS);
        this.player.getConnectingDownstream().sendPacket(clientResponsePacket);
        return true;
    }

    @Override
    public boolean handle(ResourcePackStackPacket packet) {
        ResourcePackClientResponsePacket clientResponsePacket = new ResourcePackClientResponsePacket();
        clientResponsePacket.setStatus(ResourcePackClientResponsePacket.Status.COMPLETED);
        this.player.getConnectingDownstream().sendPacket(clientResponsePacket);
        return true;
    }

    @Override
    public boolean handle(StartGamePacket packet) {
        SetPlayerGameTypePacket gamemodePacket = new SetPlayerGameTypePacket();
        gamemodePacket.setGamemode(packet.getPlayerGameType().ordinal());
        this.player.getUpstream().sendPacket(gamemodePacket);

        SetLocalPlayerAsInitializedPacket initializedPacket = new SetLocalPlayerAsInitializedPacket();
        initializedPacket.setRuntimeEntityId(packet.getRuntimeEntityId());
        this.player.getConnectingDownstream().sendPacket(initializedPacket);

        MovePlayerPacket movePlayerPacket = new MovePlayerPacket();
        movePlayerPacket.setPosition(packet.getPlayerPosition());
        movePlayerPacket.setRuntimeEntityId(packet.getRuntimeEntityId());
        movePlayerPacket.setRotation(Vector3f.from(packet.getRotation().getX(), packet.getRotation().getY(), packet.getRotation().getY()));
        movePlayerPacket.setMode(MovePlayerPacket.Mode.RESPAWN);
        this.player.getUpstream().sendPacket(movePlayerPacket);

        this.player.getConnectingDownstream().sendPacket(this.player.getChunkRadius());
        this.player.getConnectingDownstream().setPacketHandler(new ConnectedDownstreamHandler(this.player));
        return true;
    }
}
