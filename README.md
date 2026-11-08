# CampusPrint

CampusPrint is a Java console simulation of a university print desk. Compare queue policies on the same orders, print a job, and render the receipt and operator's ticket in two formats.

**Software Design Patterns · Amir Baiseitov · SE-2505**

## Build and run

Use JDK 17 or later. The project root is the folder containing `src`, `test` and this README.

### VS Code

1. Use **File > Open Folder** to open the project root. With the **Extension Pack for Java** installed, wait for **Java: Ready** in the status bar.
2. In **Run and Debug**, select **CampusPrint** and press **Ctrl+F5**. You can also open `src/campusprint/Main.java` and use **Run Java** or Code Runner's **Run Code** button.
3. For a different copy count, select **CampusPrint (choose copies)** in **Run and Debug**, press **Ctrl+F5** and enter a positive integer.
4. Open **Testing** (the beaker icon) and select **Run Tests**. Expand the project and `PrintCenterTest` to run any of the six tests individually. **Refresh Tests** updates the list after the folder opens.

### Terminal

Run these commands in PowerShell or Bash from the project root.

```text
javac --release 17 -encoding UTF-8 -d out -sourcepath src src/campusprint/Main.java
java -cp out campusprint.Main
```

`javac` writes the compiled classes to `out`. In the second command, `-cp out` tells Java where to find those classes and `campusprint.Main` is the package-qualified entry point. The optional number after the class name sets the application's copy count in the final, fresh queue. The initial run always prints the same two orders, totaling 84 pages.

```text
java -cp out campusprint.Main 39
java -cp out campusprint.Main 40
```

Each application copy contains pages 2 and 3. At 39 copies the 78-page application wins the preview. At 40 copies both jobs have 80 pages and the earlier thesis wins the tie. Omitting the argument uses 50 copies.

### Regression checks

After compiling the application above, the same six checks can run directly on the JDK.

```text
javac --release 17 -encoding UTF-8 -cp out -d out test/campusprint/PrintCenterChecks.java
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
| Strategy | `QueuePolicy` | FIFO and shortest job first select from the same arrival-ordered queue. |
| Facade | `PrintCenter.printNext()` | Coordinates selection, printing, removal and publication in order. |
| Observer | `PrintSubscriber` | The journal and page counter subscribe independently to completed prints. |
| Bridge | `OrderDocument` and `DocumentRenderer` | A receipt and a job ticket can each be issued as plain text or Markdown. |
