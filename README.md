# InkWidgets

Dua widget layar utama Android bergaya e-ink, tanpa internet, tanpa iklan, tanpa library pihak ketiga.

| Widget | Ukuran default | Fungsi |
|---|---|---|
| **Jam Dot-Matrix** | 4x2, bisa diubah horizontal dan vertikal | Jam 24 jam `HH:mm` dari grid titik 5x7 ditambah tanggal (mis. `SEL 3 NOV`). Ketuk untuk membuka aplikasi Jam. |
| **Pencarian Google** | 4x1, bisa diubah horizontal | Kapsul berisi huruf **G**. Ketuk bilah untuk membuka Google Search, ketuk ikon kamera untuk membuka Google Lens. |

Palet: Ink `#111111`, Paper `#F4F1EA`, Muted `#5A5A5A`, titik mati Ink 12%, latar transparan.

- Package: `com.taro.inkwidgets`
- minSdk 26, targetSdk/compileSdk 35
- Gradle 8.14.3, AGP 8.13.0, Kotlin 2.2.20, JDK 17

---

## 1. Push ke GitHub

APK di-build oleh GitHub Actions (`.github/workflows/build.yml`), jadi tidak perlu Android SDK di laptop.

```bash
git init
git add .
git update-index --chmod=+x gradlew
git commit -m "Initial commit: InkWidgets"
git branch -M main
git remote add origin https://github.com/<user>/<repo>.git
git push -u origin main
```

Kalau push ditolak karena autentikasi, login dulu dengan GitHub CLI:

```bash
gh auth login        # pilih GitHub.com -> HTTPS -> Login with a web browser
gh auth setup-git    # agar git memakai kredensial gh
git push -u origin main
```

## 2. Mengunduh APK dari tab Actions

1. Buka repo di GitHub, lalu tab **Actions**.
2. Pilih run terbaru **Build APK** (otomatis jalan setiap push ke `main`; bisa juga dijalankan manual lewat tombol **Run workflow**).
3. Tunggu sampai centangnya hijau (sekitar 3–6 menit).
4. Di bagian **Artifacts** paling bawah, klik **InkWidgets-apk** untuk mengunduh file ZIP.
5. Ekstrak ZIP tersebut. Isinya `app-debug.apk`.

> Unduhan artifact hanya bisa dilakukan saat login ke GitHub. Artifact disimpan 90 hari.

## 3. Install di HP (Redmi 10 2022 / MIUI 14)

**Cara A: langsung di HP**
1. Kirim `app-debug.apk` ke HP (Telegram/Drive/kabel USB), atau unduh artifact langsung dari browser HP.
2. Buka file APK dengan **File Manager**.
3. Jika muncul peringatan, ketuk **Setelan**, lalu aktifkan **Izinkan dari sumber ini** (Install dari sumber tidak dikenal) untuk aplikasi yang membuka APK.
4. Kembali, lalu ketuk **Pasang**. Jika MIUI menampilkan pemindaian keamanan, tunggu selesai dan pilih **Tetap pasang**.

**Cara B: lewat adb (opsional)**
```bash
adb install -r app-debug.apk
```

## 4. Menambahkan widget di MIUI

1. Di layar utama, **tekan lama area kosong** (atau cubit layar).
2. Ketuk **Widget**.
3. Gulir/cari **InkWidgets**.
4. Tekan lama **Jam Dot-Matrix** atau **Pencarian Google**, lalu seret ke layar utama.
5. Untuk mengubah ukuran: tekan lama widget, lalu tarik bingkai pinggirnya. Jam selalu tetap proporsional dan berada di tengah.

## 5. Mulai otomatis + penghemat baterai "Tanpa batasan" (penting untuk jam)

MIUI suka menghentikan aplikasi di latar belakang sehingga alarm per menit berhenti, dan jam jadi "macet". Lakukan sekali saja:

1. Buka aplikasi **InkWidgets**, lalu ketuk **Buka pengaturan aplikasi**.
   (Atau: Setelan → Aplikasi → Kelola aplikasi → InkWidgets.)
2. Aktifkan **Mulai otomatis** (Autostart).
3. Ketuk **Penghemat baterai**, lalu pilih **Tanpa batasan** (No restrictions).
4. Opsional: buka menu Terbaru (recent apps), tekan lama kartu InkWidgets, lalu ketuk ikon **gembok** agar tidak ikut dibersihkan.

Catatan teknis: jam memakai alarm `RTC` *exact* (izin `USE_EXACT_ALARM`) yang dijadwalkan ulang ke awal menit berikutnya. Alarm ini tidak membangunkan HP saat layar mati. Begitu layar menyala, jam langsung diperbarui. Jika exact alarm tidak diizinkan, aplikasi memakai `setWindow` dengan jendela 1 detik. Membuka aplikasi InkWidgets juga menjadwalkan ulang alarm.

---

## Build lokal (opsional, butuh Android SDK + JDK 17)

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Di Android Studio: **File → Open**, pilih folder proyek, tunggu sinkronisasi Gradle, lalu **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.

## Struktur kode

```
app/src/main/java/com/taro/inkwidgets/
├── DotFont.kt                  # pola titik 5x7 untuk digit 0–9
├── ClockRenderer.kt            # menggambar jam + tanggal ke Bitmap (Canvas)
├── ClockWidgetProvider.kt      # widget jam, alarm per menit, broadcast waktu/boot
├── SearchWidgetProvider.kt     # widget pencarian Google + Lens
├── LaunchTrampolineActivity.kt # activity transparan: coba intent satu per satu
└── MainActivity.kt             # petunjuk + tombol pengaturan aplikasi
```
