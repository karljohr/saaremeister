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
saaremeistrile määratakse vanglakaristus. Täpsemad reeglid on järgmised: kui vooru (aja)
lõppedes jääb saaremeistri kätte kasvõi üks vallutatav ala, siis võidab
saaremeister, vastasel juhul on võidukad maksuametnikud. Maksuametnikud saavad
olla nii päris mängijad kui ka AI, kuid saaremeister on alati päris mängija.

---

### Mängujuhend

- **Liitumine**:
  - Sisestada soovitud kasutajanimi ja vajutada nuppu "Liitu lobbyga"
  - Oodata lobbys kuni kõik mängijad on sisenenud
  - Vajutada nuppu "Start Game", et siseneda mängu
- **Liikumine**: klahvid W, A, S ja D
- **Tulistamine**: sihtimine hiirega ja tulistamine SPACE
- **Vestlusakna kasutus**:
  - Hiirega vajutada vestlusakna peale
  - Kirjutada sõnum
  - Vajutada hiireklahviga "->" nuppu
- **Poe kasutus**: klahv B ja avanevas aknas hiirega valimine
- **Ehitamine (Saaremeister)**:
  - Valida poest sobiv ehitis
  - Hiirega määrata ehitise asukoht ja ehitada vasaku hiireklahviga
  - Tühistada ehitamine parema hiireklahviga
- **Ala vallutamine**: Seista alas 5 sekundit (vastastiimi mängija ei tohi
samaaegselt samas alas seista)
- **Lahkumine**: ESC klahv ja valida menüüst "Lahku" või mänguakna sulgemine
- **Eesmärk**:
  - Saaremeistril: hoida saarel asuvad alad enda käes. Võitmiseks peab aja lõppedes
  vähemalt üks ala kätte jääma.
  - Maksuametnikul: püüda kõik vallutatavad alad enda tiimi valdusesse enne aja lõppu.

Mäng kestab nii mitu raundi, kui mitu mängijat on lobbys. Lobbys jookseb mängijatevaheline edetabel.

---

### Käivitamine

1. Kloonida projekti repo oma IDE-sse (soovitatavalt IntelliJ)
2. **SERVER (kui jooksutada kliendi masinas)**:
   1. Teha kindlaks, et serveri kataloog on moodul
   2. Vahetada Constants.java klassis muutuja SERVER_IP väärtus "localhost" peale
   3. Minna server → src → ee.kool.saaremeister.server → ServerLauncher.java
   4. Käivitada see fail
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

|Funktsioon|Kirjeldus|
|-|-|
|Mitmikmäng|Mängijate koguarv sõltub kasutaja masina võimsusest.<br/>Lobbysse ja ühte mängu mahub korraga kuni 5 mängijat, täitumisel luuakse uus lobby.|
|Lobby süsteem|Mängijad liituvad algselt lobbyga, kust nad saavad koos mängu liituda.|
|Paralleelsed mängu eksemplarid|Mitu mängu saavad toimuda samaaegselt.|
|Ehitamine|Saaremeister saab saarele ehitada kaitseehitisi, mis kas takistavad maksuametnike liikumist või aktiivselt tulistavad neid.|
|AI mängijad|Kui mängu alustab vähem kui 5 mängijaga lobby, asendavad puuduvaid mängijaid AI mängijad, kes sarnaselt päris mängijatele vallutavad alasid ja üritavad Saaremeistrit tappa.|
|Collisionitega kaart|Originaalsete disainidega 260x260 kaart töötava kokkupõrgete süsteemiga.|
|Majandussüsteem|Süsteem, kus raha eest saavad mängijad osta paremat varustust ning Saaremeister lisaks sellele ka kaitseehitisi.|
|Tekstipõhine vestlus|Mängus saab vestlusaknas tekstipõhiselt suhelda teiste mängijatega.|
|Raundide süsteem|Üks lobby mängib läbi nii mitu raundi, kui seal on mängijaid. Koos jooksva edetabeliga.|

---

### Autorid

- Joonas Reier
- Karl-Johannees Ränkel
- Christofer Toom

