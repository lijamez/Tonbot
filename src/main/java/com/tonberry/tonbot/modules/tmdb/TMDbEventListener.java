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

    private final TMDbClient tmdbClient;
    private final String movieSearchPrefix;
    private final String tvSearchPrefix;

    @Inject
    public TMDbEventListener(TMDbClient tmdbClient, @Prefix String prefix) {
        this.tmdbClient = Preconditions.checkNotNull(tmdbClient, "tmdbClient must be non-null.");

        Preconditions.checkNotNull(prefix, "prefix must be non-null.");
        this.movieSearchPrefix = prefix + " movie ";
        this.tvSearchPrefix = prefix + " tv ";
    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            String messageString = event.getMessage().getContent();

            if (messageString.startsWith(movieSearchPrefix)) {
                String query = messageString.substring(movieSearchPrefix.length(), messageString.length());

                handleMovie(query, event);
            } else if (messageString.startsWith(tvSearchPrefix)) {
                String query = messageString.substring(tvSearchPrefix.length(), messageString.length());

                handleTv(query, event);
            }
        } catch (Exception e) {
            //TODO: Create a harness like this for every event listener.
            BotUtils.sendMessage(event.getChannel(), "Sorry, something went wrong. :confounded:");
            throw e;
        }
    }

    private void handleMovie(String query, MessageReceivedEvent event) {

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

    private void handleTv(String query, MessageReceivedEvent event) {

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
