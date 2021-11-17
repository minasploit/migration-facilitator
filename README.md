# .Net EF Migration Facilitator

[![Build](https://github.com/minasploit/migration-facilitator/workflows/Build/badge.svg)][plugingithubactions]
[![Version](https://img.shields.io/jetbrains/plugin/v/17026.svg?label=version)][pluginversions]
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17026.svg)][plugin]
[![Rating](https://img.shields.io/jetbrains/plugin/r/stars/17026)][reviews]
[![Install](https://img.shields.io/badge/install-.NET%20EF%20Migration%20Facilitator-green)][pluginembedableinstall]

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

[<img src="assets/get from marketplace.png"></img>][pluginembedableinstall]

- Using IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for ".Net EF Migration
  Facilitator"</kbd> >
  <kbd>Install Plugin</kbd>

- Manually:

  Download the [latest release](https://github.com/minasploit/migration-facilitator/releases/latest) and install it
  manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

---
Thank you for using the plugin. 💙 from Ethiopia 🇪🇹

[plugin]: https://plugins.jetbrains.com/plugin/17026
[pluginembedableinstall]: https://plugins.jetbrains.com/embeddable/install/17026
[pluginversions]: https://plugins.jetbrains.com/plugin/17026--net-ef-migration-facilitator/versions
[reviews]: https://plugins.jetbrains.com/plugin/17026--net-ef-migration-facilitator/reviews
[plugingithubactions]: https://github.com/minasploit/migration-facilitator/actions
