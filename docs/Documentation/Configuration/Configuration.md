---
icon: fontawesome/solid/wrench
---
# Configuration

The configuration of BetonQuest is mainly done in the `config.yml` file. All of its options are described on this page.
There is also additional information about backups, updates and database transfers.
If you fail to understand options just keep their default values. You can always change them when
you have gained a more complete understanding of this plugin. 


## Config Options

!!! warning
    **Do not touch `version:` option! It may corrupt your files!**


### MySQL Database

#### Setup

In order to use a MySQL Database for saving all the data you need to fill out the mysql config section.
``` YAML linenums="1"
mysql:
  enabled: true   #Set this to true.
  host: ''        #This is the IP of your MySQL server. If it runs on the same machine as your server use localhost or 127.0.0.1 
  port: ''        #This is the port your MySQL server runs on.
  user: ''        #The name of the database user that is used to connect to the database server.
  pass: ''        #The password of that user.
  base: ''        #The database that BetonQuest will write to. You need to create it in your database server.
  prefix: betonquest_  #The table prefix of BetonQuest's data in the database.
  reconnect_interval: 1000 #The time intervall the database tries to reconnect if the connection gets lost
```


#### Migrating a database from SQLite to MySQL and back

Follow these few simple steps to migrate your database easily:

1. Create a backup with **/q backup** command.
2. Extract database backup from it.
3. Turn the server off.
4. Place the _database-backup.yml_ file inside the plugin's directory.
5. Edit which database type you want to use by setting the `enabled` option in the `mysql` section to true or false.
6. Start the server.
7. Check for errors.
8. If there are no errors, enjoy your migrated database.
9. If there are any errors, post them to the developer or try to fix them if you know how.

### Default Language
 
`language:` is the default translation of the plugin for every new player. Currently, there are 9 languages available: 
English (en), Polish (pl), German (de), French (fr), Spanish (es), Chinese (cn), Dutch (nl), Italian (it) and Hungarian (hu).

### Updating
The `update` section controls the Auto-Updater. It has the following settings:

* `enabled` (default `true`). Enables or disables the Updater. If set to false, it is not possible to update with the
  updater and no version checks are executed.
