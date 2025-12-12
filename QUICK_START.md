# 🚀 Quick Start - RaccoltApp

## ✅ Progetto completo e pronto!

Ho creato tutti i file necessari per far funzionare l'applicazione Android.

## 📱 Come avviarla ADESSO

### Metodo 1: Android Studio (3 passi)

1. **Apri Android Studio**
2. **File → Open** → Seleziona la cartella `RaccoltApp`
3. Aspetta che Gradle finisca il sync, poi premi il pulsante **Run ▶️**

### Metodo 2: Da terminale

```bash
cd ~/Developer/IngegneriaSoftware/RaccoltApp
./gradlew assembleDebug
```

## 🔧 Se ti dà errori

### Errore: "SDK location not found"
Apri `local.properties` e cambia questa riga con il path corretto:
```
sdk.dir=/Users/TUONOME/Library/Android/sdk
```

**IMPORTANTE**: Il file `local.properties` attuale ha `/Users/dalce/Library/Android/sdk` - cambialo con il TUO username se necessario!

Per trovare il path giusto:
- Android Studio → Settings (o Preferences su Mac)
- Cerca "Android SDK"
- Copia il percorso mostrato in "Android SDK Location"

### Errore: "Gradle sync failed"
- Aspetta che finisca il download delle dipendenze (serve internet)
- Se persiste: File → Invalidate Caches / Restart

### L'app crashha all'avvio
- Assicurati che l'emulatore sia almeno Android 7.0 (API 24)

## 📦 Cosa ho creato

✅ Tutti i file Java (MainActivity + 4 Fragment)  
✅ Tutti i layout XML (activity + 4 fragment)  
✅ Menu bottom navigation  
✅ AndroidManifest.xml  
✅ build.gradle con dipendenze  
✅ File di risorse (colors, strings, themes)  
✅ Gradle wrapper  
✅ Configurazione Android Studio  

## 🎯 Cosa fa l'app

L'app ha 4 sezioni accessibili dalla bottom navigation bar:

1. **📅 Calendario** - Mostra le date della raccolta differenziata
2. **🗺️ Mappa** - Punti di raccolta sulla mappa
3. **📝 Segnalazioni** - Invia segnalazioni
4. **👤 Profilo** - Gestione profilo utente

Al momento ogni sezione mostra solo un titolo placeholder - pronta per essere espansa!

## 🎨 Personalizzazioni veloci

**Cambiare colori**: modifica `app/src/main/res/values/colors.xml`

**Cambiare nome app**: modifica `app/src/main/res/values/strings.xml`

**Cambiare icone bottom nav**: modifica `app/src/main/res/menu/bottom_nav_menu.xml`

---

💡 **Consiglio**: Prova prima ad avviare l'app così com'è, poi inizia ad espandere le funzionalità!
