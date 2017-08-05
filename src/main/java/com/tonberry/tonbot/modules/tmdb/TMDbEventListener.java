package com.tonberry.tonbot.modules.tmdb;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.tonberry.tonbot.BotUtils;
import com.tonberry.tonbot.Prefix;
import org.apache.commons.lang3.StringUtils;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class TMDbEventListener {

    private final TMDbClient tmDbClient;
    private final String movieSearchPrefix;

    @Inject
    public TMDbEventListener(TMDbClient tmdbClient, @Prefix String prefix) {
        this.tmDbClient = Preconditions.checkNotNull(tmdbClient, "tmdbClient must be non-null.");

        Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.movieSearchPrefix = prefix + " movie ";
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            String messageString = event.getMessage().getContent();

            if (messageString.startsWith(movieSearchPrefix)) {
                String query = messageString.substring(movieSearchPrefix.length(), messageString.length());
                MovieSearchResult result = this.tmDbClient.searchMovies(query);
                if (result.getResults().size() > 0) {
                    MovieSearchResult.Movie topMatch = result.getResults().get(0);

                    Movie movie = tmDbClient.getMovie(topMatch.getId());

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.withTitle(movie.getTitle());

                    List<String> descriptionComponents = new ArrayList<>();
                    if (movie.getTagline().isPresent()) {
                        descriptionComponents.add("*" + movie.getTagline().get() + "*");
                    }
                    if (movie.getOverview().isPresent()) {
                        descriptionComponents.add(movie.getOverview().get());
                    }
                    String description = StringUtils.join(descriptionComponents, "\n\n");
                    embedBuilder.withDescription(description);

                    if (movie.getImdbId().isPresent()) {
                        embedBuilder.withUrl("http://www.imdb.com/title/" + movie.getImdbId().get());
                    }

                    embedBuilder.appendField("Release Date", movie.getReleaseDate(), true);
                    embedBuilder.appendField("Rating", movie.getVoteAverage() + "/10", true);

                    List<String> genreNames = movie.getGenres().stream()
                            .map(Genre::getName)
                            .collect(Collectors.toList());
                    embedBuilder.appendField("Genres", StringUtils.join(genreNames, "\n"), true);

                    if (movie.getPosterPath().isPresent()) {
                        String imageUrl = tmDbClient.getImageUrl(movie.getPosterPath().get());
                        embedBuilder.withImage(imageUrl);
                    }

                    BotUtils.sendEmbeddedContent(event.getChannel(), embedBuilder.build());
                } else {
                    BotUtils.sendMessage(event.getChannel(), "No results found! :shrug:");
                }

            }
        } catch (Exception e) {
            //TODO: Create a harness like this for every event listener.
            BotUtils.sendMessage(event.getChannel(), "Sorry, something went wrong. :confounded:");
            throw e;
        }
    }
}