* `strategy` (default `MINOR`). The update strategy is the most important feature of the Auto-Updater. An explanation is
  available [here](Version-Changes/Updating.md#choose-an-update-strategy).
* `automatic` (default `true`). If true the updater will download new Versions automatically. Otherwise, the updater
  will only download new versions when the update command is executed. Advice is
  available [here](Version-Changes/Updating.md#enable-or-disable-automatic-updates).

### Journal slots

`default_journal_slot` is the inventory slot in which the journal will appear after using the `/journal` command.
BetonQuest will try to move items out of the way if the slot is occupied. If the inventory is full the journal will not
be added. You can disable this behaviour by setting the option to `-1`. BetonQuest will then just use any free slot.

### Npc left click

`npcs.accept_left_click` activates that a conversation with an NPC can also be started by left-clicking the NPC and not only
by right-clicking the NPC.

### Npc interact limit

`npcs.interaction_limit` prevents NPC / BetonQuest conversation click spamming. The time's unit is milliseconds. Default
value: `500`

### Conversation End Distance

`max_conversation_distance` is the distance you need to walk away from the NPC for the conversation to end (in the case of using
chat-based conversation interface).

### Default Conversation Style

`default_conversation_IO` is a comma-separated list of conversation styles. The first one that is loaded
(depending on the available 3rd party plugin integrations) is used.
See [conversation styles](../Features/Conversations.md#conversation-displaying) for supported styles.

### Default Chat interceptor

`default_interceptor` is a comma-separated list of chat interceptors. The first one that is loaded
(depending on the available 3rd party plugin integrations) is used.
See [chat interceptors](../Features/Conversations.md#chat-interceptors) for supported chat interceptors.

### Default Hologram Plugin
The holograms related features work with multiple plugins.
You can use the `default_hologram` option to set which plugin should be used.
        
`default_hologram: DecentHolograms,HolographicDisplays` will make DecentHolograms the default hologram plugin. If it is not installed, 
HolographicDisplays will be used instead. If you want to use HolographicDisplays as the default, you can use `default_hologram: HolographicDisplays`


### Conversation Chat Display options
`display_chat_after_conversation` this will prevent all chat messages from displaying during a conversation and it will show them once it's finished.

### Combat Delay
`combat_delay` is a delay (in seconds) the player must wait before starting a conversation after combat.

### Conversation pullback message
`notify_pullback` will display a message every time the player is pulled back by the `stop` option in conversations (in the case of chat-based conversations).
It notifies players that they are in a conversation, and the pullback is not a bug.

### Adjusting the backpack to the KeepInventory gamerule
`remove_items_after_respawn` option should be turned on if "keepInventory" gamerule is not being used. 
It prevents other plugins from duplicating quest items after death.

When a player dies, their quest items are removed from drops and stored in the backpack, but some plugins may try to
restore all items to the player (for example WorldGuard custom flag keep-inventory).
That is why BetonQuest removes the quest items that are in a player's inventory after they respawn again, to be sure they were not re-added. 
The "keepInventory" gamerule, however, works differently - the items are never dropped, so they cannot be added to backpack. 
Removing them from the inventory would destroy them forever. Sadly, Bukkit does not allow for gamerule checking, so it is up to you to decide.

Once again, if you have "keepInventory" gamerule true, this setting has to be false and vice versa.
    
### Quest Items break behaviour
`quest_items_unbreakable` controls whether quest items can be broken by using them.
This was used in the past when the `unbreakable` tag couldn't be added to items.
Turn it off and make your quest items unbreakable by vanilla means.

### Player Hider interval
`player_hider_check_interval` the interval in which the PlayerHider checks the conditions and updates the player's visibility.
Cannot be disabled currently. If you want this to be semi-disabled set a very high value. 

### NPC Hider interval
`npc_hider_check_interval` is the interval in which the NPCHider checks the conditions and updates the NPC's visibility.
Cannot be disabled currently. If you want this to be semi-disabled set a very high value.

### Hologram update interval
`hologram_update_interval` is the interval in which the holograms check the conditions and update content and visibility.
Cannot be disabled currently. If you want this to be semi-disabled set a very high value.

### Sounds
This section defines what sounds will be played on these occasions:

  * `start` when a conversation starts
  * `end` when a conversation ends
  
A list of all possible sounds for the latest Minecraft version can be found [here](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html).
And sounds can be disabled by setting them to `false`.
    
### Conversation Command Banlist
`cmd_blacklist` is a list of commands that cannot be used during a conversation. Remember that you can only type single words (command names) here!

### Compatibility Hooks
`hook` controls compatibility with other plugins. You can turn off each hook here.

### Journal
`journal` controls various settings of the journal:

  * `chars_per_page` is the number of characters before a page break. If it is set too high, the text on a journal page can overflow and become invisible.
   **This was replaced by `chars_per_line` and `lines_per_page` and is only required if you don't like the new behaviour.**
  * `chars_per_line` is the number of characters before a line break.
   If it is set too high, the text on a journal page can overflow and become invisible.
   If this is not set, BQ will fall back on the old page wrapping behaviour configured through `chars_per_page`.
  * `lines_per_page` is the number of lines before a new page. If it is set too high, the text on a journal page can overflow and become invisible. 
   This is only required if `chars_per_line` is set.
  * `one_entry_per_page` makes each entry take a single page. Note that it will not expand to other pages even if it overflows, so keep your entries short.
  * `reversed_order` controls the chronological order of entries in the journal. By default, the entries are ordered from newest to oldest.
   It is reversible, but this will force players to click through many pages to get to the most recent entry.
  * `hide_date` hides the date of each entry. Set it to true if you don't want this functionality.
  * `full_main_page` makes the main page always take a full page. If a lot of information is being displayed, it is advised to make this true.
   If you use the main page only for small notifications, set it to false, so the entries can follow immediately.
  * `show_separator` shows a separator between journal entries (default: true). Customize the separator in `config.yml` with the key `journal.separator`.
  * `show_in_backpack` whether to display the journal in the backpack when there is no journal in the player's inventory.
  * `lock_default_journal_slot` locks the journal to the `default_journal_slot`.
  * `give_on_respawn` adds the journal to the player inventory.
  * `custom_model_data` sets the custom model data of the journal item.
  
### Journal Colors
`journal_colors` controls the colors used in the journal. It takes color codes without the `&` character.

  * `date.day` is the day number
  * `date.hour` is the hour number
  * `line` is the delimiter between entries
  * `text` is the text of the entry
    
### Conversation Colors
`conversation_colors` controls the colors of the conversation. It takes [color names](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/ChatColor.html). 
If you want to add a font style (bold, italic etc.) do so after placing a comma.

  * `npc` is the name of the NPC
  * `player` is the name of the player
  * `text` is the NPC's text
  * `answer` is the text of player's answer (after choosing it)
  * `number` is the option number
  * `option` is the text of an option

### Conversation Settings: ChestIO & SlowTellrawIO

* `conversation_IO_config` manages settings for individual conversation IO's:
  - `chest` manages settings for the chest conversation IO
    - `show_number` will show the player number option if true (default: true)
    - `show_npc_text` will show the npc text in every player option if true (default: true)
  - `slowtellraw` manages settings for the slowtellraw conversation IO
    - `message_delay` is the delay ticks between each message (default: 2)

### Quest downloader

`download` controls security restrictions for the [`/q download`](Commands-and-permissions.md) command:

* `pullrequests` defines if pull requests may be downloaded. **Only enable this if you really know what you are doing!**
  Everyone can open pull requests that could contain malicious files and if a permission misconfiguration occurs this
  will make your server vulnerable to attacks.
* `repo_whitelist` is a list of trusted GitHub repositories from which quests and templates can be downloaded.  
  By default only or official tutorial repo [BetonQuest/Quest-Tutorials](https://github.com/BetonQuest/Quest-Tutorials)
  is on this list.

### Items

The items section allows you to override the default items that are displayed across BetonQuest's GUIs.
All items you specify here need to be defined in a package. You need to reference the item with a full path.

The `cancel_button` and `compass_button` can be removed from the backpack by setting their values to `""`.

```YAML
items:
  backpack: #(1)!
    previous_button: "my_package.backpack_previous_button" #(2)!
    next_button: "my_package.backpack_next_button" #(3)!
    cancel_button: "my_package.backpack_cancel_button" #(4)!
    compass_button: "my_package.backpack_compass_button" #(5)!
```

1. All items that are used by the backpack.
2. The button to go to the previous backpack page.
3. The button to go to the next backpack page.
4. The quest cancelers. Can be removed from the backpack by setting the value to `""`.
5. The quest compass. Can be removed from the backpack by setting the value to `""`.

### Menu
* `default_close`:  Sets if menus should close by default when an item is clicked (`true`) or if they should stay open (`false`).
  Each menu can also override this.

### Misc settings

* `date_format` is the Java [date format](https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html)
  used in journal dates. It needs to have a space between the day and hour.
* `debug` control debug log settings. The debug log contains a lot of additional information about the plugin's activity.
    * `enabled` if enabled the debug log is printed to the _latest.log_ file in the _BetonQuest/logs_ directory.
      Turning this on can slow your server down.
    * `history_in_minutes` the plugin always caches debugging activity to the memory.
       Once `debug` is enabled this history is printed to the log file and no longer stored in memory.
       This is invaluable for debugging issues that happened in the past without having debug enabled all the time.
       The downside is that depending on the amount of scripts and players this can be a lot of data occupying your memory.
       So if you have memory issues, you can reduce the time the history is stored in memory down to 0 minutes to disable it.
* `message_parser` sets the default message parser.
  For more information, see the [Message Formatting](../Features/Message-Formatting.md) page.
