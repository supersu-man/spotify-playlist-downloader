<p align="center">
  <img src="githubdocs/logo.png" alt="Logo">
</p>

<h1 align="center">Spotify Playlist Downloader</h1>

<p align="center">
  <img src="https://img.shields.io/github/v/release/supersu-man/spotify-playlist-downloader?style=for-the-badge&color=1DB954" alt="Release">
  <img src="https://img.shields.io/github/license/supersu-man/spotify-playlist-downloader?style=for-the-badge" alt="License">
  <img src="https://img.shields.io/github/stars/supersu-man/spotify-playlist-downloader?style=for-the-badge" alt="Stars">
</p>

<p align="center">
  <strong>A straightforward Android application to bridge your Spotify playlists to your local storage.</strong><br>
  Fetch metadata from Spotify, find high-quality audio on YouTube Music, and download with full metadata and album art.
</p>

---

## ✨ Features

- 🎵 **Lossless Mapping**: Seamlessly converts Spotify playlist tracks into high-quality audio files.
- 🖼️ **Full Metadata**: Automatically embeds **Artist**, **Album**, **Title**, and **High-Res Cover Art** into every file.
- ⚡ **FFmpeg Powered**: High-speed remuxing and optional MP3 conversion.
- 📁 **Custom Storage**: Choose exactly where you want your music saved.
- 🌓 **Modern UI**: Built with Jetpack Compose for a smooth, native Android experience.
- 🔗 **Smart Fetching**: Uses the powerful NewPipe Extractor for reliable YouTube Music sourcing.

## 🚀 How it Works

1. **Link**: Paste any public Spotify playlist URL.
2. **Fetch**: The app uses the Spotify Web API to get track details and thumbnails.
3. **Match**: Searches YouTube Music for the best matching audio stream (M4A/MP3).
4. **Tag**: FFmpeg injects ID3v2.3 tags and album art into the file headers.
5. **Save**: Files are organized and saved directly to your device.

## 📥 Installation

1. Head over to the [**Releases**](https://github.com/supersu-man/spotify-playlist-downloader/releases) page.
2. Download the latest `.apk` file.
3. Install it on your Android device (ensure "Install from Unknown Sources" is enabled).
4. Launch the app and grant the necessary Storage permissions.

## 🛠️ Tech Stack

| Component | Library/Service |
| :--- | :--- |
| **Language** | Kotlin / Jetpack Compose |
| **Extraction** | [NewPipe Extractor](https://github.com/TeamNewPipe/NewPipeExtractor) |
| **Metadata** | [Spotify Web API Java](https://github.com/thelinmichael/spotify-web-api-java) |
| **Processing** | [FFmpeg Kit](https://github.com/arthenica/ffmpeg-kit) |
| **Networking** | [OkHttp 4](https://square.github.io/okhttp/) |

## 🤝 Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

