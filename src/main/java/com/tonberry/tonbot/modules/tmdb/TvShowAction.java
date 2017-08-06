package com.tonberry.tonbot.modules.tmdb;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.tonberry.tonbot.common.BotUtils;
import com.tonberry.tonbot.common.MessageReceivedAction;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TvShowAction implements MessageReceivedAction {

    private static final List<String> ROUTE = ImmutableList.of("tv");

    private final TMDbClient tmdbClient;

    @Inject
    public TvShowAction(TMDbClient tmdbClient) {
        this.tmdbClient = Preconditions.checkNotNull(tmdbClient, "tmdbClient must be non-null.");
    }

    @Override
    public List<String> getRoute() {
        return ROUTE;
    }

    @Override
    public void enact(MessageReceivedEvent event, String query) {
        TvShowSearchResult result = this.tmdbClient.searchTvShows(query);
        if (result.getResults().size() > 0) {
            TvShowHit topMatch = result.getResults().get(0);

            TvShow tvShow = tmdbClient.getTvShow(topMatch.getId());

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.withTitle(tvShow.getName());

            embedBuilder.withUrl("https://www.themoviedb.org/tv/" + tvShow.getId());

            embedBuilder.withDescription(tvShow.getOverview());

            embedBuilder.appendField("Air Dates", tvShow.getFirstAirDate() + " to " + tvShow.getLastAirDate(), true);
            embedBuilder.appendField("Rating", tvShow.getVoteAverage() + "/10", true);
            embedBuilder.appendField("# of Episodes", Integer.toString(tvShow.getNumberOfEpisodes()), true);
            embedBuilder.appendField("# of Seasons", Integer.toString(tvShow.getNumberOfSeasons()), true);

            List<String> genreNames = tvShow.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList());
            embedBuilder.appendField("Genres", StringUtils.join(genreNames, "\n"), true);

            if (tvShow.getPosterPath().isPresent()) {
                String imageUrl = tmdbClient.getImageUrl(tvShow.getPosterPath().get());
                embedBuilder.withImage(imageUrl);
            }

            BotUtils.sendEmbeddedContent(event.getChannel(), embedBuilder.build());
        } else {
            BotUtils.sendMessage(event.getChannel(), "No results found! :shrug:");
        }
    }
}
