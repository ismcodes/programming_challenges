import java.util.stream.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class LineOfCredit {
  private static double APR, CREDIT_LIMIT;
  // these should be set when the program is run and then not change after that
  // is the credit limit different then loan amount? I suppose it could be, but that does
  // not happen in the examples
  private static double netAmountLoaned, totalInterestDue = 0.0;
  private static int lastTransactionDay = 1;
  public static void main(String[] args){
    if(args.length == 0){
      System.out.println("usage: java LineOfCredit 35 1000 eventlist1.csv");
      System.exit(0);
    }
    APR = Double.parseDouble(args[0])/100;
    CREDIT_LIMIT = Double.parseDouble(args[1]);
    Path eventListFile = Paths.get(args[2]);
    try {
      Stream<String[]> events = Files.lines(eventListFile).map(line -> line.split(","));
      events.forEach(eventArray -> {
        // format: [day, pay/draw, amount]
        int today = Integer.parseInt(eventArray[0]);
        double interestFromLastInterval = netAmountLoaned * (today - lastTransactionDay) / 365 * APR;
        double amount = Double.parseDouble(eventArray[2]);
        if (eventArray[1].equals("pay")) {
          netAmountLoaned -= amount;
          totalInterestDue += interestFromLastInterval;
          lastTransactionDay = today;
        }
        else if (netAmountLoaned + amount < CREDIT_LIMIT) {
          netAmountLoaned += amount;
          totalInterestDue += interestFromLastInterval;
          lastTransactionDay = today;
          // I repeated these two lines because I thought it would be more readable/less clunky then creating another variable
          // `isValidLoan` or something. I originally wanted to have `continue` if the loan was not valid, and then if the end of
          // the iteration was reached the interest due and last transaction day would be updated.
        }
      });
      events.close(); // close stream
      //last interest payment
      totalInterestDue += netAmountLoaned * (30 - lastTransactionDay) / 365 * APR;
      System.out.println("You still owe: " + netAmountLoaned + " and " + totalInterestDue + " for interest");
      System.out.println("Total owed: " + (netAmountLoaned + totalInterestDue));
    }
    catch (IOException e){
      System.out.println("Error reading file: " + e.getMessage());
    }
  }
  
}
