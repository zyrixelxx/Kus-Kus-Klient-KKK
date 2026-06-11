# Kus Kus Klient

Kus Kus Klient is a Fabric utility client base for Minecraft `1.21.11` with a modular architecture, custom ClickGUI, HUD editor, profile snapshots, and a lightweight addon API.

License: `GPL-3.0`

## Features

- Modular client architecture with separate managers for modules, commands, HUD, friends, profiles, macros, configs, and addons
- ClickGUI with persistent settings and theme storage
- HUD editor with draggable elements and saved layout
- Profile snapshot system for saving and loading full client state
- Addon API with lifecycle hooks and automatic Fabric entrypoint loading
- Safer JSON config handling with backup/corruption recovery

## Requirements

- Java `21`
- Fabric Loader `0.19.2+`
- Fabric API for Minecraft `1.21.11`

## Development

```powershell
.\gradlew.bat genSources
.\gradlew.bat runClient
.\gradlew.bat build
```

Build output is written to `build/libs/`.

## Default Controls

- `Right Shift` opens the ClickGUI
- `F12` opens the HUD editor
- `Ctrl + F` opens module search inside the GUI
- Left click toggles a module
- Right click opens module settings

## Commands

Default prefix: `.`

- `.help [page]`
- `.friend add|remove|list|note <name> [note]`
- `.prefix <symbol>`
- `.config save`
- `.config load`
- `.config reload`
- `.config reset`
- `.config profile save|load|delete|list <name>`

## Config Layout

Live client data is stored in `.minecraft/kuskusklient/`.

- `config.json`
- `friends.json`
- `hud.json`
- `macros.json`
- `modules/*.json`
- `profiles/<name>/...`
- `addons/<addon-id>/...`
- `crashes/*.txt`

The codebase also contains support for full config snapshots, but the default public command surface currently exposes profile snapshots through `.config profile ...`.

## Adding Modules

Create a class extending `me.kuskus.module.Module`, define one or more `SettingGroup`s, subscribe to the event bus in `onEnable()`, unregister in `onDisable()`, and register the module through `ModuleManager`.

```java
public class MyModule extends Module {
    public MyModule() {
        super("MyModule", "Example module.", Category.MISC);
        group("General").add(new BoolSetting("Enabled Option", "Example setting.", true));
    }
}
```

## Addon API

The client supports Fabric entrypoint discovery for addons with the entrypoint key `kuskusklient-addon`.

Minimal addon example:

```java
public final class ExampleAddon extends Addon {
    @Override
    public String name() {
        return "ExampleAddon";
    }

    @Override
    public AddonMetadata metadata() {
        return new AddonMetadata(
            "example-addon",
            "Example Addon",
            "1.0.0",
            "YourName",
            "Sample extension for Kus Kus Klient."
        );
    }

    @Override
    public void onInitialize(AddonContext context) {
        context.logger().info("Addon data dir: {}", context.dataDirectory());
    }

    @Override
    public List<Module> modules() {
        return List.of(new MyModule());
    }
}
```

Addon `fabric.mod.json` entrypoint example:

```json
{
  "entrypoints": {
    "kuskusklient-addon": [
      "com.example.ExampleAddon"
    ]
  }
}
```

You can also register addons manually with `KusKusKlient.ADDONS.register(new ExampleAddon())`, but the Fabric entrypoint path is the recommended public API.

## Discord Rich Presence

1. Create an application at <https://discord.com/developers/applications>.
2. Copy the Application ID.
3. Put the ID into the `DiscordPresence` module setting.

If Discord is not running or the ID is invalid, the module disables itself without crashing the client.

## Publishing Notes

- Keep only source files under version control and ignore build artifacts with the included `.gitignore`.
- Document addon compatibility using the client version from `AddonContext.clientVersion()`.
- If you change config formats again, bump `ConfigManager.CONFIG_VERSION`.
