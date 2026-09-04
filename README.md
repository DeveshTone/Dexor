# Dexor

Dexor is a modern, minimal, and high-performance Android application management and ahead-of-time (AOT) compilation utility powered by the Android Runtime (`dex2oat`) and Shizuku. It provides an intuitive, fluid Jetpack Compose interface allowing users to inspect the live compilation status (compiler filters, status change reasons, installation sources) of user and system applications, run on-demand or batch DEX optimizations to boost app launch speed and runtime performance, and reclaim storage without needing root access.

## Screenshots

<p align="center">
  <img src="screenshots/app_list.png" width="31%" alt="App List" />
  <img src="screenshots/app_detail.png" width="31%" alt="App Inspection" />
  <img src="screenshots/compilation_modes.png" width="31%" alt="Compilation Modes" />
</p>

## Requirements

- **Android Version:** Android 9.0 to Android 15 (API levels 28 – 35).
- **Shizuku:** A working [Shizuku](https://github.com/RikkaApps/Shizuku) environment (running via Wireless Debugging, ADB, or Root) is required to execute elevated package compilation commands without root.

## Compilation Modes

Dexor interfaces directly with Android's `cmd package compile` system commands. The available compilation filters in the app are:

- **`verify`**: Completely clears all native code binaries. Frees up maximum storage space. Reverts the app to the raw JIT interpreter (~5s per app).
- **`space`**: Compiles only core initialization methods. Optimizes for the lowest possible storage footprint while ensuring stable app boot times (~15s per app).
- **`speed`**: Force-compiles all accessible app methods into native machine code. Maximizes runtime smoothness and eliminates micro-stutters (ideal for gaming) (~30s per app).
- **`everything`**: Compiles all code and resolves all class architecture references. Provides maximum possible deep optimization but uses the most storage space (~60s per app).

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
