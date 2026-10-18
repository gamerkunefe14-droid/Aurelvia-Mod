#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Uretilen JAR'daki bytecode referanslarini GERCEK 1.21.11 Mojang eslemeleriyle
karsilastirir. Amac: derleme stub'larinda yanlis yazilmis bir sinif/uyenin
calisma aninda NoClassDefFoundError / NoSuchMethodError vermesini ONCEDEN yakalamak.

Kullanim: python3 tools/verify_bytecode.py
"""
import csv
import os
import struct
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MAPPINGS = os.environ.get("MCMAP", os.path.expanduser("~/aurelvia-toolchain/mcmap"))
JAR = os.path.join(ROOT, "AURELVIA-Machines-1.0.0-fabric-1.21.11.jar")


# ------------------------------------------------------------------ constant pool
def parse_header(data, cp):
    """class dosyasindan this_class / super_class / interfaces oku."""
    count = struct.unpack(">H", data[8:10])[0]
    i = 10
    n = 1
    while n < count:
        tag = data[i]; i += 1
        if tag == 1:
            ln = struct.unpack(">H", data[i:i + 2])[0]; i += 2 + ln
        elif tag in (3, 4):
            i += 4
        elif tag in (5, 6):
            i += 8; n += 1
        elif tag in (7, 8, 16, 19, 20):
            i += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            i += 4
        elif tag == 15:
            i += 3
        else:
            raise ValueError("tag %d" % tag)
        n += 1
    acc, this_i, super_i, icount = struct.unpack(">HHHH", data[i:i + 8])
    i += 8
    ifs = []
    for _ in range(icount):
        ifs.append(struct.unpack(">H", data[i:i + 2])[0]); i += 2
    def cls(idx):
        if idx == 0:
            return None
        v = cp.get(idx)
        return utf8(cp, v[1]) if v and v[0] == "Ref1" else None
    return cls(this_i), cls(super_i), [cls(x) for x in ifs]


def parse_cp(data):
    if data[:4] != b"\xca\xfe\xba\xbe":
        raise ValueError("class dosyasi degil")
    count = struct.unpack(">H", data[8:10])[0]
    cp = {}
    i = 10
    n = 1
    while n < count:
        tag = data[i]
        i += 1
        if tag == 1:
            ln = struct.unpack(">H", data[i:i + 2])[0]
            i += 2
            cp[n] = ("Utf8", data[i:i + ln].decode("utf-8", "replace"))
            i += ln
        elif tag in (3, 4):
            cp[n] = ("Num", None); i += 4
        elif tag in (5, 6):
            cp[n] = ("Wide", None); i += 8; n += 1
        elif tag in (7, 8, 16, 19, 20):
            cp[n] = ("Ref1", struct.unpack(">H", data[i:i + 2])[0]); i += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            a, b = struct.unpack(">HH", data[i:i + 4]); i += 4
            cp[n] = ({9: "Field", 10: "Method", 11: "IMethod", 12: "NameAndType",
                      17: "Dynamic", 18: "InvokeDynamic"}[tag], (a, b))
        elif tag == 15:
            cp[n] = ("Handle", struct.unpack(">BH", data[i:i + 3])); i += 3
        else:
            raise ValueError("bilinmeyen cp tag %d" % tag)
        n += 1
    return cp


def utf8(cp, idx):
    v = cp.get(idx)
    return v[1] if v and v[0] == "Utf8" else None


def references(classdata):
    """(sinif_ici_ad, uyeler) -> [(owner, name, desc, tur)]"""
    cp = parse_cp(classdata)
    out = set()
    for _, v in cp.values():
        pass
    for n, (tag, val) in cp.items():
        if tag in ("Field", "Method", "IMethod"):
            ci, nti = val
            cname = cp.get(cp[ci][1]) if cp[ci][0] == "Ref1" else None
            owner = cname[1] if cname else "?"
            nat = cp[nti][1]
            nm = utf8(cp, nat[0])
            ds = utf8(cp, nat[1])
            out.add((owner, nm, ds, tag))
        elif tag == "Ref1" and n:
            pass
    # sinif referanslari
    classes = set()
    for n, (tag, val) in cp.items():
        if tag == "Ref1":
            nm = utf8(cp, val)
            if nm:
                classes.add(nm)
    return out, classes


# ------------------------------------------------------------------ eslemeler
def load_mappings():
    classes = {}
    with open(os.path.join(MAPPINGS, "classes.csv"), encoding="utf-8-sig", newline="") as fh:
        rd = csv.reader(fh)
        hdr = next(rd)
        mi = hdr.index("Mojang")
        for r in rd:
            if len(r) > mi and r[mi]:
                classes[r[mi].replace(".", "/")] = r[0]
    obf_to_mojang = {}
    with open(os.path.join(MAPPINGS, "classes.csv"), encoding="utf-8-sig", newline="") as fh:
        rd = csv.reader(fh)
        hdr = next(rd)
        mi = hdr.index("Mojang")
        for r in rd:
            if len(r) > mi and r[mi]:
                obf_to_mojang[r[0]] = r[mi].replace(".", "/")

    all_method_names = set()
    all_field_names = set()
    methods = {}
    with open(os.path.join(MAPPINGS, "methods.csv"), encoding="utf-8-sig", newline="") as fh:
        rd = csv.reader(fh)
        hdr = next(rd)
        oi, mi, ni = hdr.index("owner_obf"), hdr.index("Mojang"), 1  # 1 = obf sutunu
        for r in rd:
            if len(r) <= max(oi, mi, ni):
                continue
            owner = obf_to_mojang.get(r[oi], "?")
            # Mojang sutunu TAM IMZA icerir ("isClientSide()") -> sadece adi al
            name = (r[mi] or r[ni]).split("(")[0].strip()
            if not name:
                name = "<init>"
            methods.setdefault(owner, set()).add(name)
            all_method_names.add(name)

    fields = {}
    with open(os.path.join(MAPPINGS, "fields.csv"), encoding="utf-8-sig", newline="") as fh:
        rd = csv.reader(fh)
        hdr = next(rd)
        oi, mi, ni = hdr.index("owner_obf"), hdr.index("Mojang"), 1  # 1 = obf sutunu
        for r in rd:
            if len(r) <= max(oi, mi, ni):
                continue
            owner = obf_to_mojang.get(r[oi], "?")
            name = (r[mi] or r[ni]).split("(")[0].strip()
            if name:
                fields.setdefault(owner, set()).add(name)
                all_field_names.add(name)
    return classes, methods, fields, all_method_names, all_field_names


def build_hierarchy(classes_dir):
    """stub .class dosyalarindan sinif hiyerarsisi (super + interface) kurar."""
    parents = {}
    for dirpath, _, files in os.walk(classes_dir):
        for f in files:
            if not f.endswith(".class"):
                continue
            path = os.path.join(dirpath, f)
            try:
                with open(path, "rb") as fh:
                    data = fh.read()
                cp = parse_cp(data)
                this, sup, ifs = parse_header(data, cp)
                if this:
                    parents[this] = [x for x in ([sup] + ifs) if x]
            except Exception:
                continue
    return parents


def chain(owner, parents):
    seen = set()
    stack = [owner]
    out = []
    while stack:
        c = stack.pop()
        if c in seen:
            continue
        seen.add(c)
        out.append(c)
        stack.extend(parents.get(c, []))
    return out


def main():
    if not os.path.isdir(MAPPINGS):
        print("!! eslemeler yok: %s (MCMAP=... ile belirt)" % MAPPINGS)
        sys.exit(2)
    classes, methods, fields, all_mnames, all_fnames = load_mappings()
    print("Eslemeler: %d sinif, %d sinifta metod, %d sinifta alan"
          % (len(classes), len(methods), len(fields)))

    parents = build_hierarchy(os.path.join(ROOT, "build", "classes"))
    print("Hiyerarsi: %d sinif (stub'lardan)" % len(parents))

    with zipfile.ZipFile(JAR) as z:
        entries = [n for n in z.namelist() if n.startswith("tr/aurelvia/") and n.endswith(".class")]
        refs = set()
        clsrefs = set()
        for e in entries:
            r, c = references(z.read(e))
            refs |= r
            clsrefs |= c
    print("Mod sinifi: %d | referans: %d | sinif referansi: %d"
          % (len(entries), len(refs), len(clsrefs)))

    # ---- sinif var mi?
    missing_classes = set()
    for c in clsrefs:
        if not c.startswith("net/minecraft/"):
            continue
        base = c.split("[")[-1].lstrip("L").rstrip(";")
        if base not in classes:
            missing_classes.add(base)

    # ---- uyeler var mi? (sadece net/minecraft sahipleri)
    missing_methods = set()
    missing_fields = set()
    inherited = []
    for owner, name, desc, kind in refs:
        if not owner.startswith("net/minecraft/"):
            continue
        if owner not in classes:
            continue  # sinif zaten raporlandi
        owners = chain(owner, parents)
        if kind == "Field":
            if not any(name in fields.get(o, set()) for o in owners):
                if name not in all_fnames:
                    missing_fields.add("%s.%s %s  [1.21.11'de HIC YOK]" % (owner.replace("/", "."), name, desc))
                else:
                    inherited.append("%s.%s  [baska sinifta tanimli -> kalitim, OK]" % (owner.replace("/", "."), name))
        else:
            if not any(name in methods.get(o, set()) or name == "<init>" for o in owners):
                if name not in all_mnames:
                    missing_methods.add("%s.%s%s  [1.21.11'de HIC YOK]" % (owner.replace("/", "."), name, desc))
                else:
                    decl = sorted({o.replace("/", ".") for o, s2 in methods.items() if name in s2})[:3]
                    inherited.append("%s.%s  [tanimli: %s -> kalitim, OK]" % (owner.replace("/", "."), name, ", ".join(decl)))

    print("\n--- BULUNAMAYAN SINIFLAR (%d) ---" % len(missing_classes))
    for m in sorted(missing_classes):
        print("  ", m)
    print("\n--- BULUNAMAYAN METODLAR (%d) ---" % len(missing_methods))
    for m in sorted(missing_methods):
        print("  ", m)
    print("\n--- BULUNAMAYAN ALANLAR (%d) ---" % len(missing_fields))
    for m in sorted(missing_fields):
        print("  ", m)

    print("\n--- KALITIMLA GELEN (sorun degil): %d ---" % len(inherited))
    for m in sorted(set(inherited)):
        print("  ", m)

    total = len(missing_classes) + len(missing_methods) + len(missing_fields)
    print("\nSONUC: %d sorun" % total)
    sys.exit(1 if total else 0)


if __name__ == "__main__":
    main()
