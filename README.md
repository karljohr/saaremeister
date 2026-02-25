# ITI0301-2026
## Saaremeister: Läänemere valitseja

---

### Mängu idee ja kokkuvõte


“Saaremeister: Läänemere valitseja” on king-of-the-hill top-down tüüpi videomäng, 
mille eesmärgiks on kas saaremeistrina oma saart vallutajate eest kaitsta ehitades 
kindlustusi ja kaitserelvi või maksuameti liikmetena saart rünnata ja see üle võtta.

---

### Idee kirjeldus

Mäng koosneb Läänemere kõige uhkemast meelelahutussaarest, ühest saaremeistrist 
ning mitmeliikmelisest maksuametnike tiimist. Saaremeistri ülesanne on oma 
lõbustussaart kaitsta ründavate maksuametnike eest, kes maksupettuse kahtlustamise 
tõttu üritavad saart üle võtta ning saaremeistrit kõrvaldada. Saaremeistril on 
enne igat vooru ressursse ning natuke aega luua saarele kindlustusehitisi ja -relvi, 
millega strateegiliselt maksuametnikele vastu võidelda. Maksuametnikud töötavad 
aga tiimina, mistõttu nemad saavad valmistusaega kasutada ründeplaanide koostamiseks 
ning relvade ostmiseks. Kui saaremeister suudab teatud aja lõppedes maksuametnikud 
välja tõrjuda, jääb saar talle alles ning meelelahutus jätkub samas vaimus. Kui 
aga maksuametnikud suudavad saare endale võtta, läheb saar riigi omandisse ning 
saaremeistrile määratakse vanglakaristus. Täpsemad reeglid on järgmised: kui vooru 
lõppedes jääb saaremeistri kätte vähemalt 20% saare pindalast, siis võidab 
saaremeister, vastasel juhul on võidukad maksuametnikud. Maksuametnikud saavad 
olla nii päris mängijad kui ka AI, kuid saaremeister on alati päris mängija.

---

### Mängujuhend

- **Liikumine**: klahvid W, A, S ja D.
- **Eesmärk**: liigutada enda ruut (rohelist värvi) musta ruutu.

---

### Käivitamine

1. Kloonida projekti repo oma IDE-sse (https://gitlab.cs.taltech.ee/chrito/iti0301-2026.git)
2. Panna tööle fail iti0301-2026 -> server -> src -> ee.kool.server -> ChatServer.java
3. Panna tööle fail iti0301-2026 -> server -> src -> ee.kool.server -> GameServer.java
4. Panna tööle fail iti0301-2026 -> desktop -> src -> ee.kool.saaremeister -> DesktopLauncher.java

DesktopLauncher.java võib käivitada mitu korda, et mängida mitme mängijaga.

---

### Kasutatud tehnoloogiad

- Java 21
- LibGDX 1.12.1
- Kryonet 2.22.0-RC1
- Gradle 9.0.0

---

### Mängu funktsioonid (*features*)

| Funktsioon        | Kirjeldus                 |
|-------------------|---------------------------|
| Mitmikmäng        | Mängijate arv sõltub kasutaja masina võimsusest |

---

### Autorid

- Joonas Reier
- Karl-Johannees Ränkel
- Christofer Toom

---

### Ühendus TalTechi serveriga

Hetkel puudub.