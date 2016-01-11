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
  private static int lastTransactionDay = 0; // to help calculate interest
  public static void main(String[] args){
    if(args.length != 3){
      // this is run from the command line using arguments
      System.out.println("usage: java LineOfCredit 35 1000 eventlist1.csv");
      // print out example usage if user is not sure how to use it
      System.exit(0); // have to exit because the arguments are likely not valid 
    }
    APR = Double.parseDouble(args[0])/100;
    CREDIT_LIMIT = Double.parseDouble(args[1]);
    Path eventListFile = Paths.get(args[2]);
    try{
      Stream<String[]> events = Files.lines(eventListFile).map(line -> line.split(","));
      events.forEach(eventArray -> {
        // format: [day, pay/draw, amount]
        int today = Integer.parseInt(eventArray[0]);
        double interestFromLastInterval = netAmountLoaned * (today - lastTransactionDay) / 365 * APR; // interest formula given in the instructions
        double amount = Double.parseDouble(eventArray[2]);
        if(eventArray[1].equals("pay")){
          netAmountLoaned -= amount;
          totalInterestDue += interestFromLastInterval;
          lastTransactionDay = today;
        }
        else if(netAmountLoaned + amount < CREDIT_LIMIT){
          netAmountLoaned += amount;
          totalInterestDue += interestFromLastInterval;
          lastTransactionDay = today;
          // I repeated these two lines because I thought it would be more readable/less clunky then creating another variable
          // `isValidLoan` or something. I originally wanted to have `continue` if the loan was not valid, and then if the end of
          // the iteration was reached the interest due and last transaction day would be updated.
          // however this is not possible to do with streams. Not using streams could be an option.
        }
      });
      events.close(); // close stream
      //last interest payment
      totalInterestDue += netAmountLoaned * (30 - lastTransactionDay) / 365 * APR;
      System.out.println("You still owe: " + netAmountLoaned + " and " + totalInterestDue + " for interest");
      System.out.println("Total payment: " + (netAmountLoaned + totalInterestDue));
    }
    catch (IOException e){
      System.out.println("Error reading file: " + e.getMessage());
    }
  }
  
}
