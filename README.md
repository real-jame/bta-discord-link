# Discord Server Status

Display live statistics for your BTA server on Discord!

## Current Features
- Display stats on your server via voice channels:
  - Uptime
  - Player count
  - World file size

This is a really basic version so that's about it. And the code could use some work, I just wanted to get it done (also, this was my first time coding Java and Minecraft mods).

## Setup
**Get the server ready:** Download the Babric server for **1.7.7.0_02** from https://github.com/Turnip-Labs/babric-instance-repo/releases and add this mod's jar file from https://github.com/real-jame/bta-server-status-mod/releases to the mods directory. When you run the mod for the first time, it will create a config file for you to fill out in `config/serverstatus.yaml` at your server directory.

**Get the Discord bot ready:** Visit the [Discord Developer Portal](https://discord.com/developers/applications) to create a new application. Customize the name, images, etc. however you want; it's only for your personal use. Go to the bot tab and copy the **token** to the config file's `token` option, replacing the placeholder value.

**Invite the Discord bot:** To do so, take this link and replace 123456789012345678 with your bot's token, then open it in your browser:

```https://discord.com/api/oauth2/authorize?client_id=123456789012345678&permissions=0&scope=bot%20applications.commands```

**Give the bot permissions:** Give it a role with the permissions it needs: Manage Channels. However, I instead had to give it Administrator permissions because it would error otherwise. Not sure why.

**Get the Discord server ready:** Turn on Developer Mode in Discord's Settings > Advanced tab, so you can copy IDs for the bot to access. Right click on the server icon and copy the ID to the `guildId` option. Create a category for the bot to put its stat channels in (moving it to the top of your channels list is recommended), right click on it to copy the ID to the `categoryId` option.

**One more step:** If you use a different name for your world folder, make sure to change the `worldName` option to whatever yours is named. 

That's all. Start your server again, and the bot will now update statistics every 5 minutes as long as the server is running! (Why 5? Discord rate limits.)

## TODOs
- Customize which stats are displayed
- Customize how often stats update
- Display stats in a text channel for realtime updating
- More stats
- Discord <-> Minecraft chat bridge
- Print server output to a text channel
- Run commands on the server from a text channel

Basically, make it more of a bridge between your BTA server and your Discord server. These features would be toggleable, of course.

----
Developer info I kept from the template README below lol:

## Prerequisites for compiling
- JDK for Java 17 ([Eclipse Temurin](https://adoptium.net/temurin/releases/) recommended)
- IntelliJ IDEA
- Minecraft Development plugin (Optional, but highly recommended)

## IntelliJ Setup instructions

1. After importing the project into IntelliJ, close it and open it again.  
   If that does not work, open the right sidebar with `Gradle` on it, open `Tasks` > `fabric` and run `ideaSyncTask`.

2. Create a new run configuration by going in `Run > Edit Configurations`.  
   Then click on the plus icon and select Gradle. In the `Tasks and Arguments` field enter `build`.  
   Running it will build your finished jar files and put them in `build/libs/`.

3. While in the same place, select the Client and Server run configurations and edit the VM options under the SDK selection.

   ![image](https://github.com/Turnip-Labs/bta-example-mod/assets/58854399/2d45551d-83e3-4a75-b0e6-acdbb95b8114)

   Click the double arrow icon to expand the list, and append `-Dfabric.gameVersion=1.7.7.0` to the end.

   ![image](https://github.com/Turnip-Labs/bta-example-mod/assets/58854399/e4eb8a22-d88a-41ef-8fb2-e37c66e18585)

4. Lastly, open `File` > `Settings` and head to `Build, Execution, Development` > `Build Tools` > `Gradle`.  
   Make sure `Build and run using` and `Run tests using` is set to `Gradle`.
