#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Aurelvia Makineleri'ni sandbox icinde derleyip mod JAR'ini uretir.

Gradle/Loom KULLANMAZ (bu ortamda Maven/Mojang sunucularina erisim yok).
Bunun yerine:
  1) tools/gen_stubs.py      -> derleme zamani API iskeletleri (JAR'a girmez)
  2) tools/gen_resources.py  -> dokular + JSON kaynaklari
  3) Eclipse ECJ (Java 21)   -> derleme
  4) zipfile                 -> mod JAR'i (siniflar + kaynaklar)

Kullanim:  python3 tools/build_jar.py
"""
import json
import os
import subprocess
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = "/usr/local/lib/python3.11/dist-packages/jdk4py/java-runtime/bin/java"
ECJ_CANDIDATES = [
    os.path.expanduser("~/aurelvia-toolchain/ecj.jar"),
    "/tmp/ecj/package/server/plugins/org.eclipse.jdt.core.compiler.batch_3.46.100.v20260624-0231.jar",
]
BUILD = os.path.join(ROOT, "build")
CLASSES = os.path.join(BUILD, "classes")
RESOURCES = os.path.join(ROOT, "src", "main", "resources")
JAR_NAME = "AURELVIA-Machines-1.0.0-fabric-1.21.11.jar"


def run(step, cmd):
    print("\n>>> %s" % step)
    r = subprocess.run(cmd, capture_output=True, text=True, cwd=ROOT)
    if r.stdout.strip():
        print(r.stdout.strip()[:4000])
    if r.stderr.strip():
        print("[stderr]", r.stderr.strip()[:4000])
    if r.returncode != 0:
        print("!! %s basarisiz (kod %d)" % (step, r.returncode))
        sys.exit(1)
    return r.stdout


def find_ecj():
    for c in ECJ_CANDIDATES:
        if os.path.isfile(c):
            return c
    print("!! ECJ bulunamadi: %s" % ECJ_CANDIDATES)
    sys.exit(1)


def collect_sources():
    srcs = []
    for base in ("src/main/java", "src/client/java", "stubs"):
        for dirpath, _, files in os.walk(os.path.join(ROOT, base)):
            for f in files:
                if f.endswith(".java"):
                    srcs.append(os.path.relpath(os.path.join(dirpath, f), ROOT))
    return sorted(srcs)


def validate_json():
    bad = []
    n = 0
    for dirpath, _, files in os.walk(RESOURCES):
        for f in files:
            if not f.endswith(".json"):
                continue
            n += 1
            p = os.path.join(dirpath, f)
            try:
                with open(p, encoding="utf-8") as fh:
                    json.load(fh)
            except Exception as e:
                bad.append("%s: %s" % (os.path.relpath(p, ROOT), e))
    print(">>> JSON dogrulama: %d dosya, %d hatali" % (n, len(bad)))
    for b in bad:
        print("   HATALI:", b)
    if bad:
        sys.exit(1)


def main():
    run("1/5 stub uretimi", [sys.executable, "tools/gen_stubs.py"])
    run("2/5 kaynak uretimi", [sys.executable, "tools/gen_resources.py"])
    validate_json()

    ecj = find_ecj()
    srcs = collect_sources()
    print("\n>>> 3/5 ECJ ile derleme (%d dosya)" % len(srcs))
    if os.path.isdir(CLASSES):
        subprocess.run(["rm", "-rf", CLASSES], cwd=ROOT, check=True)
    os.makedirs(CLASSES, exist_ok=True)

    listfile = os.path.join(BUILD, "sources.txt")
    os.makedirs(BUILD, exist_ok=True)
    with open(listfile, "w", encoding="utf-8") as fh:
        fh.write("\n".join(srcs) + "\n")

    r = subprocess.run(
        [JAVA, "-jar", ecj,
         "-source", "21", "-target", "21", "-encoding", "UTF-8",
         "-nowarn", "-d", CLASSES, "@" + listfile],
        capture_output=True, text=True, cwd=ROOT)
    out = (r.stdout + r.stderr).strip()
    if out:
        print(out[:8000])
    if r.returncode != 0:
        print("!! Derleme basarisiz")
        sys.exit(1)

    mod_classes = []
    for dirpath, _, files in os.walk(os.path.join(CLASSES, "tr")):
        for f in files:
            if f.endswith(".class"):
                mod_classes.append(os.path.join(dirpath, f))
    print("    derlenen mod sinifi: %d" % len(mod_classes))
    if not mod_classes:
        print("!! hic sinif uretilmedi")
        sys.exit(1)

    jar_path = os.path.join(ROOT, JAR_NAME)
    print("\n>>> 4/5 JAR yaziliyor: %s" % JAR_NAME)
    if os.path.exists(jar_path):
        os.remove(jar_path)
    with zipfile.ZipFile(jar_path, "w", zipfile.ZIP_DEFLATED) as z:
        for c in sorted(mod_classes):
            z.write(c, os.path.relpath(c, CLASSES))
        for dirpath, _, files in os.walk(RESOURCES):
            for f in sorted(files):
                full = os.path.join(dirpath, f)
                z.write(full, os.path.relpath(full, RESOURCES))

    print("\n>>> 5/5 JAR icerigi")
    with zipfile.ZipFile(jar_path) as z:
        names = z.namelist()
        for n in sorted(names):
            print("   ", n)
        print("\nToplam giris: %d" % len(names))
        assert "fabric.mod.json" in names, "fabric.mod.json eksik!"
        assert any(n.startswith("tr/aurelvia/") for n in names), "mod siniflari eksik!"
        assert not any(n.startswith("net/minecraft/") for n in names), "stub sinifi sizmis!"
    size = os.path.getsize(jar_path)
    print("\nTAMAM: %s (%d bayt)" % (jar_path, size))


if __name__ == "__main__":
    main()
