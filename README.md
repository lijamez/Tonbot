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

## Installation
### Preparation
1. Install Oracle JDK 8 in your environment. Yes, it must be Oracle, not OpenJDK.
2. Create a Discord Bot App (and therefore a Bot Token) from https://discordapp.com/developers/applications/me. Take note of your Client ID (public) and Token (keep secret).
3. Decide on what plugins above you're going to enable and get the API keys for those services, if any.

### Get and Configure Tonbot
1. Clone the repository by running ``git clone https://github.com/lijamez/Tonbot.git`` in a directory of your choice. This will create a new ``Tonbot`` directory.
2. Go into that directory with ``cd Tonbot`` and run ``./gradlew run``. The first run will generate a ``.tonbot`` directory in your home directory.
3. Edit the ``~/.tonbot/tonbot.config`` file. You need to fill in the ``discordBotToken``, and a list of plugin names that you intend to enable.
4. Run ``./gradlew run`` again. This time, the bot should go online. However, your plugins may need further configuration, so stop the Tonbot process.
5. Go to ``~/.tonbot/plugin_config`` and edit the configs of each plugin that you enabled. Check the GitHub page of the plugint that you are enabling for instructions. (TODO: This step doesn't work yet, since plugins currently don't generate their own barebones config.)
6. Once the plugin configs are set up, run ``./gradlew run`` again. If all is well, the plugins should initialize properly.

### Updating Tonbot (and its plugins)
NOTE: At this time, I can't guarantee that future commits will be backwards compatible.

1. Go to the ``Tonbot`` directory and run ``git pull``.
2. There is no step 2.

### Invite your instance of Tonbot into a server. 
Go to ``https://discordapp.com/oauth2/authorize?client_id={YOUR BOT CLIENT ID HERE}&scope=bot&permissions=0``
