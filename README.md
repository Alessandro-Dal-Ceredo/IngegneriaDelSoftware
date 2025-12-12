# RaccoltApp - Progetto Android

App per la gestione della raccolta differenziata con calendario, mappa dei punti di raccolta, segnalazioni e profilo utente.

## Struttura creata

```
RaccoltApp/
├── app/src/main/
│   ├── java/it/unive/raccoltapp/
│   │   ├── MainActivity.java
│   │   ├── CalendarFragment.java
│   │   ├── MapFragment.java
│   │   ├── ReportsFragment.java
│   │   └── ProfileFragment.java
│   └── res/
│       ├── layout/
│       │   ├── activity_main.xml
│       │   ├── fragment_calendar.xml
│       │   ├── fragment_map.xml
│       │   ├── fragment_reports.xml
│       │   └── fragment_profile.xml
│       └── menu/
│           └── bottom_nav_menu.xml
└── README.md
```

## Come avviare il progetto

### Opzione 1: Aprire in Android Studio (CONSIGLIATO)

1. **Apri Android Studio**
2. **File → Open** e seleziona la cartella `RaccoltApp`
3. Aspetta che Gradle scarichi le dipendenze (potrebbe richiedere alcuni minuti al primo avvio)
4. Se ti chiede di aggiornare il plugin Gradle o Android SDK, clicca su **Update** o **Install**
5. **Verifica il path dell'SDK**: vai in `local.properties` e controlla che `sdk.dir` punti al tuo Android SDK
   - Su Mac: di solito è `/Users/TUONOME/Library/Android/sdk`
   - Per trovarlo: Android Studio → Settings → Appearance & Behavior → System Settings → Android SDK
6. **Connetti un dispositivo Android o avvia un emulatore**:
   - Dispositivo fisico: abilita "Opzioni sviluppatore" e "Debug USB"
   - Emulatore: Tools → Device Manager → Create Device
7. Clicca sul pulsante **Run** ▶️ (o premi Ctrl+R / Cmd+R)

### Opzione 2: Build da linea di comando

```bash
cd RaccoltApp
./gradlew assembleDebug
```

L'APK verrà generato in `app/build/outputs/apk/debug/app-debug.apk`

### Risoluzione problemi comuni

**Errore "SDK location not found"**:
- Modifica il file `local.properties` e imposta il path corretto del tuo Android SDK

**Errore "Gradle sync failed"**:
- Controlla la connessione internet (Gradle deve scaricare le dipendenze)
- Android Studio → File → Invalidate Caches / Restart

**L'app non si avvia sull'emulatore**:
- Verifica che l'emulatore abbia almeno API 24 (Android 7.0)
- Device Manager → Create Virtual Device → Scegli un dispositivo con API 24+

## Miglioramenti apportati rispetto alla bozza

1. **Layout più strutturati**: ho usato `LinearLayout` al posto di `FrameLayout` per i fragment, con titoli più grandi e spaziatura migliore
2. **Commenti esplicativi**: ogni layout ha commenti che indicano dove aggiungere funzionalità future
3. **Coerenza**: tutti i costruttori dei fragment hanno il commento sul costruttore vuoto richiesto
4. **Titoli styled**: textStyle="bold" e dimensioni appropriate (24sp per i titoli)

## Prossimi passi

1. **Calendario**: sostituire il TextView placeholder con un `CalendarView` o `RecyclerView` per mostrare le date
2. **Mappa**: integrare Google Maps o OpenStreetMap per visualizzare i punti di raccolta
3. **Segnalazioni**: creare un form per inviare segnalazioni con foto
4. **Profilo**: aggiungere camcampi per nome, indirizzo, preferenze notifiche
5. **Icone personalizzate**: sostituire le icone di sistema con icone Material Design personalizzate
6. **Database locale**: utilizzare Room per salvare i dati offline
7. **Backend**: integrare con un'API REST per sincronizzare i dati

## Note

- Il package è impostato su `it.unive.raccoltapp` - se usi un package diverso, modificalo in tutti i file Java
- Gli icone del menu bottom navigation usano icone di sistema Android per ora - sostituiscile con icone personalizzate più avanti
- Tutti i fragment sono al momento placeholder - espandili in base alle funzionalità specifiche richieste
