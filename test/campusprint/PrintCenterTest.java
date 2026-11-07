package campusprint;

import org.junit.jupiter.api.Test;

class PrintCenterTest {
    @Test
    void validatesJobLimitsAndCalculatesPrices() {
        PrintCenterChecks.checkJobLimitsAndPrices();
    }

    @Test
    void preservesTheQueueAcrossAPaperJam() {
        PrintCenterChecks.checkSchedulingAndRetry();
    }

    @Test
    void appliesSubscriptionChangesToTheNextPrint() {
        PrintCenterChecks.checkSubscriptionChanges();
    }

    @Test
    void sendsReceiptAndTicketFieldsToTheSameRenderer() {
        PrintCenterChecks.checkDocumentBridge();
    }

    @Test
    void keepsSpecialCharactersInOneMarkdownCell() {
        PrintCenterChecks.checkMarkdownCells();
    }

    @Test
    void demonstratesLiveQueueSelectionInTheConsole() {
        PrintCenterChecks.checkConsoleDemo();
    }
}
