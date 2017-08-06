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

public class MovieAction implements MessageReceivedAction {

    private static final List<String> ROUTE = ImmutableList.of("movie");

    private final TMDbClient tmdbClient;

    @Inject
    public MovieAction(TMDbClient tmdbClient) {
        this.tmdbClient = Preconditions.checkNotNull(tmdbClient, "tmdbClient must be non-null.");
    }

    @Override
    public List<String> getRoute() {
        return ROUTE;
    }

    @Override
    public void enact(MessageReceivedEvent event, String query) {
        MovieSearchResult result = this.tmdbClient.searchMovies(query);
        if (result.getResults().size() > 0) {
            MovieSearchResult.Movie topMatch = result.getResults().get(0);

            Movie movie = tmdbClient.getMovie(topMatch.getId());

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.withTitle(movie.getTitle());

            embedBuilder.withUrl("https://www.themoviedb.org/movie/" + movie.getId());

            List<String> descriptionComponents = new ArrayList<>();
            if (movie.getTagline().isPresent()) {
                descriptionComponents.add("*" + movie.getTagline().get() + "*");
            }
            if (movie.getOverview().isPresent()) {
                descriptionComponents.add(movie.getOverview().get());
            }
            String description = StringUtils.join(descriptionComponents, "\n\n");
            embedBuilder.withDescription(description);

            embedBuilder.appendField("Release Date", movie.getReleaseDate(), true);
            embedBuilder.appendField("Rating", movie.getVoteAverage() + "/10", true);

            List<String> genreNames = movie.getGenres().stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList());
            embedBuilder.appendField("Genres", StringUtils.join(genreNames, "\n"), true);

            if (movie.getPosterPath().isPresent()) {
                String imageUrl = tmdbClient.getImageUrl(movie.getPosterPath().get());
                embedBuilder.withImage(imageUrl);
            }

            BotUtils.sendEmbeddedContent(event.getChannel(), embedBuilder.build());
        } else {
            BotUtils.sendMessage(event.getChannel(), "No results found! :shrug:");
        }
    }


}
