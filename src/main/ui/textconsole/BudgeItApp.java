// Credit to TellerApp from the CPSC 210 phase 1 example for the command line application development.
// TellerApp -- https://github.students.cs.ubc.ca/CPSC210/TellerApp.git

package ui.textconsole;

import model.Account;
import model.DebtAcc;
import model.Source;
import persistence.JsonReader;
import persistence.JsonWriter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

/**
 * Represents the main UI processing for the BudgeItApp, initializing the user's account.
 */
public class BudgeItApp {
    private Account userAccount;
    private Scanner input;
    private final String saveLocation = "./data/UserAccount.json";

    public BudgeItApp() {
        startBudgeIt();
    }

    //MODIFIES: this
    //EFFECTS: Starts the application and creates the user's account.
    private void startBudgeIt() {
        boolean running = true;
        String command;

        init();

        while (running) {
            input = new Scanner(System.in);
            displayOptions();
            command = input.next();
            command = command.toLowerCase();

            if (command.equals("q")) {

                running = false;
            } else {
                commandLine(command);
            }
        }
    }

    //EFFECTS: runs the main operations associated with the user's inputs
    private void commandLine(String command) {
        if (command.equals("a")) {
            displayAccountOptions();
            commandLineAccount(awaitNextMenuCommand());
        } else if (command.equals("s")) {
            displaySavingsAccountOptions();
            commandLineSavings(awaitNextMenuCommand());
        } else if (command.equals("d")) {
            displayDebtAccountOptions();
            commandLineDebts(awaitNextMenuCommand());
        } else if (command.equals("sur")) {
            displaySurplus();
        } else if (command.equals("rec")) {
            displayReceipt();
        } else if (command.equals("recs")) {
            displayReceipts();
        } else if (command.equals("end")) {
            endPeriod();
        } else if (command.equals("file")) {
            displayFileOptions();
            commandLineFile(awaitNextMenuCommand());
        } else {
            System.out.println("Please enter a valid selection.");
        }
    }

    //EFFECTS: Processes the user's file save/load commands
    private void commandLineFile(String command) {
        if (command.equals("save")) {
            save();
        } else if (command.equals("load")) {
            load();
        } else if (command.equals("b")) {
            // RETURN TO MAIN MENU
        } else if (command.equals("cr")) {
            clearReceipts();
        } else if (command.equals("reset")) {
            resetAccount();
        } else {
            System.out.println("Please enter a valid selection.");

        }
    }

    //EFFECTS: Processes the user's account commands
    private void commandLineAccount(String command) {
        BigDecimal val;
        if (command.equals("d")) {
            System.out.println("Please enter the amount you'd like to deposit to your account balance: ");
            val = input.nextBigDecimal();
            userAccount.depositBalance(val);
        } else if (command.equals("w")) {
            System.out.println("Please enter the amount you'd like to withdraw from your account balance: ");
            val = input.nextBigDecimal();
            userAccount.withdrawBalance(val);
        } else if (command.equals("bal")) {
            showBalance();
        } else if (command.equals("u")) {
            updateBalance();
        } else if (command.equals("show")) {
            showSources();
        } else if (command.equals("add")) {
            addSource();
        } else if (command.equals("rem")) {
            removeSource();
        } else if (command.equals("b")) {
            // RETURN TO MAIN MENU
        } else {
            System.out.println("Please enter a valid selection.");
        }
    }

    //EFFECTS: Processes the user's savings commands
    private void commandLineSavings(String command) {
        BigDecimal val;

        if (command.equals("d")) {
            System.out.println("Please enter the amount you'd like to deposit to your savings: ");
            val = input.nextBigDecimal();
            addToSavings(val);
        } else if (command.equals("w")) {
            System.out.println("Please enter the amount you'd like to withdraw from your savings: ");
            val = input.nextBigDecimal();
            withdrawFromSavings(val);
        } else if (command.equals("bal")) {
            showSavings();
        } else if (command.equals("g")) {
            setSavingsGoal();
        } else if (command.equals("i")) {
            setSavingsInterestRate();
        } else if (command.equals("b")) {
            // RETURN TO MAIN MENU
        } else {
            System.out.println("Please enter a valid selection.");
        }
    }

