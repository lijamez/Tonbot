# Tonbot [![Build Status](https://travis-ci.org/lijamez/Tonbot.svg?branch=master)](https://travis-ci.org/lijamez/Tonbot) [![Dependency Status](https://www.versioneye.com/user/projects/599a0b220fb24f0bd02f772e/badge.svg?style=flat)](https://www.versioneye.com/user/projects/599a0b220fb24f0bd02f772e)

A modular Discord bot. To give Tonbot some functionality, enable plugins in the config.

## Plugins
To enable these plugins, run Tonbot once so that it generates config file ``~/.tonbot/tonbot.config``. Then add any of the plugin names below to the ``plugins`` list.

### [Music](https://github.com/lijamez/tonbot-plugin-music) 
``net.tonbot.net.tonbot.plugin.music.MusicPlugin``

Plays music in a voice channel. (YouTube API Key required. Spotify API key optional.)

### [Interactive Fiction Player](https://github.com/lijamez/tonbot-plugin-ifplayer) 
``net.tonbot.plugin.ifplayer.IfPlayerPlugin``

Plays text adventure games. Many games in Z-code format (except .z6) work.

### [TMDb](https://github.com/lijamez/tonbot-plugin-tmdb) 
``net.tonbot.plugin.tmdb.TMDbPlugin``

Looks up Movie and TV info from TMDb. (TMDb API Key Required)

## [Time Info](https://github.com/lijamez/tonbot-plugin-time)
``net.tonbot.plugin.time.TimePlugin``

Gets time information from WolframAlpha. Use this to ask for time conversions, current time, etc. (WolframAlpha API Key Required)

## [Decision Maker](https://github.com/lijamez/tonbot-plugin-decisionmaker)
``net.tonbot.plugin.decisionmaker.DecisionMakerPlugin``

Flips coins, shuffles items, picks a random number. Only for the most important decisions.
