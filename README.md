# ConduitFly Plugin

ConduitFly is a Minecraft plugin that allows players to fly within designated areas around Conduits. It integrates with permissions and rank systems, as well as providing particle effects to visualize the area of flight.

## Features

- **Flight System**: Allows players to fly within specific areas around Conduits.
- **Conduit Area Display**: Players can toggle the display of the Conduit’s flight area.
- **Rank System**: Customizable ranks with different flight durations, radius, and permissions.
- **Particle Effects**: Visualize the Conduit area with customizable particle effects.
- **MySQL/SQLite Storage**: Supports saving and loading Conduit data from a MySQL or SQLite database.
- **Configurable Messages**: Fully configurable messages for various events, including flight mode activation, area toggles, and rank up notifications.

## Installation

1. **Download** the latest release of the plugin from the [Releases page](https://github.com/ogsammaenr/ConduitFly/releases).
2. **Place** the `.jar` file in the `plugins` folder of your server.
3. **Restart** or **reload** your server to load the plugin.
4. **Configure** the plugin in the `config.yml` file to customize settings such as particle effects, rank permissions, and Conduit area radius.

## Configuration

### `config.yml`

The plugin configuration file is located in the plugin’s folder (`plugins/ConduitFly/config.yml`). Here you can configure the following:

- **Particle Effects**: Customize the particle effects used to visualize the Conduit’s area. For example, the color and size of the particle for the DUST effect.
- **Rank Settings**: Customize the ranks, including flight duration, area radius, and the fall damage protection.
- **Messages**: All plugin messages (e.g., flight mode enabled, area toggled) can be customized here.

### `messages.yml`

The plugin supports full localization of messages. Customize the messages for various events such as flight mode activation, area display toggles, and rank upgrades.

Example:

```yaml
# Flight mode messages
flight:
  enabled: "&aFlight mode enabled."
  disabled: "&cFlight mode disabled."