    //EFFECTS: Processes the user's debt commands
    private void commandLineDebts(String command) {
        if (command.equals("d")) {
            showDebts();
        } else if (command.equals("p")) {
            payDebt();
        } else if (command.equals("a")) {
            addToDebt();
        } else if (command.equals("add")) {
            addDebt();
        } else if (command.equals("rem")) {
            removeDebt();
        } else if (command.equals("b")) {
            // RETURN TO MAIN MENU
        } else {
            System.out.println("Please enter a valid selection.");
        }
    }

    //EFFECTS: Used to await an update to the command in the menus
    private String awaitNextMenuCommand() {
        String command = input.next();
        command = command.toLowerCase();
        return command;
    }

    //MODIFIES: this
    //EFFECTS: Creates the user's account for entry
    private void init() {
        userAccount = new Account();
        input = new Scanner(System.in);
        input.useDelimiter("\n");
    }

    //EFFECTS: Display options for input to the user
    private void displayOptions() {
        System.out.println("\nSelect from:");
        System.out.println("\ta -> show account options");
        System.out.println("\ts -> show savings account options");
        System.out.println("\td -> show debt management options");
        System.out.println("\tsur -> show this month's surplus");
        System.out.println("\trec -> show this month's receipt");
        System.out.println("\trecs -> show all receipts on your account");
        System.out.println("\tend -> end the current month, and record a receipt to your account");
        System.out.println("\tfile -> access the options to save and load your file");
        System.out.println("\tq -> quit");
    }

    //EFFECTS: Displays options for modifying your account.
    private void displayFileOptions() {
        System.out.println("\nSelect from:");
        System.out.println("\tsave -> save your file to disk");
        System.out.println("\tload -> load your file from disk");
        System.out.println("\tcr -> clear receipts from your account, and reset the month tracker");
        System.out.println("\treset -> create a new account");
        System.out.println("\tb -> return to main menu");
    }

    //EFFECTS: Displays options for modifying your account
    private void displayAccountOptions() {
        System.out.println("\nSelect from:");
        System.out.println("\td -> deposit");
        System.out.println("\tw -> withdraw");
        System.out.println("\tbal -> show current account balance");
        System.out.println("\tu -> update your account balance quickly");
        System.out.println("\tshow -> show all income and expense sources");
        System.out.println("\tadd -> add income or expense to account");
        System.out.println("\trem -> remove income or expense from account");
        System.out.println("\tb -> return to main menu");
    }

    //EFFECTS: Displays the options for modifying your savings account
    private void displaySavingsAccountOptions() {
        System.out.println("\td -> deposit to savings"); //Account mod
        System.out.println("\tw -> withdraw from savings");
        System.out.println("\tbal -> show current savings account balance");
        System.out.println("\tg -> set a savings goal for your account");
        System.out.println("\ti -> set an interest rate for your savings");
        System.out.println("\tb -> return to main menu");
    }

    //EFFECTS: Displays the options for modifying your debt
    private void displayDebtAccountOptions() {
        System.out.println("\td -> show all current debts");
        System.out.println("\tp -> make a payment on one of your debts");
        System.out.println("\ta -> add value to one of your debts");
        System.out.println("\tadd -> add a debt to your account");
        System.out.println("\trem -> remove a debt from your account");
        System.out.println("\tb -> return to main menu");
    }

    //MODIFIES: this
    //EFFECTS: Updates the user's balance to given value
    private void updateBalance() {
        System.out.println("Please enter your desired account balance:");
        BigDecimal amount = input.nextBigDecimal();
        userAccount.updateBalance(amount);
    }

    //MODIFIES: this
    //EFFECTS: Adds a source to the userAccount
    private void addSource() {
        String name;
        BigDecimal val;

        System.out.println("Please enter the name of the source you'd like to add: ");
        name = input.next();
        input.nextLine();
        System.out.println("Please enter the value of the source you'd like to add: ");
        try {
            input.reset();
            val = input.nextBigDecimal();
            userAccount.addSource(name, val);
        } catch (Exception e) {
            System.out.println("Please make sure you enter a positive or negative number for value of the source.");
        }
    }

