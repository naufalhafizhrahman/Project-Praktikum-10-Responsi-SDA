import java.util.*;

class Song {
    private String title;
    private int durationSeconds;

    public Song(String title, int durationSeconds) {
        this.title = title;
        this.durationSeconds = durationSeconds;
    }

    public String getTitle() {
        return title;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    @Override
    public String toString() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%s (%02d:%02d)", title, minutes, seconds);
    }
}

class CatalogNode {
    private String name;
    private String type; // "ROOT", "GENRE", "ARTIST", "ALBUM"
    private Map<String, CatalogNode> children; // Menggunakan Map untuk pencarian O(1) berdasarkan nama
    private List<Song> songs; // Hanya digunakan jika type adalah "ALBUM"

    public CatalogNode(String name, String type) {
        this.name = name;
        this.type = type;
        this.children = new LinkedHashMap<>(); // Menjaga urutan input data
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<String, CatalogNode> getChildren() {
        return children;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void addChild(CatalogNode child) {
        this.children.put(child.getName(), child);
    }

    public void addSong(Song song) {
        this.songs.add(song);
    }
}

/**
 * Sistem Utama Pemutar Musik
 */
public class MusicPlayerApp {
    private CatalogNode catalogRoot;
    private Deque<Song> playQueue;    // FIFO Queue (Menggunakan ArrayDeque untuk efisiensi)
    private Stack<Song> playHistory;  // LIFO Stack
    private Song currentlyPlaying;

    public MusicPlayerApp() {
        // Inisialisasi struktur data dasar
        catalogRoot = new CatalogNode("Music Library", "ROOT");
        playQueue = new ArrayDeque<>();
        playHistory = new Stack<>();
        currentlyPlaying = null;

        // Seeding data awal katalog musik
        seedCatalogData();
    }

    /**
     * Mengisi data bawaan (seeding) ke dalam katalog musik hierarkis.
     * Analisis Kompleksitas Waktu: O(1) karena jumlah data konstan.
     */
    private void seedCatalogData() {
        // Genre Pop
        CatalogNode pop = new CatalogNode("Pop", "GENRE");
        CatalogNode tulus = new CatalogNode("Tulus", "ARTIST");
        CatalogNode albumManusia = new CatalogNode("Manusia", "ALBUM");
        albumManusia.addSong(new Song("Hati-Hati di Jalan", 242));
        albumManusia.addSong(new Song("Diri", 242));
        tulus.addChild(albumManusia);
        pop.addChild(tulus);

        // Genre Rock
        CatalogNode rock = new CatalogNode("Rock", "GENRE");
        CatalogNode dewa19 = new CatalogNode("Dewa 19", "ARTIST");
        CatalogNode albumBintangLima = new CatalogNode("Bintang Lima", "ALBUM");
        albumBintangLima.addSong(new Song("Roman Picisan", 247));
        albumBintangLima.addSong(new Song("Dua Sejoli", 274));
        dewa19.addChild(albumBintangLima);
        rock.addChild(dewa19);

        // Masukkan ke root
        catalogRoot.addChild(pop);
        catalogRoot.addChild(rock);
    }

    /**
     * Menampilkan katalog secara hierarkis (Genre -> Artist -> Album -> Song).
     * Menerapkan konsep Preorder Traversal pada General Tree.
     * Analisis Kompleksitas Waktu: O(N), di mana N adalah total seluruh elemen/node di dalam pohon.
     */
    public void displayCatalog(CatalogNode node, String indent) {
        if (!node.getType().equals("ROOT")) {
            System.out.println(indent + "└── [" + node.getType() + "] " + node.getName());
        }
        
        // Cetak lagu jika node saat ini adalah ALBUM
        if (node.getType().equals("ALBUM")) {
            for (Song song : node.getSongs()) {
                System.out.println(indent + "    ├── 🎵 " + song);
            }
        }

        // Rekursif ke anak-anak node
        for (CatalogNode child : node.getChildren().values()) {
            displayCatalog(child, indent + "    ");
        }
    }

    /**
     * Mencari objek lagu tertentu di dalam hirarki katalog.
     * Analisis Kompleksitas Waktu: O(N) traversal terburuk untuk menemukan judul lagu yang sama.
     */
    public Song findSongInCatalog(CatalogNode node, String title) {
        if (node.getType().equals("ALBUM")) {
            for (Song s : node.getSongs()) {
                if (s.getTitle().equalsIgnoreCase(title)) {
                    return s;
                }
            }
        }
        for (CatalogNode child : node.getChildren().values()) {
            Song found = findSongInCatalog(child, title);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Menambahkan lagu ke dalam Antrean Putar (Enqueue).
     * Menerapkan Prinsip: FIFO (First In, First Out).
     * Analisis Kompleksitas Waktu: O(1) karena operasi addLast pada Deque/Queue memakan waktu konstan.
     */
    public void enqueueSong(String title) {
        Song song = findSongInCatalog(catalogRoot, title);
        if (song != null) {
            playQueue.addLast(song);
            System.out.println("[SUKSES] '" + song.getTitle() + "' ditambahkan ke antrean.");
        } else {
            System.out.println("[ERROR] Lagu '" + title + "' tidak ditemukan di katalog.");
        }
    }

    /**
     * Memutar lagu berikutnya dari antrean (Dequeue & Play).
     * Menerapkan Prinsip: FIFO untuk mengambil lagu, LIFO untuk menyimpan ke riwayat.
     * Analisis Kompleksitas Waktu: O(1) karena operasi pollFirst pada Deque dan push pada Stack adalah O(1).
     */
    public void playNext() {
        if (playQueue.isEmpty()) {
            System.out.println("[INFO] Antrean kosong. Tidak ada lagu untuk diputar berikutnya.");
            return;
        }

        // Jika ada lagu yang sedang diputar saat ini, masukkan ke riwayat sebelum ganti lagu
        if (currentlyPlaying != null) {
            playHistory.push(currentlyPlaying);
        }

        currentlyPlaying = playQueue.pollFirst();
        System.out.println("▶️ SEKARANG MEMUTAR: " + currentlyPlaying);
    }

    /**
     * Fitur Mundur / Kembali ke lagu sebelumnya (Undo / Back).
     * Menerapkan Prinsip: LIFO (Last In, First Out).
     * Analisis Kompleksitas Waktu: O(1) karena operasi pop pada Stack dan addFirst pada Deque adalah O(1).
     */
    public void undoBack() {
        if (playHistory.isEmpty()) {
            System.out.println("[INFO] Tidak ada riwayat lagu sebelumnya (Tidak bisa Undo/Back).");
            return;
        }

        // Kembalikan lagu saat ini ke barisan depan antrean jika ada
        if (currentlyPlaying != null) {
            playQueue.addFirst(currentlyPlaying);
        }

        // Ambil lagu terakhir dari tumpukan riwayat
        currentlyPlaying = playHistory.pop();
        System.out.println("UNDO BERHASIL. Kembali memutar: " + currentlyPlaying);
    }

    /**
     * Menampilkan daftar antrean lagu saat ini.
     * Analisis Kompleksitas Waktu: O(Q) di mana Q adalah jumlah elemen di dalam antrean.
     */
    public void displayQueue() {
        System.out.println("\n=== ANTRIAN LAGU (NEXT IN QUEUE) ===");
        if (playQueue.isEmpty()) {
            System.out.println("[Kosong]");
            return;
        }
        int index = 1;
        for (Song song : playQueue) {
            System.out.println(index + ". " + song);
            index++;
        }
    }

    /**
     * Menampilkan riwayat pemutaran lagu.
     * Analisis Kompleksitas Waktu: O(H) di mana H adalah jumlah elemen di dalam stack riwayat.
     */
    public void displayHistory() {
        System.out.println("\n=== RIWAYAT PEMUTARAN (HISTORY - LIFO) ===");
        if (playHistory.isEmpty()) {
            System.out.println("[Kosong]");
            return;
        }
        // Menampilkan dari yang paling baru diputar (top of stack)
        for (int i = playHistory.size() - 1; i >= 0; i--) {
            System.out.println("• " + playHistory.get(i));
        }
    }

    public CatalogNode getCatalogRoot() {
        return catalogRoot;
    }

    public Song getCurrentlyPlaying() {
        return currentlyPlaying;
    }

    /**
     * Main Menu Interface CLI
     */
    public static void main(String[] args) {
        MusicPlayerApp app = new MusicPlayerApp();
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        System.out.println("=====================================================");
        System.out.println("SISTEM MANAJEMEN PLAYLIST & KATALOG MUSIK (TEMA 2)");
        System.out.println("=====================================================");

        while (choice != 7) {
            System.out.println("\n-----------------------------------------------------");
            if (app.getCurrentlyPlaying() != null) {
                System.out.println("📻 Now Playing: " + app.getCurrentlyPlaying());
            } else {
                System.out.println("📻 Now Playing: [Tidak ada lagu diputar]");
            }
            System.out.println("-----------------------------------------------------");
            System.out.println("1. Lihat Katalog Musik Bertingkat (Tree)");
            System.out.println("2. Tambah Lagu ke Antrean (Enqueue)");
            System.out.println("3. Lihat Antrean (Queue)");
            System.out.println("4. Putar Lagu Berikutnya (Play Next/Skip)");
            System.out.println("5. Lihat Riwayat Putar (History Stack)");
            System.out.println("6. Kembalikan ke Lagu Sebelumnya (Undo/Back)");
            System.out.println("7. Keluar Aplikasi");
            System.out.print("Pilih opsi menu (1-7): ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        System.out.println("\n=== KATALOG MUSIK HIERARKIS ===");
                        app.displayCatalog(app.getCatalogRoot(), "");
                        break;
                    case 2:
                        System.out.print("Masukkan Judul Lagu yang ingin dimasukkan antrean: ");
                        String titleInput = scanner.nextLine();
                        app.enqueueSong(titleInput);
                        break;
                    case 3:
                        app.displayQueue();
                        break;
                    case 4:
                        app.playNext();
                        break;
                    case 5:
                        app.displayHistory();
                        break;
                    case 6:
                        app.undoBack();
                        break;
                    case 7:
                        System.out.println("Keluar dari sistem pemutar musik. Terima kasih!");
                        break;
                    default:
                        System.out.println("[PERINGATAN] Pilihan menu tidak valid!");
                }
            } catch (InputMismatchException e) {
                System.out.println("[ERROR] Harap masukkan input berupa angka numerik!");
                scanner.nextLine(); // Clear buffer input salah
            }
        }
        scanner.close();
    }
}