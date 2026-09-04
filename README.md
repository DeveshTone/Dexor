# Dexor

Dexor is a modern, minimal, and high-performance Android application management and ahead-of-time (AOT) compilation utility powered by the Android Runtime (`dex2oat`) and Shizuku. It provides an intuitive, fluid Jetpack Compose interface allowing users to inspect the live compilation status (compiler filters, status change reasons, installation sources) of user and system applications, run on-demand or batch DEX optimizations to boost app launch speed and runtime performance, and reclaim storage or reset compilation artifacts without needing root access.

## Screenshots

<!-- Add screenshots here -->
| App List | Detailed Compilation | Batch Operations |
|---|---|---|
| *(Screenshot Placeholder)* | *(Screenshot Placeholder)* | *(Screenshot Placeholder)* |

## Requirements

- **Android Version:** Android 9.0 to Android 15 (API levels 28 – 35).
- **Shizuku:** A working [Shizuku](https://github.com/RikkaApps/Shizuku) environment (running via Wireless Debugging, ADB, or Root) is required to execute elevated package compilation commands without root.
- **Orientation:** Portrait only.

## Compilation Modes

Dexor interfaces directly with Android's `cmd package compile` system commands. The available compilation filters and operations are:

- **`verify`**: Runs only DEX code verification. Does not perform Ahead-Of-Time (AOT) machine code compilation. Uses minimal disk space and takes almost no time.
- **`space`**: Optimizes the app with an emphasis on conserving storage space. Compiles code with size-reduction heuristics.
- **`speed`**: Performs maximum Ahead-Of-Time (AOT) native compilation for all methods. Delivers the fastest execution speed and instant app launch times at the cost of higher storage usage.
- **`speed-profile`**: Profile-guided compilation. Uses execution profiles (`.prof`) collected during app usage to compile only frequently executed ("hot") code paths, balancing peak performance with storage efficiency.
- **`everything`**: Compiles absolutely all DEX code and classes. Highly aggressive compilation mode that provides comprehensive native code execution at maximum storage usage.
- **`reset`**: Clears all compiled native artifacts (`.odex`, `.vdex`, `.art`) and resets the package to its initial unoptimized state.
- **`compact`**: Triggers ART heap compaction and background secondary DEX optimization to free active memory.

## Known Limitations

- **System Restrictions without Shizuku:** Without Shizuku authorization, Dexor operates in a read-only mode and cannot trigger compilation commands or query system-level package dump statuses.
- **OEM Variations:** Certain manufacturer skins (e.g., aggressive battery savers or vendor ART forks) may delay or throttle background `dex2oat` execution when battery levels are low or thermals are high.
- **Storage Trade-off:** Using `speed` or `everything` modes on large apps or games can significantly increase the storage space taken by compiled `.odex` files in `/data/dalvik-cache` or `/data/app`.

## Credits

- **[Shizuku](https://github.com/RikkaApps/Shizuku)** by [RikkaApps](https://github.com/RikkaApps) – Providing system-level API access without root.
- Built with **Google Gemini** and **Antigravity**.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
