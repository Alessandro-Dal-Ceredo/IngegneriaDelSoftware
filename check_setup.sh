#!/bin/bash

echo "🔍 Controllo setup RaccoltApp..."
echo ""

# Colori
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

errors=0

# Check file essenziali
echo "📁 Controllo file essenziali..."
files=(
    "app/build.gradle"
    "build.gradle"
    "settings.gradle"
    "app/src/main/AndroidManifest.xml"
    "app/src/main/java/it/unive/raccoltapp/MainActivity.java"
    "gradle/wrapper/gradle-wrapper.jar"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file"
    else
        echo -e "${RED}✗${NC} $file MANCANTE!"
        errors=$((errors+1))
    fi
done

echo ""
echo "🎨 Controllo risorse..."
resources=(
    "app/src/main/res/layout/activity_main.xml"
    "app/src/main/res/values/strings.xml"
    "app/src/main/res/values/colors.xml"
    "app/src/main/res/menu/bottom_nav_menu.xml"
)

for res in "${resources[@]}"; do
    if [ -f "$res" ]; then
        echo -e "${GREEN}✓${NC} $res"
    else
        echo -e "${RED}✗${NC} $res MANCANTE!"
        errors=$((errors+1))
    fi
done

echo ""
echo "⚙️  Controllo configurazione..."

# Check local.properties
if [ -f "local.properties" ]; then
    sdk_path=$(grep "sdk.dir" local.properties | cut -d'=' -f2)
    if [ -d "$sdk_path" ]; then
        echo -e "${GREEN}✓${NC} Android SDK trovato: $sdk_path"
    else
        echo -e "${YELLOW}⚠${NC}  Android SDK non trovato in: $sdk_path"
        echo -e "   Modifica local.properties con il path corretto!"
    fi
else
    echo -e "${RED}✗${NC} local.properties MANCANTE!"
    errors=$((errors+1))
fi

echo ""
if [ $errors -eq 0 ]; then
    echo -e "${GREEN}✅ Setup completo! Pronto per essere aperto in Android Studio${NC}"
    echo ""
    echo "Prossimi passi:"
    echo "1. Apri Android Studio"
    echo "2. File → Open → Seleziona questa cartella"
    echo "3. Aspetta il Gradle sync"
    echo "4. Premi Run ▶️"
else
    echo -e "${RED}❌ Trovati $errors problemi. Controlla i file mancanti sopra.${NC}"
fi

echo ""
