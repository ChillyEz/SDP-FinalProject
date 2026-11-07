# CampusPrint

CampusPrint is a Java console simulation of a university print desk. Compare queue policies on the same orders, print a job, and render the receipt and operator's ticket in two formats.

**Software Design Patterns · Amir Baiseitov · SE-2505**

## Build and run

Use a JDK with Java 17 or later. Run these commands from the project root. The application and its checks use only the Java standard library.

**PowerShell**

```powershell
$files = (Get-ChildItem -Path src,test -Recurse -Filter *.java).FullName
javac --release 17 -encoding UTF-8 -d out $files
java -cp out campusprint.Main
java -cp out campusprint.PrintCenterChecks
```

**Bash**

```bash
mkdir -p out
find src test -name '*.java' > out/sources.txt
javac --release 17 -encoding UTF-8 -d out @out/sources.txt
java -cp out campusprint.Main
java -cp out campusprint.PrintCenterChecks
```

The main run prints an 80-page thesis and pages 2 through 3 of an application in two copies. FIFO previews the thesis. Shortest job first previews the application. A final comparison starts a fresh queue and changes the application to 50 copies. Supply a positive copy count to try another comparison:

```text
java -cp out campusprint.Main 10
```

The fresh queue only previews its next job. At 10 copies the application has 20 pages and is selected. Try 39 and 40 copies in turn. At 39 the 78-page application is shorter. At 40 both jobs have 80 pages and the earlier thesis wins the tie. At 50 copies the application has 100 pages and costs 2,000 KZT.

## Design

| Pattern | Entry point | Behavior |
| --- | --- | --- |
| Builder | `PrintJob.Builder` | Validates the range, copy count and print volume before acceptance. |
| Decorator | `BasicPrinting`, `Cover`, `Binding` | The selected finishing services supply both charges and production instructions. |
| Strategy | `QueuePolicy` | FIFO and shortest job first select from the same arrival-ordered queue. |
| Facade | `PrintCenter.printNext()` | Coordinates selection, printing, removal and publication in order. |
| Observer | `PrintSubscriber` | The journal and page counter subscribe independently to completed prints. |
| Bridge | `OrderDocument` and `DocumentRenderer` | A receipt and a job ticket can each be issued as plain text or Markdown. |

The [project specification](PROJECT_PLAN.md) gives the business rules. The [report](REPORT.md) includes diagrams and terminal screenshots. Application code lives in `src/` and executable regression checks in `test/`.

## Report and UML

The [pattern overview](docs/uml/architecture.svg) shows how the six patterns cooperate. Each pattern keeps the same color across the diagrams. The [complete implementation class diagram](docs/uml/class-model.svg) shows the fields, constructors, method signatures and structural relationships of all 25 application types. The PDF includes both views, with one A2 landscape sheet for the detailed model. The [document class diagram](docs/uml/document-bridge.svg) and [print sequence](docs/uml/print-next.svg) can also be opened individually. The editable PlantUML sources are in the same directory. To regenerate the SVGs with PlantUML 1.2025.4:

```text
java -jar plantuml.jar -charset UTF-8 -nometadata -tsvg "docs/uml/*.puml"
```

The editable report is `REPORT.md`. To rebuild its PDF, use Node.js 22.12+ and a Chromium browser. The report builder includes the screenshots, vector diagrams and complete Java application listing automatically.

```text
npm --prefix tools/report install --no-package-lock
node tools/report/build.cjs
```

The PDF is written to `output/pdf/CampusPrint-Report.pdf`. If the browser is installed outside its usual location, set `CHROME_PATH` to its executable. Report tools are optional for running the Java application.
