# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v1.2.0-1.16.5] - 2021-11-09
### Added
- Added new world selection screen for editing server configs directly from the main menu when no world is loaded yet

## [v1.1.1-1.16.5] - 2021-11-07
### Changed
- Configs are now reloaded immediately after being edited
### Fixed
- Fixed vanilla servers showing as incompatible when they aren't
- Fixed server config reloading event not being fired for the player that changed the config

## [v1.1-1.16.5] - 2021-11-05
### Added
- Added filter button to config screen, supports: all, edited, resettable
- Added filter button to search screen, in addition to config screen options supports: entries only, categories only
### Fixed
- Fixed server configs reloading on a dedicated server throwing an exception

## [v1.0.1-1.16.5] - 2021-11-05
### Changed
- Better compatibility for button rendering with resource packs
### Fixed
- Fixed an issue where buttons would be wrongly colored in the list editing screen

## [v1.0-1.16.5] - 2021-11-04
- Initial release

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
[Puzzles Lib]: https://www.curseforge.com/minecraft/mc-mods/puzzles-lib
