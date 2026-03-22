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

- **Liitumine**: 
  - Sisestada soovitud kasutajanimi ja vajutada nuppu "Join a lobby"
  - Oodata lobbys kuni kõik mängijad on sisenenud
  - Vajutada nuppu "Start Game", et siseneda mängu
- **Liikumine**: klahvid W, A, S ja D
- **Vestlusakna kasutus**: 
  - Hiirega vajutada vestlusakna peale
  - Kirjutada sõnum
  - Vajutada hiireklahviga "Send" nuppu
- **Lahkumine**: "esc" klahv või mänguakna sulgemine
- **Eesmärk**: puudub

---

### Käivitamine

1. Kloonida projekti repo oma IDE-sse (soovitatavalt IntelliJ) (https://gitlab.cs.taltech.ee/chrito/iti0301-2026.git)
2. **SERVER**: 
   1. Teha kindlaks, et serveri kataloog on moodul 
   2. Minna server → src → ee.kool.saaremeister.server → ServerLauncher.java
   3. Käivitada see fail
3. **KLIENT**:
   1. Teha kindlaks, et desktopi kataloog on moodul
   2. Minna desktop → src → ee.kool.saaremeister → DesktopLauncher.java
   3. Käivitada see fail

**Lisaks**: DesktopLauncher.java faili võib käivitada üheaegselt mitu eksemplari, et mängida mitme mängijaga.


---

### Kasutatud tehnoloogiad

- Java 21
- LibGDX 1.12.1
- Kryonet 2.22.0-RC1
- Gradle 9.0.0

---

### Mängu funktsioonid (*features*)

| Funktsioon                     | Kirjeldus                                                                                                                                     |
|--------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| Mitmikmäng                     | Mängijate koguarv sõltub kasutaja masina võimsusest.<br/>Lobbysse ja ühte mängu mahub korraga kuni 10 mängijat, täitumisel luuakse uus lobby. |
| Lobby süsteem                  | Mängijad suunatakse algselt lobbysse, kust nad saavad koos mänguga liituda.                                                                   |
| Paralleelsed mängu eksemplarid | Mitu mängu saavad toimuda samaaegselt.                                                                                                        |
| Vestlusaken                    | Mängus saab vestlusaknas suhelda teiste mängijatega.                                                                                          |


---

### Autorid

- Joonas Reier
- Karl-Johannees Ränkel
- Christofer Toom

---

### Ühendus TalTechi serveriga

1. Minna shared → src → constant → Constants.java
2. Asendada rida `public static final String SERVER_IP = "localhost";` </br> selle vastu `public static final String SERVER_IP = "193.40.157.179";`
3. Käivitada DesktopLauncher.java eelneva juhendi järgi