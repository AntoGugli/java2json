# Java Class to JSON Serializer Changelog

## [1.0.0]
### Added
- **Automatic JSON Generation**: Instant serialization of the Java class in the active editor tab when opening the tool window.
- **Native Search Experience**: Full support for integrated search via `Ctrl+F` (Windows/Linux) or `Cmd+F` (macOS).
- **Advanced Navigation**: Navigate through search results using native UI buttons, Arrow keys, or the Enter key.
- **Cross-Platform Support**: Optimized keyboard shortcuts for both Windows and macOS users.
- **Smart Formatting**: JSON output is beautified with a clean and consistent **4-space indentation**.
- **Recursive Serialization**: Deep analysis of custom Java objects and their fields.
- **Enhanced Type Support**:
  - Automatic **Enum** handling (returns the first constant name).
  - Support for Java Collections (List, Set) and Maps.
  - Handling of primitive types, wrappers, and Strings.
- **UI/UX Improvements**: Syntax highlighting, smooth scrolling, and background processing to keep the IDE responsive.
- **Safety Features**: Circular reference detection and automatic filtering of technical fields like `serialVersionUID`.