# Aurelvia Makineleri Modu - Build Talimatları

## Önemli Not
GitHub Actions workflow dosyasını otomatik ekleyemediğim için, senin manuel olarak eklemen gerekiyor. Bu çok basit bir işlem:

### Adım 1: Workflow Dosyası Ekle
1. GitHub'da repoyu aç: https://github.com/gamerkunefe14-droid/Aurelvia-Mod
2. `aurelvia-mod-project` klasörüne git
3. Sol üstteki "Add file" → "Create new file" butonuna tıkla
4. Dosya adı: `.github/workflows/build.yml` (nokta ile başlıyor!)
5. Aşağıdaki içeriği yapıştır:

```yaml
name: Build Aurelvia Mod

on:
  push:
    branches: [ arena/80dc3dc8-aurelvia-mod ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Make gradlew executable
        run: chmod +x ./aurelvia-mod-project/gradlew
      
      - name: Build with Gradle
        run: cd aurelvia-mod-project && ./gradlew build --no-daemon
      
      - name: Upload JAR
        uses: actions/upload-artifact@v4
        with:
          name: Aurelvia-Machines-JAR
          path: aurelvia-mod-project/build/libs/*.jar
```

6. Sayfanın altına in ve "Commit changes" butonuna tıkla
7. "Actions" sekmesine git - build otomatik başlayacak
8. Build bitince "Aurelvia-Machines-JAR" artifact'ını indir

### Alternatif: Kendi Bilgisayarında Build
Eğer Java 21 kuruluysa:
```bash
git clone https://github.com/gamerkunefe14-droid/Aurelvia-Mod.git
cd Aurelvia-Mod
git checkout arena/80dc3dc8-aurelvia-mod
cd aurelvia-mod-project
./gradlew build
```
JAR dosyası `build/libs/` klasöründe olacak.

## Kurulum
İndirdiğin JAR dosyasını Minecraft'ın `mods` klasörüne koy. Mevcut Aurelvia modu ile birlikte çalışır.

## Özellikler
- **Bilgisayar**: Tek başına kullanılamaz, kimlik makinesinin parçası
- **Konveyör**: Tek başına konveyör olarak kullanılabilir
- **Kimlik Makinesi**: 2 blok genişliğinde, sağ tıklayınca şifre ister
- **Kimlik Kartı**: Üzerinde oyuncu adı ve "UNION of YELLOW BANANA" yazar

## Crafting Tarifleri

### Bilgisayar
```
I A I
I C I
I I I
```
I = Demir Külçe, A = Altın Külçe, C = Bakır Külçe

### Konveyör
```
L   L
S   S
  B  
```
L = Deri, S = Çubuk, B = Demir Blok

### Kimlik Makinesi
```
C    
K K K
I K I
```
C = Bilgisayar, K = Konveyör, I = Demir Blok

## Kullanım
1. Kimlik makinesini yere koy (otomatik olarak yanına konveyör eklenir)
2. Makineye sağ tıkla
3. Şifreyi gir: `UOhfyu7679nu6obBnoeryb79huı824byuYBYO`
4. Listeden bir oyuncu seç
5. Kimlik kartı konveyörden çıkacak
