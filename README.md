# CampusPrint

CampusPrint is a Java simulation of a university print desk with a Swing interface. Enter orders, compare queue policies, print the next job, and inspect its receipt or job ticket.

**Software Design Patterns · Amir Baiseitov · SE-2505**  
**GitHub Repository:** [https://github.com/ChillyEz/SDP-FinalProject](https://github.com/ChillyEz/SDP-FinalProject)

## Build and run

Use JDK 17 or later. The project root is the folder containing `src`, `test` and this README.

### VS Code

1. Use **File > Open Folder** to open the project root. With the **Extension Pack for Java** installed, wait for **Java: Ready** in the status bar.
2. In **Run and Debug**, select **CampusPrint** and press **Ctrl+F5**. You can also open `src/campusprint/DesktopMain.java` and use **Run Java** or Code Runner's **Run Code** button.
3. In the window, enter the document length, page range and copies. Select finishing services and press **Add order**. Change the queue policy to see the next order. **Print next** updates the journal, counters and completed-order document. Select a queued row to preview its documents. **Simulate paper jam** lets you check failure and retry.
4. Open **Testing** (the beaker icon) and select **Run Tests**. Expand the project and `PrintCenterTest` to run any of the six tests individually. **Refresh Tests** updates the list after the folder opens.

### Terminal

Run these commands in PowerShell or Bash from the project root.

```text
javac --release 17 -encoding UTF-8 -d out -sourcepath src src/campusprint/DesktopMain.java
java -cp out campusprint.DesktopMain
```

`javac` writes the compiled classes to `out`. In the second command, `-cp out` tells Java where to find those classes. The desktop interface starts with an empty queue.

### Console demonstration

The **CampusPrint (console demo)** launch profile runs the scripted example. **CampusPrint (choose copies)** prompts for the comparison copy count. These can also run from the terminal.

```text
javac --release 17 -encoding UTF-8 -d out -sourcepath src src/campusprint/Main.java
java -cp out campusprint.Main 39
java -cp out campusprint.Main 40
```

Each application copy contains pages 2 and 3. At 39 copies the 78-page application wins the preview. At 40 copies both jobs have 80 pages and the earlier thesis wins the tie. Omitting the argument uses 50 copies.

### Regression checks

The six core checks can run directly on the JDK.

```text
javac --release 17 -encoding UTF-8 -sourcepath src -d out test/campusprint/PrintCenterChecks.java
java -cp out campusprint.PrintCenterChecks
```

To run the individual JUnit tests from a terminal, use the commands below. VS Code uses the same six test cases. The [JUnit Platform Console Standalone 1.14.4 distribution](https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.14.4/) is included in `lib/` with its license notices in the JAR.

```text
javac --release 17 -encoding UTF-8 -cp "lib/*" -sourcepath src -d out test/campusprint/PrintCenterChecks.java test/campusprint/PrintCenterTest.java
java -jar lib/junit-platform-console-standalone-1.14.4.jar execute --class-path out --scan-class-path --fail-if-no-tests
```

## Design

| Pattern | Entry point | Behavior |
| --- | --- | --- |
| Builder | `PrintJob.Builder` | Validates the range, copy count and print volume before acceptance. |
| Decorator | `BasicPrinting`, `Cover`, `Binding` | The selected finishing services supply both charges and production instructions. |
| Strategy | `PrintQueue`, `QueuePolicy` | FIFO and shortest job first select from the same arrival-ordered queue. |
| Facade | `PrintCenter` | Coordinates queue selection, printing, removal and publication in order. |
| Observer | `PrintSubscriber`, `PrintJournal`, `PrintStatistics` | Independent subscribers record completed orders and printed page totals after each print. |
| Bridge | `OrderDocument` and `DocumentRenderer` | The document and format selectors independently choose a receipt or ticket and plain text or Markdown. |
