# Migration Facilitator

![Build](https://github.com/minasploit/migration-facilitator/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/17026-migration-facilitator.svg)](https://plugins.jetbrains.com/plugin/17026-migration-facilitator)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17026-migration-facilitator.svg)](https://plugins.jetbrains.com/plugin/17026-migration-facilitator)

Manages the addition and removal of EF Core migrations as well as database updates.

Find <kbd>Migrations</kbd> on the Main Menu, which gives access to <kbd>Add Migration</kbd>, <kbd>Remove Migration</kbd>
and <kbd>Update Database</kbd>

---

<h2>Features</h2>

<h3>Add Migration</h3>

- Under <kbd>Migrations</kbd>, click <kbd>Add Migration</kbd>
- Set the correct <b>startup</b> and <b>data</b> project
- Set the migration name and click `Add Migration`

<h3>Remove Migration</h3>

- Under <kbd>Migrations</kbd>, click <kbd>Remove Migration</kbd>
- Set the correct <b>startup</b> and <b>data</b> project
- Click `Remove Migration` and confirm

<h3>Update Database</h3>

- Under <kbd>Migrations</kbd>, click <kbd>Update Database</kbd>
- Wait for the migrations to be loaded...
- If an error occurs when migrations are loaded, make sure you set the correct <b>startup</b> and <b>data</b> project,
  and use the `Refresh Migration` and wait for the migrations to be loaded
- Select the desired migration to updates the database to and click `Update Database`

---

## Installation

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Migration
  Facilitator"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/minasploit/migration-facilitator/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
