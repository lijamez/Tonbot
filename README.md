# Tonbot [![Build Status](https://travis-ci.org/lijamez/Tonbot.svg?branch=master)](https://travis-ci.org/lijamez/Tonbot) [![Dependency Status](https://www.versioneye.com/user/projects/599a0b220fb24f0bd02f772e/badge.svg?style=flat)](https://www.versioneye.com/user/projects/599a0b220fb24f0bd02f772e)

A modular Discord bot. To give Tonbot some functionality, enable plugins in the config.

## Plugins
To enable these plugins, run Tonbot once so that it generates config file ``~/.tonbot/tonbot.config``. Then add any of the plugin names below to the ``plugins`` list.

### [Music](https://github.com/lijamez/tonbot-plugin-music) 
``net.tonbot.net.tonbot.plugin.music.MusicPlugin``

Plays music in a voice channel. (YouTube API Key and Spotify API key optional, but recommended.)

### [Interactive Fiction Player](https://github.com/lijamez/tonbot-plugin-ifplayer) 
``net.tonbot.plugin.ifplayer.IfPlayerPlugin``

Plays text adventure games. Many games in Z-code format (except .z6) work.

### [TMDb](https://github.com/lijamez/tonbot-plugin-tmdb) 
``net.tonbot.plugin.tmdb.TMDbPlugin``

Looks up Movie and TV info from TMDb. (TMDb API Key Required)

### [Time Info](https://github.com/lijamez/tonbot-plugin-time)
``net.tonbot.plugin.time.TimePlugin``

Gets time information from WolframAlpha. Use this to ask for time conversions, current time, etc. (WolframAlpha API Key Required)

### [Decision Maker](https://github.com/lijamez/tonbot-plugin-decisionmaker)
``net.tonbot.plugin.decisionmaker.DecisionMakerPlugin``

Flips coins, shuffles items, picks a random number. Only for the most important decisions.

## Installation
### Preparation
1. Install Oracle JDK 8 in your environment. Yes, it must be Oracle JDK, not OpenJDK.
2. Create a Discord Bot App (and therefore a Bot Token) from https://discordapp.com/developers/applications/me. Take note of your Client ID (public) and Token (keep secret).
3. Decide on what plugins above you're going to enable and get the API keys for those services, if any.

### Get and Configure Tonbot
1. Clone the repository by running ``git clone https://github.com/lijamez/Tonbot.git`` in a directory of your choice. This will create a new ``Tonbot`` directory.
2. Go into that directory with ``cd Tonbot`` and run ``./gradlew run``. The first run will generate a ``.tonbot`` directory in your home directory.
3. Edit the ``~/.tonbot/tonbot.config`` file. You need to fill in the ``discordBotToken``, and a list of plugin names that you intend to enable.
4. Run ``./gradlew run`` again. This time, the bot should go online. However, your plugins may need further configuration, so stop the Tonbot process.
5. Go to ``~/.tonbot/plugin_config`` and edit the configs of each plugin that you enabled. Check the GitHub page of the plugint that you are enabling for instructions. (TODO: This step may not work yet, since some plugins currently don't generate their own barebones config.)
6. Once the plugin configs are set up, run ``./gradlew run`` again. If all is well, the plugins should initialize properly.

### Updating Tonbot (and its plugins)
NOTE: At this time, I can't guarantee that future commits will be backwards compatible.

1. Go to the ``Tonbot`` directory and run ``git pull``.
2. There is no step 2.

### Invite your instance of Tonbot into a server. 
Go to ``https://discordapp.com/oauth2/authorize?client_id={YOUR BOT CLIENT ID HERE}&scope=bot&permissions=0``

## Usage
### Show Help
The following command shows the available commands based on the plugins you have loaded.
```
t, help
```

You can get more information about a command by supplying some arguments. For example, to get more information about the ``t, music play`` command, say:

```
t, help music play
```

### Set up permissions
Maybe you don't want everyone to be able to access all commands. You can set up permission controls right from the server. 

TODO

## Concept
Tonbot reacts to messages that:
1. Start with the configured prefix
2. Contain a valid **route** to an existing **activity**

To run an activity, a message sent to a channel must be structured like so:
``{PREFIX}{ROUTE} {ARGS}``

Example: ``t, music play sound of silence``

Where:
* PREFIX == ``t, ``
* ROUTE == ``music play``
* ARGS == ``sound of silence``

Every Activity has a **canonical** route to activate them. If you feel that the canonical route is too long and therefore cumbersome to type out, consider setting up an alias (see below).

## Configuration (tonbot.config)

### Prefix
The prefix that Tonbot will watch out for.

### Discord Bot Token
Your Discord app's token. Must be set or else you literally cannot connect to Discord. Make sure that this is kept secret.

### Color
The RGB value of accents. This field is required.

### Plugins
A list of fully qualified plugin names to be loaded.

Example:
```json
"plugins" : [
  "net.tonbot.plugin.decisionmaker.DecisionMakerPlugin",
  "net.tonbot.plugin.music.MusicPlugin"
]
```

### Aliases
Aliases are basically shortcuts to activities. For example, if "music play" is a route and you feel it's too long to type out, then you can set up an alias "p". Instead of having to type out ``t, music play hello adele`` you can just type ``t, p hello adele``.

Aliases must be a struct with the alias as the field names and the **canonical** actvitiy route name as the value.

Example:
```json
"aliases" : {
  "p" : "music play",
  "j" : "music join",
  "np" : "music nowplaying",
  "l" : "music list",
  "q" : "music list",
  "s" : "music skip",
  "seek" : "music seek"
}
```

## Acknowledgements
* Built with [Discord4J](https://github.com/austinv11/Discord4J)
