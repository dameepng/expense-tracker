# Prompt untuk Claude Fable 5.1 — Smart Receipt Scan (Expense Tracker)

## WAJIB: Workflow Kerja — Breakdown Ticket Dulu, Baru Eksekusi Per-Ticket

**Jangan langsung ngerjain semua requirement di bawah jadi 1 task besar.** Ikutin urutan ini:

1. **Step 0 — Breakdown**: Sebelum nulis kode apapun, pecah fitur ini jadi beberapa ticket/milestone kecil yang logis (misal: Ticket 1 = camera/gallery picker UI, Ticket 2 = image upload & vision AI call, Ticket 3 = response parsing & data model, Ticket 4 = preview/confirm screen, Ticket 5 = save ke Room + tests). Tulis breakdown ini ke file `TICKETS.md` (append kalau file udah ada dari fase sebelumnya, bikin section baru "## Fase: Receipt Scan"), format per ticket:
   ```
   ## Ticket N: <judul singkat>
   - Scope: <apa yang dikerjain>
   - Files touched: <estimasi file yang bakal ditambah/diubah>
   - Acceptance criteria: <gimana cara ngecek ticket ini beres>
   - Depends on: <ticket sebelumnya kalau ada>
   ```
2. **Tampilin breakdown itu ke gw dulu**, **STOP**, tunggu gw approve/koreksi urutan sebelum mulai eksekusi.
3. Setelah gw approve, kerjain **satu ticket per giliran**. Selesai satu ticket → **STOP, kasih summary** (file apa yang berubah, gimana cara gw test/review), **tunggu gw bilang lanjut** baru mulai ticket berikutnya.
4. Update status ticket di `TICKETS.md` (checklist done/in-progress) tiap kali ada ticket yang kelar.

## Konteks Project
Lanjutan dari fase NL Input dan Chat Assistant yang udah selesai — app expense tracker Android Kotlin + Jetpack Compose, udah ada AI client (Claude API), Repository/UseCase layer buat AI call, data model Transaction/Expense, dan Room DB.

Explore dulu codebase yang udah ada, terutama:
- AI client & config dari fase sebelumnya (reuse, jangan bikin instance baru terpisah)
- Data model Transaction/Expense yang udah final (field apa aja yang wajib ke-isi)
- Kategori list yang udah dipakai di fase NL Input (biar konsisten, jangan bikin kategori baru sembarangan)

Roadmap besar: NL Input (done) → Chat Assistant (done) → **Smart Receipt Scan (fase ini)** → AI Wrapped

## Goal Fase 3: Smart Receipt Scan
User foto struk belanja (dari kamera atau pilih dari galeri) → AI baca gambar → extract jadi transaksi terstruktur, sama kayak output NL Input:
```json
{
  "amount": 47500,
  "category": "Makanan & Minuman",
  "merchant": "Indomaret",
  "date": "2026-09-13",
  "note": "belanja bulanan",
  "items": ["Indomie 2pcs", "Teh Botol", "..."],
  "is_recurring": false
}
```

## Requirements
1. **UI**: entry point ambil foto struk — bisa dari FAB/menu yang sama kayak NL Input (kasih opsi: ketik teks / foto struk). Flow: buka kamera atau pilih dari galeri → preview gambar → tombol "Scan" → loading state → hasil parsing ditampilin di preview/confirm screen (reuse UI confirm dari fase NL Input kalau bisa, jangan bikin dari nol).
2. **Permission handling**: minta izin CAMERA runtime permission (Android 6+), handle kasus user reject izin dengan pesan yang jelas + tombol ke Settings.
3. **Image processing**: compress/resize gambar sebelum dikirim ke AI (biar gak boros bandwidth & biaya API) — target resolusi yang masih cukup jelas buat dibaca teks tapi gak terlalu besar file size-nya.
4. **AI Call**: kirim gambar (base64) + prompt yang strict suruh return JSON only, sama pattern kayak fase NL Input. List kategori yang tersedia harus dikirim juga ke prompt.
5. **Handling struk gak jelas/rusak**: kalau AI gak bisa baca struk dengan confidence tinggi (misal blur, kepotong, bukan struk sama sekali), kasih response yang jelasin itu ke user, jangan maksa return data ngasal — arahkan ke input manual/NL Input sebagai fallback.
6. **Multiple items di 1 struk**: struk biasanya ada banyak item, tapi transaksi expense tracker biasanya dicatat sebagai 1 entry per struk (total belanja) — kecuali lo mau breakdown per item jadi beberapa transaksi terpisah, itu keputusan lo, tapi kasih tau gw trade-off-nya di summary sebelum eksekusi ticket terkait.
7. **Error handling**: no internet, timeout, rate limit, image terlalu besar/gagal compress — semua harus ada state UI jelas.
8. **Arsitektur**: reuse Repository/UseCase pattern dari fase sebelumnya, extend dengan method baru buat handle image input, jangan bikin layer paralel yang beda pattern.

## Yang HARUS lo lakuin sebelum nulis kode
1. Cek ulang AI client, Repository, dan confirm/preview UI dari fase NL Input — reuse sebanyak mungkin.
2. Cek dependency yang udah ada buat image picker/camera (CameraX, Coil, dll) sebelum nambahin library baru — kalau belum ada, kasih tau opsi library yang mau dipakai dan alasannya sebelum eksekusi.
3. Kalau ada konflik sama konvensi codebase yang udah ada, ikutin konvensi yang ada dan kasih tau apa yang disesuaikan.

## Deliverable
- `TICKETS.md` section baru buat fase ini
- Composable UI buat pilih foto (kamera/galeri) + preview + confirm (reuse dari NL Input kalau memungkinkan)
- ViewModel + state (idle/loading/success/error) buat flow scan
- Repository/UseCase yang handle image compression + AI vision call + parsing
- Data class request/response yang type-safe
- Unit test minimal buat parsing logic (mock response AI dari gambar struk → pastiin ke-parse bener)

Setelah tiap ticket selesai, kasih summary singkat: file apa aja yang ditambah/diubah, dependency baru yang perlu gw approve, dan hal apa yang butuh gw putusin manual.
