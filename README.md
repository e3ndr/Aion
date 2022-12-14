# Aion

<p align="center">
	The most-bestest package manager.
	<br />
	<small>
		Windows & *nix
	</small>
</p>

<p align="center">
	<a href="./LICENSE.md"><img src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
</p>

Get started over at the [Wiki](https://github.com/e3ndr/Aion/wiki).

## TODO List

- [ ] Packages

  - [x] Listing all packages installed.
  - [x] Installing a package and it's dependencies.
  - [x] Removing a package.
  - [ ] Pruning any orphaned dependencies. (A dependency on the path will NOT be considered orphaned)
  - [x] Updating all packages by parsing and comparing the `patch` version.
  - [x] Archive decompression support.

- [x] Path

  - [x] Listing all commands provided by a package.
  - [x] Listing the commands and their packages.
  - [x] Updating which package controls what command.
  - [x] A path rebuild command.
  - [x] The RUN command.
  - [ ] Adding a package's dependencies to it's path in the launch command.

- [x] Sources

  - [x] Listing all sources configured.
  - [x] Adding a new source.
  - [x] Removing a source.
  - [x] Refreshing the local source cache, similar to `apt update`.
