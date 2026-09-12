# Prompt untuk Claude Fable 5.1 — AI Chat Assistant (Expense Tracker)

## WAJIB: Workflow Kerja — Breakdown Ticket Dulu, Baru Eksekusi Per-Ticket

**Jangan langsung ngerjain semua requirement di bawah jadi 1 task besar.** Ikutin urutan ini:

1. **Step 0 — Breakdown**: Sebelum nulis kode apapun, pecah fitur ini jadi beberapa ticket/milestone kecil yang logis (misal: Ticket 1 = data model + context aggregator, Ticket 2 = Repository/UseCase + AI call, Ticket 3 = ViewModel + state, Ticket 4 = UI Chat screen, Ticket 5 = tests). Tulis breakdown ini ke file `TICKETS.md` di root project (atau folder docs kalau ada), format per ticket:
   ```
   ## Ticket N: <judul singkat>
   - Scope: <apa yang dikerjain>
   - Files touched: <estimasi file yang bakal ditambah/diubah>
   - Acceptance criteria: <gimana cara ngecek ticket ini beres>
   - Depends on: <ticket sebelumnya kalau ada>
   ```
2. **Tampilin breakdown itu ke gw dulu** (isi `TICKETS.md`-nya), **STOP**, tunggu gw approve/koreksi urutan sebelum mulai eksekusi.
3. Setelah gw approve, kerjain **satu ticket per giliran**. Selesai satu ticket → **STOP, kasih summary** (file apa yang berubah, gimana cara gw test/review), **tunggu gw bilang lanjut** baru mulai ticket berikutnya.
4. Update status ticket di `TICKETS.md` (checklist done/in-progress) tiap kali ada ticket yang kelar.

Alasan: fase NL Input kemarin kelamaan karena dikerjain sebagai 1 task besar tanpa checkpoint review. Sekarang gw mau bisa review & kasih feedback tiap potongan kecil, bukan nunggu semuanya jadi baru ketauan salah arah.

## Konteks Project
Lanjutan dari fase 1 (Natural Language Input) yang udah selesai — app expense tracker Android Kotlin + Jetpack Compose, udah ada AI client (Claude API), Repository/UseCase layer buat AI call, dan data transaksi kesimpen di Room.

Explore dulu codebase yang udah ada, terutama:
- Repository/UseCase AI call yang udah dibikin di fase 1 (reuse, jangan duplikat)
- Struktur data Transaction/Expense yang udah final
- Pattern error handling & loading state yang udah dipakai, biar konsisten

Roadmap besar: NL Input (done) → **Chat Assistant (fase ini)** → Smart Receipt Scan → AI Wrapped

## Goal Fase 2: Chat Assistant
User bisa tanya bebas soal data pengeluaran mereka lewat chat interface, contoh:
- "berapa abis gw buat makan bulan ini?"
- "kategori apa yang paling boros minggu ini?"
- "bandingin pengeluaran gw bulan ini sama bulan lalu"
- "ada transaksi aneh gak akhir-akhir ini?"

AI harus jawab berdasar data transaksi REAL milik user, bukan ngarang.

## Requirements
1. **UI**: chat screen baru (list bubble chat + input field di bawah), bisa diakses dari bottom nav atau FAB. Loading indicator pas AI lagi mikir/nunggu response.
2. **Context/Data Feeding ke AI**: karena kirim SELURUH transaksi tiap kali chat gak efisien & boros token, tentuin strategi:
   - Opsi A (simpel dulu): pre-agregasi data di app (total per kategori, per bulan, dsb) sebelum dikirim ke AI sebagai context — bukan raw list transaksi
   - Opsi B (lebih advanced, kalau mau): tool use / function calling, AI bisa "minta" query spesifik (misal `getTransactionsByCategory`, `getTotalByMonth`) dan app yang eksekusi query-nya, baru hasilnya dikirim balik ke AI

   Pilih Opsi A dulu buat MVP fase ini biar cepet kelar, tapi arsitekturnya bikin gampang upgrade ke Opsi B nanti (jangan hardcode context building di satu tempat yang susah di-extend).
3. **Percakapan multi-turn**: simpan history chat (minimal in-memory per session, gak perlu persist ke DB dulu kecuali lo mau), biar AI ngerti konteks pertanyaan lanjutan ("gimana kalau dibanding bulan lalu?" harus nyambung ke pertanyaan sebelumnya).
4. **Privacy/scope guard**: system prompt AI harus dibatasi cuma jawab soal data keuangan user, jangan jawab pertanyaan di luar topik (biar gak disalahgunakan jadi general chatbot gratis lewat app lo).
5. **Error handling**: sama kayak fase 1 — no internet, timeout, rate limit, semua harus ada state UI jelas.
6. **Arsitektur**: pisahin ChatViewModel + ChatRepository/UseCase yang manggil AI client yang sama dari fase 1 (reuse instance/config-nya, jangan bikin AI client baru terpisah).

## Yang HARUS lo lakuin sebelum nulis kode
1. Cek ulang AI client & Repository dari fase 1 — reuse strukturnya, extend kalau perlu, jangan bikin paralel yang beda pattern.
2. Kalau format kategori/struktur agregasi data belum ada helper-nya, bikin dulu (misal `TransactionAggregator` atau semacamnya) biar reusable juga buat fase AI Wrapped nanti.
3. Kalau ada konflik sama konvensi codebase yang udah ada, ikutin konvensi yang ada dan kasih tau apa yang disesuaikan.

## Deliverable
- Chat screen Composable (bubble UI, input bar, loading state)
- ChatViewModel + state (messages, loading, error)
- ChatRepository/UseCase yang build context dari data transaksi + call AI
- Data class buat ChatMessage (role: user/assistant, content, timestamp)
- System prompt yang scope-nya dibatasi ke topik keuangan aja
- Unit test minimal buat context-building logic (pastiin agregasi data yang dikirim ke AI itu akurat)

Setelah selesai, kasih summary: file apa aja yang ditambah/diubah, dan flag kalau ada bagian yang butuh gw putusin sendiri (misal soal persist chat history atau enggak).
