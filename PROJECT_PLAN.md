# CampusPrint project specification

Amir Baiseitov · SE-2505 · Software Design Patterns

## Scope

A single operator accepts print orders, chooses a scheduling policy and prints the next order. The Java console program uses one simulated printer and an in-memory queue. The implementation covers six course patterns in one workflow.

| Pattern | Responsibility | Demonstration |
| --- | --- | --- |
| Builder | Construct a valid, immutable print job | Select a page range and a copy count. Reject an inverted range. |
| Decorator | Compose printing, cover and binding charges | Each service adds a charge to the receipt and its operation name to the ticket. |
| Strategy | Choose the next order in the current queue | Switch between arrival order and shortest job first. |
| Facade | Coordinate selection, printing, removal and publication | Keep the pending order after a paper jam, then retry it. |
| Observer | Deliver each completion to registered subscribers | Check the journal and the page count after printing. |
| Bridge | Vary document content and output format independently | Render a receipt and a job ticket in plain text and Markdown. |

## Business rules

Page ranges are inclusive. The full document and one copy are the defaults. The number of printed pages must fit a positive Java `int`. Monetary amounts and the running page count use `long`. Printing costs 20 KZT per printed page. A cover costs 100 KZT per copy and binding costs 200 KZT per copy. Each finishing service can be selected once.

Switching policies preserves arrival order. When two jobs have the same volume, the earlier arrival wins. A preview leaves the queue intact. A failed print leaves the job pending. A successful print removes the job and delivers one event to every subscriber registered at the start of the notification.

## Defense scenario

1. Accept an 80-page thesis with a cover and binding. Accept pages 2 through 3 of a five-page application, in two copies.
2. Compare the two previews. FIFO selects the thesis. Shortest job first selects the four-page application.
3. Print the application. The journal records order 2 and statistics show one job and four pages.
4. Render the thesis receipt and ticket in both formats. The receipt totals 1,900 KZT.
5. Print the thesis. A further request finds the queue empty. Statistics remain at two jobs and 84 pages.
6. Repeat the preview with 50 application copies. Its volume rises to 100 pages and its price to 2,000 KZT. The thesis is now shorter.

## Submission

Submit the PDF report and the source ZIP to Moodle by **15 November 2026 at 23:59 Astana time**. The defense is scheduled for week 10. The report must contain an introduction, main body, conclusion, future work, Java source, application screenshots and UML diagrams.