    //MODIFIES: this
    //EFFECTS: Removes a source from the userAccount
    private void removeSource() {
        String name;

        System.out.println("Please enter the name of the source you'd like to remove: ");
        name = input.next();

        if (userAccount.removeSource(name)) {
            System.out.println("Source removed.");
        } else {
            System.out.println("Source name could not be found, please try again.");
        }
    }

    //EFFECTS: Displays this month's budget surplus
    private void displaySurplus() {
        System.out.println("Your current surplus is: ");
        System.out.println(userAccount.calculateSurplus());
    }

    //REQUIRES: User inputs a value between 0 and 100
    //MODIFIES: this
    //EFFECTS: Updates savings goal percentage
    private void setSavingsGoal() {
        BigDecimal savingsGoal;

        System.out.println("Please enter your desired savings goal as a percentage (of your income): ");
        savingsGoal = input.nextBigDecimal();
        savingsGoal = savingsGoal.movePointLeft(2);

        if (savingsGoal.compareTo(BigDecimal.ONE) > 0) {
            System.out.println("Please make sure your value is not greater than 100%...");
        } else if (savingsGoal.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Please make sure your value is not less than 0%...");
        } else {
            userAccount.setSavingsPercentGoal(savingsGoal);
        }
    }

    //MODIFIES: this
    //EFFECTS: Adds a debt to the user's account
    private void addDebt() {
        BigDecimal interest;
        BigDecimal val;
        String name;

        System.out.println("Please enter the name of the debt: ");
        name = input.next();
        input.nextLine();
        System.out.println("Please enter the balance for the debt: ");
        val = input.nextBigDecimal();
        System.out.println("Please enter the interest rate for your debt: ");
        interest = input.nextBigDecimal();
        interest = interest.movePointLeft(2);

        if (interest.compareTo(BigDecimal.ONE) > 0) {
            System.out.println("Please make sure your value is not greater than 100%...");
        } else if (interest.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Please make sure your value is not less than 0%...");
        } else {
            userAccount.addDebt(name, val, interest);
        }
    }

    //MODIFIES: this
    //EFFECTS: Removes a debt from the userAccount
    private void removeDebt() {
        String name;

        System.out.println("Please enter the name of the debt you'd like to remove: ");
        name = input.next();

        if (!userAccount.removeDebt(name)) {
            System.out.println("Could not find that debt in your list.");
        } else {
            System.out.println("Debt removed.");
        }
    }

    //MODIFIES: this
    //EFFECTS: Makes a payment on a debt, reducing it to a minimum of zero
    private void payDebt() {
        String name;
        BigDecimal amt;
        System.out.println("Please enter the name of the debt you'd like to make a payment on: ");
        name = input.next();
        System.out.println("Please enter the payment amount: ");
        amt = input.nextBigDecimal();

        List<DebtAcc> debts = userAccount.getDebts();
        for (DebtAcc d : debts) {
            if (name.equals(d.getName())) {
                if (amt.compareTo(d.getValue()) > 0) {
                    System.out.println("You cannot pay a value greater than your debt balance.");
                } else {
                    d.subValue(amt);
                    userAccount.withdrawBalance(amt);
                    System.out.println("Balance remaining: " + d.getValue());
                }
            } else {
                System.out.println("We could not find that debt in your list!");
            }
        }

    }

    //MODIFIES: this
    //EFFECTS: Adds amt to one of userAccount's debts
    private void addToDebt() {
        String name;
        BigDecimal amt;
        System.out.println("Please enter the name of the debt you'd like to increase the value of: ");
        name = input.next();
        System.out.println("Please enter the amount to increase the debt by: ");
        amt = input.nextBigDecimal();

        List<DebtAcc> debts = userAccount.getDebts();
        for (DebtAcc d : debts) {
            if (name.equals(d.getName())) {
                d.addValue(amt);
            } else {
                System.out.println("We could not find that debt in your list!");
            }
        }
    }

