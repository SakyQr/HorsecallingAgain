# Horse Calling AGAIN

**⚠️ BETA VERSION - Use with caution on production servers ⚠️**

A Minecraft Bukkit/Spigot plugin that enhances horse mechanics with persistent horse calling, taming improvements, and advanced horse management features.

## Features

- **Horse Whistling**: Call your tamed horses from anywhere in the world
- **Persistent Horse Data**: Your horses are saved between server restarts and player sessions
- **Combat Tracking**: Advanced combat detection to prevent horse abuse
- **Movement Tracking**: Monitor horse movement patterns and behaviors
- **Comprehensive Commands**: Easy-to-use commands for horse management
- **Auto-Save System**: Automatic data saving to prevent data loss
- **Thread-Safe Operations**: Robust data handling to prevent corruption

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/whistle` | Call your tamed horses to your location | `horsecalling.whistle` |
| `/tame` | Enhanced horse taming command | `horsecalling.tame` |
| `/horseinfo` | Display information about your horses | `horsecalling.info` |
| `/horsereload` | Reload plugin configuration | `horsecalling.admin` |

## Permissions

- `horsecalling.whistle` - Allows players to whistle for their horses
- `horsecalling.tame` - Allows players to use enhanced taming features
- `horsecalling.info` - Allows players to view horse information
- `horsecalling.admin` - Allows access to admin commands like reload

## Installation

**BETA NOTICE**: This plugin is currently in beta testing. Please backup your world and test on a staging server before using in production.

1. Download the latest beta release of HorsecallingAGAIN.jar
2. Place the jar file in your server's `plugins/` directory
3. Start or restart your server
4. The plugin will generate a default configuration file
5. Configure the plugin settings as needed
6. Use `/horsereload` to apply configuration changes

## Configuration

The plugin automatically generates a configuration file on first run. Key configuration options include:

- **Auto-save intervals**: Configure how often player data is saved
- **Save on logout**: Whether to save data when players disconnect
- **Combat tracking settings**: Configure combat detection parameters
- **Movement tracking options**: Adjust movement monitoring settings
- **Horse calling range**: Set limits on whistle distance and behavior

## Data Management

### Persistent Storage
- Player horse data is automatically saved to prevent loss
- Data is saved on player logout (if enabled)
- Automatic periodic saving during gameplay
- Safe shutdown procedures to prevent data corruption

### Thread Safety
- All data operations are thread-safe
- Proper synchronization prevents race conditions
- Robust error handling for edge cases

## Technical Details

### Architecture
- **ConfigManager**: Handles all configuration loading and validation
- **DataManager**: Manages persistent storage and auto-save functionality
- **HorseManager**: Core horse management and player data handling
- **CombatTracker**: Monitors combat states and prevents abuse
- **MovementTracker**: Tracks horse movement patterns

### Requirements
- Minecraft Server (Bukkit/Spigot/Paper)
- Java 21
- Kotlin runtime (included in plugin)

## Troubleshooting

### Common Issues

**Plugin won't start:**
- Check server console for error messages
- Ensure you have the correct server version
- Verify Java version compatibility

**Data not saving:**
- Check file permissions in the plugin directory
- Review configuration settings for save options
- Check console for data-related errors

**Commands not working:**
- Verify player has required permissions
- Check command syntax
- Ensure plugin is fully loaded

### Error Reporting

If you encounter issues:
1. Check the server console for error messages
2. Enable debug logging if available
3. Note the exact steps to reproduce the problem
4. Include server version and plugin version

## Development

### Building from Source
```bash
# Clone the repository
git clone [repository-url]

# Build with Gradle/Maven
./gradlew build
```

### Contributing
- Follow Kotlin coding standards
- Include proper error handling
- Add appropriate comments for complex logic
- Test thoroughly before submitting

## Changelog

### Latest Version (BETA)
- Improved thread safety for data operations
- Enhanced error handling during shutdown
- Fixed data saving on player logout
- Added comprehensive logging
- **Known Issues**: Some features may be unstable - report any bugs found

## License

[Insert your license information here]

## Support

**BETA SUPPORT**: As this is a beta version, please report any bugs or issues promptly. Include detailed reproduction steps and server logs.

---

*This plugin enhances the vanilla Minecraft horse experience while maintaining server performance and data integrity.*
