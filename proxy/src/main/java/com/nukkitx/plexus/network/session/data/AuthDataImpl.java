package com.nukkitx.plexus.network.session.data;

import com.nukkitx.protocol.bedrock.session.data.AuthData;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@NonNull
public class AuthDataImpl implements AuthData {
    private final String displayName;
    private final UUID identity;
    private final String xuid;
}
