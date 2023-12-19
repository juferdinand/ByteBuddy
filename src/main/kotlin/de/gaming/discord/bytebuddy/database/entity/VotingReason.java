package de.gaming.discord.bytebuddy.database.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum VotingReason {

    NEW_GAMES("Neues Spiele"),
    TODAY_GAMES("Heutige Spiele");


    private String desc;

    //find by name
    public static VotingReason findByName(String name) {
        for (VotingReason v : values()) {
            if (v.getDesc().equalsIgnoreCase(name)) {
                return v;
            }
        }
        return null;
    }

}
