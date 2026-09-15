# Receipt Scan

Receipt Scan memilih satu foto, mengirim image terkompresi bersama prompt dan kategori ke Claude, lalu menampilkan draft untuk dikoreksi. Internet diperlukan. Data gambar/base64 bersifat sementara di memori dan tidak disimpan sebagai history atau kolom Room.

Konfirmasi menyimpan satu transaksi Expense. Daftar item tidak menjadi transaksi terpisah; item diringkas ke note user secara deterministik dengan batas 1.000 karakter dan tanda `…` bila terpotong. Receipt yang blur, bukan receipt, total ambigu, kategori/tanggal tidak valid, timeout, offline, atau kegagalan image menggunakan jalur retry/fallback manual atau NL Input.

QA device (kamera, permission, rotasi, tema, keyboard, dan live Claude) harus dicatat hanya setelah benar-benar dijalankan.
