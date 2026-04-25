# Tarka Fortune Wheel

Mobilna aplikacja na Androida (target SDK 36 / Android 16) z interaktywnym kołem fortuny.

## Funkcje

- Koło fortuny rysowane w czasie rzeczywistym (Jetpack Compose Canvas).
- Konfigurowalna liczba pól (od 2 do 24).
- Indywidualna etykieta dla każdego pola.
- Obracanie koła ruchem palca po ekranie z fizyką bezwładności (rzut/fling) i tarciem.
- Wskaźnik na górze koła pokazuje wynik po zatrzymaniu.

## Struktura

- `app/src/main/java/com/datark/fortunewheel/MainActivity.kt` - punkt wejścia.
- `app/src/main/java/com/datark/fortunewheel/FortuneWheelApp.kt` - UI (lista etykiet, ustawienia, wynik).
- `app/src/main/java/com/datark/fortunewheel/FortuneWheel.kt` - rysowanie koła i obsługa gestów.

## Wymagania

- Android Studio Ladybug+ lub nowszy.
- JDK 17.
- Android SDK z platformą 36 (Android 16).

## Budowanie

Po sklonowaniu repozytorium wygeneruj Gradle Wrapper (jednorazowo, jeśli go brakuje):

```
gradle wrapper --gradle-version 8.11.1 --distribution-type bin
```

Następnie zbuduj APK:

```
./gradlew assembleDebug
```

Wynikowy plik: `app/build/outputs/apk/debug/app-debug.apk`.

## Sterowanie

- Przesuń palcem po kole, aby je obrócić - prędkość ruchu palca wyznacza prędkość obrotu.
- Po puszczeniu palca koło zwalnia z tarciem i zatrzymuje się na losowym polu.
- Ikona zębatki w prawym górnym rogu otwiera ekran konfiguracji - tam ustawisz liczbę pól i ich etykiety.
