# Ctrl-Q

Client-side mod for Minecraft 1.8.9 Forge that enables CTRL+Q for dropping entire item stacks. Primarily designed for macOS users where CMD+Q quits the application instead of dropping items.

## Installation

1. Install Minecraft Forge 1.8.9
2. Place the mod JAR in your `mods` folder
3. Launch Minecraft

## Usage

Press CTRL+Q to drop entire item stacks:
- In hotbar (when no GUI is open)
- In inventory containers (chests, furnaces, etc.)

The mod respects your configured drop key binding.

## Technical Implementation

- Uses Forge event system (`InputEvent.KeyInputEvent`, `GuiScreenEvent.KeyboardInputEvent`)
- Implements proper networking via `PlayerController.windowClick()`
- Reflection-based slot access for GUI containers
- Compatible with vanilla servers and anti-cheat plugins

## Limitations

- Creative Mode inventory is not supported
- Requires exactly Minecraft 1.8.9 with Forge

## Building

```bash
JAVA_HOME=/path/to/java8 ./gradlew build
```

Requires Java 8 for compatibility with Minecraft 1.8.9.

## License

MIT License