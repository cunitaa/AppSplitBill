# 📱 SplitBill - Aplikasi Pecah Tagihan Profesional

**SplitBill** adalah aplikasi Android modern yang dirancang untuk memudahkan pembagian tagihan makan atau belanja bersama teman secara adil, cepat, dan transparan. Dilengkapi dengan teknologi AI OCR dan antarmuka premium ala Instagram.

---

## 🚀 15 Fitur Utama (Lengkap & Aktif)

Aplikasi ini telah dioptimalkan dengan 15 fitur cerdas untuk memenuhi kebutuhan nyata:

1.  **Quick Split**: Pembagian tagihan instan dengan satu kali input nominal.
2.  **Itemized Split**: Fitur detail "Siapa Makan Apa" untuk pembagian yang lebih akurat per menu.
3.  **OCR Smart Scanner**: Memindai struk fisik secara otomatis menggunakan Google ML Kit AI.
4.  **Friends Management**: Kelola daftar teman lengkap dengan foto profil masing-masing.
5.  **Group System (Circle)**: Buat grup (misal: Geng Kantor) untuk memilih banyak orang sekaligus.
6.  **Sistem Autentikasi**: Login dan Register untuk keamanan data profil dan riwayat Anda.
7.  **Proporsional Tax Calculation**: Pajak dihitung secara adil berdasarkan porsi harga makanan tiap orang.
8.  **Service Charge Support**: Input biaya layanan terpisah yang sering ada di restoran modern.
9.  **Settlement Tracking**: Tandai siapa yang sudah bayar (✅) dan belum (⏳) di halaman riwayat.
10. **Dashboard Summary**: Ringkasan piutang (uang yang akan diterima) langsung di layar utama.
11. **Instagram Dark Mode**: Tema gelap pekat (Deep Black #000000) yang elegan dan nyaman di mata.
12. **Multi-Profile Image**: Fitur ganti foto profil untuk akun pribadi maupun profil teman di daftar kontak.
13. **Share to WhatsApp**: Mengirim rincian tagihan sangat detail (Nama -> Item -> Pajak) ke grup WhatsApp.
14. **Detail Payment Info**: Dropdown pemilihan Bank/E-Wallet (BCA, OVO, DANA, dll) yang terintegrasi di profil.
15. **Rounding Logic**: Pembulatan otomatis ke Rp 500 terdekat untuk memudahkan proses transfer/pembayaran.

---

## 🎨 UI/UX Highlights

- **Consistent Design**: Semua tombol menggunakan gaya *Material 3* membulat (12dp) yang seragam.
- **Smart Form**: Input rekening dan foto profil diproteksi dengan "Mode Edit" agar tidak mudah berubah tidak sengaja.
- **Responsive Layout**: Konten halaman profil telah dioptimalkan agar pas dalam satu layar tanpa tertutup navigasi bawah.

---

## 🛠️ Teknologi yang Digunakan

- **Bahasa**: Java / Kotlin (Android Studio)
- **AI/ML**: Google ML Kit (Text Recognition)
- **UI Components**: Google Material Design 3
- **Database**: SharedPreferences (Local Storage) with GSON Serialization
- **Architecture**: Fragment-based Navigation Graph

---

## 📸 Cara Menggunakan OCR

1.  Masuk ke menu **Scan Struk**.
2.  Arahkan kamera ke struk belanja Anda (fokuskan pada daftar harga).
3.  Sistem akan mendeteksi teks secara real-time.
4.  Tekan **"GUNAKAN HASIL"** untuk memindahkan data otomatis ke daftar pesanan.

---

## 📥 Instalasi

1. Clone repositori ini:
   ```bash
   git clone https://github.com/cunitaa/AppSplitBill.git
   ```
2. Buka project di **Android Studio**.
3. Pastikan SDK 34 sudah terinstall.
4. Klik **Run** ke Emulator atau HP Fisik.

---

**Developed with ❤️ for better splitting experience.**
