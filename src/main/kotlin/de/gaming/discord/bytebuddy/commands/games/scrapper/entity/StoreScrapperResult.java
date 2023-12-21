package de.gaming.discord.bytebuddy.commands.games.scrapper.entity;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class StoreScrapperResult {
    private String title;
    private BigDecimal price;
    private String imageUrl;
    private String releaseDateText;
}
