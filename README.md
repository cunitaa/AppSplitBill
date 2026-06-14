# AppSplitBill - Dokumentasi Project

Aplikasi ini adalah solusi untuk membagi tagihan (split bill) dengan mudah menggunakan fitur manual maupun otomatis (OCR).

## 15+ Fitur Utama
1. **Splash Screen**: Branding awal aplikasi.
2. **Login System**: Autentikasi pengguna.
3. **Register System**: Pendaftaran pengguna baru.
4. **Dashboard**: Ringkasan aktivitas dan menu utama.
5. **Quick Split**: Bagi rata tagihan secara cepat tanpa rincian item.
6. **Itemized Split**: Bagi tagihan berdasarkan item yang dipesan masing-masing.
7. **Scan Struk (OCR)**: Ekstraksi teks dari foto struk menggunakan ML Kit.
8. **Manajemen Teman**: Tambah dan hapus teman dari daftar kontak aplikasi.
9. **Selection People**: Pilih teman yang terlibat dalam transaksi tertentu.
10. **Item Assignment**: Memetakan "siapa makan apa" pada fitur Itemized Split.
11. **Kalkulasi Pajak & Service**: Penambahan biaya pajak secara otomatis (persentase).
12. **Kalkulasi Diskon**: Pengurangan total tagihan dengan nominal diskon.
13. **Bill Summary**: Tampilan rincian akhir sebelum disimpan/dibagikan.
14. **Share to WhatsApp**: Mengirim rincian tagihan dan info bank ke teman via WA.
15. **Riwayat (History)**: Menyimpan semua transaksi split bill sebelumnya.
16. **Profil Pengguna**: Pengaturan nama, nomor WhatsApp, dan informasi rekening bank.

## Proses Bisnis
1. User masuk ke aplikasi (Login/Register).
2. User memilih metode pembagian (Quick atau Itemized).
3. User menginput data (Manual atau Scan Struk).
4. User memilih teman yang ikut patungan.
5. Aplikasi menghitung pembagian secara proporsional termasuk pajak/diskon.
6. User membagikan rincian hasil hitungan ke WhatsApp teman-temannya.
7. Data tersimpan di Riwayat.

## Struktur Kode
- `com.example.appsplitbill.ui`: Berisi Fragment Dashboard, History, dan Profile.
- `com.example.appsplitbill.model`: Model data User, Bill, dan BillItem.
- `com.example.appsplitbill.utils`: Utility untuk Currency Formatter dan Storage.
- `res/layout`: Berisi 15+ layout XML untuk setiap tampilan.

## Cara Menjalankan Demo
Klik tombol **"🚀 RUN DEMO"** di halaman Login untuk memuat data simulasi secara otomatis.