    //MODIFIES: this
    //EFFECTS: Adds the given amt to your savings balance
    private void addToSavings(BigDecimal amt) {
        if (amt.compareTo(userAccount.getBalance()) > 0) {
            System.out.println("Cannot deposit more than your account balance to savings.");
        } else {
            userAccount.depositSavings(amt);
        }
    }

    //MODIFIES: this
    //EFFECTS: Withdraws from your savings balance to your account balance
    private void withdrawFromSavings(BigDecimal amt) {
        if (amt.compareTo(userAccount.getSavingsBal()) > 0) {
            System.out.println("You cannot withdraw more than your current balance. Please try again.");
        } else {
            userAccount.withdrawSavings(amt);
        }
    }

    //EFFECTS: displays this month's receipt
    private void displayReceipt() {
        System.out.println(userAccount.returnReceipt());
    }

    //EFFECTS: Displays all receipts attached to the user account
    private void displayReceipts() {
        if (userAccount.getReceipts().isEmpty()) {
            System.out.println("There are no receipts recorded for this account.");
        } else {
            for (String r : userAccount.getReceipts()) {
                System.out.println(r);
            }
        }
    }

    //EFFECTS: Prints out the user's current balance
    private void showBalance() {
        System.out.println("Here is your current account balance: ");
        System.out.println(userAccount.getBalance());
    }

    private void showSources() {
        for (Source s : userAccount.getSources()) {
            System.out.println("Name: " + s.getName() + ", Value: " + s.getValue());
        }
    }

    //EFFECTS: Prints out the user's current savings balance
    private void showSavings() {
        System.out.println("Here is your current savings balance: ");
        System.out.println(userAccount.getSavings().getBal());
    }

    //EFFECTS: Prints out the user's list of debts attached to the account
    private void showDebts() {
        System.out.println("Here are the debts listed on your account: ");
        for (DebtAcc da : userAccount.getDebts()) {
            System.out.println("Name: " + da.getName()
                    + "\nValue: " + da.getValue()
                    + "\nInterest: " + da.getInterest().movePointRight(2) + "%");
        }
    }

    //MODIFIES: this
    //EFFECTS: Ends the current period, and starts the next one. Records the receipt for the previous period.
    private void endPeriod() {
        System.out.println("Ending period...");
        userAccount.computeNextPeriod();
    }

    //EFFECTS: Saves a JSON object with the details of the current account object
    private void save() {
        try {
            System.out.println("File saved to " + saveLocation);
            JsonWriter writer = new JsonWriter(saveLocation);
            writer.open();
            writer.write(userAccount);
            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file " + saveLocation);
        }
    }

    //MODIFIES: this
    //EFFECTS: Loads the saved JSONObject and updates account to what was saved
    private void load() {
        try {
            System.out.println("File loading from " + saveLocation);
            JsonReader reader = new JsonReader(saveLocation);
            userAccount = reader.read();
        } catch (IOException e) {
            System.out.println("Unable to read file " + saveLocation);
        }
    }


    //MODIFIES: this
    //EFFECTS: Sets the savings interest rate on your savings account
    private void setSavingsInterestRate() {
        BigDecimal interest;
        System.out.println("Please enter the interest rate as a percentage: ");
        interest = input.nextBigDecimal();
        interest = interest.movePointLeft(2);

        if (interest.compareTo(BigDecimal.ONE) > 0) {
            System.out.println("Please make sure your value is not greater than 100%...");
        } else if (interest.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Please make sure your value is not less than 0%...");
        } else {
            userAccount.getSavings().setInterest(interest);
        }
    }

    //MODIFIES: this
    //EFFECTS: clears all receipts from the user's account, and resets the month tracker
    private void clearReceipts() {
        userAccount.getReceipts().clear();
        userAccount.setMonthTracker(1);
    }

    //MODIFIES: this
    //EFFECTS: Creates a brand-new account. NOTE: WILL NOT SAVE TO FILE, MUST MANUALLY SAVE.
    private void resetAccount() {
        userAccount = new Account();
    }
}
