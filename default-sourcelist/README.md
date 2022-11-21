# Aion / default-sourcelist

This is the default sourcelist for Aion. The following packages are provided here:

- -
  - `java-temurin-jre` (19, 18, 17, 16, 11, 8) [X86_64, X86, AARCH64, ARM]
- [FFMpeg](https://ffmpeg.org)
  - `ffmpeg/ffprobe/ffplay` (5) [X86_64, X86, AARCH64, ARM]

| Source                                   | Package Name       | Aliases             | Major Versions        | Supported Systems                                 |
| ---------------------------------------- | ------------------ | ------------------- | --------------------- | ------------------------------------------------- |
| [Adoptium Temurin](https://adoptium.net) | `java-temurin-jdk` | `jdk`               | 19, 18, 17, 16, 11, 8 | Windows, macOS, Linux (X86_64, X86, AARCH64, ARM) |
| [Adoptium Temurin](https://adoptium.net) | `java-temurin-jre` | `java`              | 19, 18, 17, 16, 11, 8 | Windows, macOS, Linux (X86_64, X86, AARCH64, ARM) |
| [FFmpeg](https://ffmpeg.org)             | `ffmpeg`           | `ffplay`, `ffprobe` | 5                     | Windows (X86_64)                                  |

## Updating

If you wish to add a package then you may open an issue to request a package, or if you are the developer you can add the package yourself via PR.

If you have your own sourcelist that contains your packages then we will link it in the main sourcelist IF your server is reliable. Otherwise, we'd recommend just adding the sourcelist to this repository to help ensure reliablilty/speed.

**Note:** We have a generator script for some packages. You can add your own generator (either in NodeJS or Shell) and call it from the `generate.sh` file. (Make sure that your code properly accounts for the cwd!)
