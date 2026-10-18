# Aurelvia Makineleri — Yerel Derleme

Bu mod **sandbox içinde derleniyor**. GitHub Actions / Gradle / Loom **kullanılmıyor**
(çünkü bu ortamda Maven ve Mojang sunucularına erişim yok).

## Hazır JAR

Derlenmiş mod dosyası:

```
aurelvia-mod-project/AURELVIA-Machines-1.0.0-fabric-1.21.11.jar
```

Kurulum: `.minecraft/mods` klasörüne at. **Fabric API** kurulu olmalı.

## Yeniden derlemek için

```bash
python3 aurelvia-mod-project/tools/build_jar.py
```

Bu betik sırayla şunları yapar:

| Adım | Ne yapar |
|---|---|
| 1 | `tools/gen_stubs.py` — derleme zamanı API iskeletlerini üretir (`stubs/`, JAR'a girmez) |
| 2 | `tools/gen_resources.py` — dokuları ve tüm JSON kaynaklarını üretir |
| 3 | JSON dosyalarının tamamını doğrular |
| 4 | Eclipse **ECJ** ile Java 21 hedefleyerek derler |
| 5 | `tr/aurelvia/**` sınıflarını + `src/main/resources` içeriğini JAR'a yazar |

## Çalışma zamanı doğrulaması

Derlemek yetmez: stub'larda yanlış yazılmış bir imza derlemeyi geçer ama oyunda
`NoSuchMethodError` / `NoSuchFieldError` verir. Bunu önceden yakalamak için:

```bash
python3 aurelvia-mod-project/tools/verify_bytecode.py
```

Bu betik üretilen JAR'ın **constant pool**'unu okur, içindeki her
`net/minecraft/**` sınıf / metod / alan referansını gerçek **Minecraft 1.21.11
Mojang eşlemeleriyle** karşılaştırır ve bulunamayanı raporlar.

Son doğrulamada: 303 referans, **0 sorun**.

## Gereken araçlar (kalıcı kopya)

```
~/aurelvia-toolchain/ecj.jar          Eclipse JDT derleyicisi (Java 21)
~/aurelvia-toolchain/mcmap/           Minecraft 1.21.11 eşlemeleri (classes/methods/fields.csv)
/usr/local/lib/python3.11/dist-packages/jdk4py/java-runtime/bin/java   JRE 21 (ECJ'yi çalıştırır)
```

## Not: neden stub?

Elimizde JDK değil JRE var ve Minecraft/Fabric kütüphaneleri indirilemiyor.
Bu yüzden Minecraft ve Fabric API'nin **yalnızca imzalarından** oluşan
iskelet sınıflar üretiliyor; derleme bunlara karşı yapılıyor, JAR'a ise
sadece modun kendi sınıfları konuyor.

Bu yöntemde asıl risk stub imzalarının gerçektekinden sapmasıdır — o yüzden
yukarıdaki `verify_bytecode.py` denetimi zorunlu bir adımdır.
