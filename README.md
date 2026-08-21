# RiFF Deck

A local, offline Android music player with a rock/metal/Y2K **skeuomorphic** UI — brushed steel panels, chrome transport controls, riveted corners, LCD-style readouts, and a spinning vinyl Now Playing screen.

Built on top of the [Booming Music](https://github.com/mardous/BoomingMusic) codebase (GPL-3.0) for its playback engine, lyrics system, tag editor, and library management — with a completely original visual design layered on top. See [NOTICE.md](NOTICE.md) for attribution.

## Status

**This is a work in progress.** The functional core (playback, scanning, tagging, lyrics, database, widgets, equalizer, scrobbling) is intact and should work. The visual reskin is being done screen-by-screen; not every screen has been reskinned yet. Check `NEXT_STEPS.md` (if present) or open issues for current progress.

## Design direction

- **Not Material Design.** Real depth: brushed-metal gradients, beveled panels, screw/rivet details, chrome buttons with pressed states, LCD-style monospace readouts.
- **Album art forward.** Now Playing centers on a spinning vinyl-style record with the album cover as the label.
- **Dark only.** The rock/metal/Y2K hardware aesthetic (amps, car stereos, MP3 players) doesn't have a "light mode."

## Features (inherited from the underlying playback engine)

- Offline local playback via Media3/ExoPlayer, gapless, crossfade, ReplayGain, audio offload
- Automatic lyric download + in-app lyric editor (LRC, TTML, word-by-word sync)
- Full tag editor with instant library updates
- Smart auto playlists (Recently Played, Most Played, History)
- Built-in equalizer with import/export profiles
- Native ListenBrainz and Last.fm scrobbling
- Widgets, Android Auto, sleep timer
- No ads, no tracking, no account required

## Building

Standard Gradle/Android Studio project.

```
./gradlew assemble
```

GitHub Actions (`.github/workflows/android.yml`) builds and lints on every push to `main`, so you can push from a phone (e.g. via Termux) and let CI build the APK without needing a local Android SDK.

## License

GPL-3.0, inherited from the upstream project. See [LICENSE.txt](LICENSE.txt) and [NOTICE.md](NOTICE.md).
