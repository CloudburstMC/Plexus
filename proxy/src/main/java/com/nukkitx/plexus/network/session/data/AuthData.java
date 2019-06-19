package com.nukkitx.plexus.network.session.data;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@NonNull
public class AuthData {
    private final String displayName;
    private final UUID identity;
    private final String xuid;
}
