package de.tmxx.abilities.entity;

/**
 * Project: abilities
 * 09.03.2025
 *
 * @author timmauersberger
 * @version 1.0
 */
public enum FallingBlockType {
    BLUE_ICE(13954);

    private final int protocolId;

    FallingBlockType(int protocolId) {
        this.protocolId = protocolId;
    }

    public int getProtocolId() {
        return protocolId;
    }
}
