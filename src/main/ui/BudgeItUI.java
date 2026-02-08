//CREDIT: Based on AlarmController example from CPSC-210 at UBC. Provided in week C4 of class instruction:
//https://github.students.cs.ubc.ca/CPSC210/AlarmSystem

package ui;

import model.Account;
import model.DebtAcc;
import model.Event;
import model.EventLog;
import model.Source;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import persistence.JsonReader;
import persistence.JsonWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.jfree.data.general.DatasetUtils.findMaximumStackedRangeValue;
import static org.jfree.data.general.DatasetUtils.findMinimumStackedRangeValue;

/**
 * Represents applications main UI window frame.
 */
public class BudgeItUI extends JFrame {
    private static final int WIDTH = 1500;
    private static final int HEIGHT = 700;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final String CURRENT = "Current";
    private static final String FUTURE = "Next month";
    private static final Dimension PREFERRED_SIZE = new Dimension(WIDTH / 4, HEIGHT - 300);
    private static final Dimension PREFERRED_BUTTON_SIZE = new Dimension(WIDTH / 4, HEIGHT - 600);

    private final String saveLocation = FilePathManager.getSaveLocation();

    private Account userAccount;
    private final JFrame desktop;
    private final JPanel desktopMain;
    private JLabel balanceLabel;
    private ChartPanel graph;
    private ChartPanel debts;
    private ChartPanel savings;


    private JMenu fileMenu;
    private JMenu chartMenu;
    private JMenu quickEditMenu;
    private JMenu sourceEdit;
    private JMenu debtEdit;

    //EFFECTS: Sets up the main window with visual elements and interaction.
    public BudgeItUI() {
        userAccount = new Account();

        desktop = new JFrame();
        desktop.addMouseListener(new DesktopFocusAction());

        desktopMain = new JPanel();
        desktop.add(desktopMain);
        GridBagLayout desktopLayout = new GridBagLayout();
        desktopMain.setLayout(desktopLayout);

        setContentPane(desktopMain);
        setTitle("Don't Budge-It!");
        setSize(WIDTH, HEIGHT);

        addMenu();
        addVisuals(desktopMain);

        setCloseFunctions();
        centreOnScreen();
        setVisible(true);
        pack();
    }

    //EFFECTS: Prints the log of all actions taken to the console on close
    private void setCloseFunctions() {
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (Event event : EventLog.getInstance()) {
                    System.out.println(event);
                }
                EventLog.getInstance().clear();
                System.exit(0);
            }
        };
        this.addWindowListener(exitListener);
    }

    //MODIFIES: this
    //EFFECTS: Adds the visual components, with their correct gridbaglayout constraints to the main panel. Panels are
    //          aligned to align the modification button panels with its respective graph component.
    private void addVisuals(Component frame) {
        frame.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.gridx = 3;
        c.gridy = 0;
        addGraph(c);
        c.gridx =  2;
        addDebtGraph(c);
        c.gridx = 1;
        addSavingsGraph(c);
        c.gridx = 0;
        addImage(c);
        c.gridx = 3;
        c.gridy = 1;
        addSourceButtons(c);
        c.gridx = 2;
        addDebtButtons(c);
        c.gridx = 1;
        addSavingsButtons(c);
        c.gridx = 0;
        addBalance(c);
    }

    //MODIFIES: this
    //EFFECTS: Adds the excellent little frog mascot to the image panel
    private void addImage(GridBagConstraints c) {
        JPanel imagePanel = new JPanel();
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setLayout(new GridBagLayout());
        GridBagConstraints inner = new GridBagConstraints();
        imagePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        inner.fill = GridBagConstraints.BOTH;
        try {
            Image image = ImageIO.read(new File("./data/gefraks.jpg"));
            image = image.getScaledInstance(300, 300, 0);
            JLabel picLabel = new JLabel(new ImageIcon(image));
            JLabel picText = new JLabel("Sir Gerald Budge-It");
            picText.setText("Sir Gerald Budge-It");
            inner.gridx = 0;
            inner.gridy = 1;
            imagePanel.add(picLabel, inner);
            inner.gridy = 0;
            imagePanel.add(picText, inner);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        desktopMain.add(imagePanel, c);
    }

    //MODIFIES: this
    //EFFECTS: Adds the balance display and its modification buttons
    private void addBalance(GridBagConstraints c) {
        BalanceButtons balanceButtonsGrid = new BalanceButtons();
        JPanel balancePanel = new JPanel();

        balancePanel.add(balanceButtonsGrid);

        balancePanel.setSize(PREFERRED_BUTTON_SIZE);

        desktopMain.add(balancePanel, c);
    }

    //MODIFIES: this
    //EFFECTS: Adds the source modification button panel
    private void addSourceButtons(GridBagConstraints c) {
        SourceButtons sourceButtonGrid = new SourceButtons();

        sourceButtonGrid.setSize(PREFERRED_BUTTON_SIZE);
        desktopMain.add(sourceButtonGrid, c);
    }

    //MODIFIES: this
    //EFFECTS: adds the debt modification button panel
    private void addDebtButtons(GridBagConstraints c) {
        DebtButtons debtButtonGrid = new DebtButtons();

        debtButtonGrid.setSize(PREFERRED_BUTTON_SIZE);

        desktopMain.add(debtButtonGrid, c);
    }

    //MODIFIES: this
    //EFFECTS: adds the savings buttons panel
    private void addSavingsButtons(GridBagConstraints c) {
        SavingsButtons savingsButtonGrid = new SavingsButtons();

        savingsButtonGrid.setSize(PREFERRED_BUTTON_SIZE);

        desktopMain.add(savingsButtonGrid, c);
    }

    //MODIFIES: this
    //EFFECTS: adds the source graph panel, with income, expenses, and surplus
    private void addGraph(GridBagConstraints c) {
        CategoryDataset sourceDataset = createSurplusDataSet();

        JFreeChart inOutGraph = ChartFactory.createStackedBarChart("Income/Expense Graph",
                "Sources",
                "Value ($)", sourceDataset);

        LegendTitle legend = inOutGraph.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        graph = new ChartPanel(inOutGraph);
        graph.setPreferredSize(PREFERRED_SIZE);
        graph.setDomainZoomable(false);
        graph.setRangeZoomable(false);
        graph.setVisible(true);

        desktopMain.add(graph, c);
        pack();
    }

    //MODIFIES: this
    //EFFECTS: adds the debt graph panel with all debts current values, followed by their value in the next period.
    private void addDebtGraph(GridBagConstraints c) {
        CategoryDataset debtDataset = createDebtFutureDataSet();

        JFreeChart debtFutureGraph = ChartFactory.createBarChart("Debt Interest Accumulation",
                "Debts",
                "Value ($)",
                debtDataset);

        LegendTitle legend = debtFutureGraph.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        debts = new ChartPanel(debtFutureGraph);
        debts.setPreferredSize(PREFERRED_SIZE);
        debts.setDomainZoomable(false);
        debts.setRangeZoomable(false);
        debts.setVisible(true);

        desktopMain.add(debts, c);
        pack();
    }

    //MODIFIES: this
    //EFFECTS: adds the savings graph with current value and future value displayed side by side
    private void addSavingsGraph(GridBagConstraints c) {
        CategoryDataset savingsDataset = createSavingsFutureDataset();

        JFreeChart savingsFutureGraph = ChartFactory.createBarChart("Savings Interest Accumulation",
                "Savings",
                "Value ($)",
                savingsDataset);

        savingsFutureGraph.removeLegend();

        savings = new ChartPanel(savingsFutureGraph);
        savings.setPreferredSize(PREFERRED_SIZE);
        savings.setDomainZoomable(false);
        savings.setRangeZoomable(false);
        savings.setVisible(true);

        desktopMain.add(savings, c);
        pack();
    }

    //EFFECTS: Creates a dataset representing current and future balance of a savings account.
    private CategoryDataset createSavingsFutureDataset() {
        BigDecimal savingsVal = userAccount.getSavingsBal();
        BigDecimal savingsInt = userAccount.getSavings().getInterest();
        BigDecimal savingsGoal = userAccount.getSavingsPercentGoal();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        dataset.addValue(savingsVal, "Savings", CURRENT);
        dataset.addValue(savingsVal.multiply(ONE.add(savingsInt)), "Savings", FUTURE);
        dataset.addValue(savingsVal.multiply(savingsGoal), "Savings", "Savings Goal");

        return dataset;
    }

    //EFFECTS: Creates a dataset representing current and future values of multiple debt accounts
    private CategoryDataset createDebtFutureDataSet() {
        List<DebtAcc> debts = userAccount.getDebts();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (DebtAcc d : debts) {
            BigDecimal val = d.getValue();
            String nameKey = d.getName();
            dataset.addValue(val, nameKey, CURRENT);

            BigDecimal futureVal = d.getValue().multiply(ONE.add(d.getInterest()));

            dataset.addValue(futureVal, nameKey, FUTURE);
        }
        return dataset;
    }

    //EFFECTS: Creates a CategoryDataset of the userAccount's income and expense sources.
    private CategoryDataset createSurplusDataSet() {
        final String incomeColumn = "Income";
        final String expenseColumn = "Expense";

        List<Source> sources = userAccount.getSources();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Source s : sources) {
            BigDecimal val = s.getValue();
            String nameKey = s.getName();
            if (val.compareTo(BigDecimal.ZERO) > -1) {
                dataset.addValue(val, nameKey, incomeColumn);
            } else {
                dataset.addValue(val, nameKey, expenseColumn);
            }
        }

        if (dataset.getColumnCount() != 0) {
            createSurplusCategory(dataset);
        }

        return dataset;
    }

    //EFFECTS: Creates a surplus column for a graph representing income and expenses.
    private void createSurplusCategory(DefaultCategoryDataset dataset) {
        Number totalIncomeNum = findMaximumStackedRangeValue(dataset);
        Number totalExpenseNum = findMinimumStackedRangeValue(dataset);

        BigDecimal totalIncome = new BigDecimal(totalIncomeNum.toString());
        BigDecimal totalExpense = new BigDecimal(totalExpenseNum.toString());
        BigDecimal totalSurplus = totalIncome.add(totalExpense);
        dataset.addValue(totalSurplus, "Surplus", "Surplus");
    }

    //EFFECTS: standardizes the way balance BigDecimals are displayed
    private void setBalanceLabelText(BigDecimal bd) {
        balanceLabel.setText("Balance: " + bd);
    }

    //MODIFIES: this
    //EFFECTS: Adds a menu bar with file functionality, quick editing functionality, and a refresh button for graphs
    private void addMenu() {
        JMenuBar menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        quickEditMenu = new JMenu("QuickEdit");
        quickEditMenu.setMnemonic('Q');

        sourceEdit = new JMenu("Source");
        debtEdit = new JMenu("Debt");

        chartMenu = new JMenu("Chart");

        addInternalMenu();
        addMenuFunctions();

        menuBar.add(fileMenu);
        menuBar.add(quickEditMenu);
        menuBar.add(chartMenu);

        setJMenuBar(menuBar);
    }

    //MODIFIES: this
    //EFFECTS: Adds quick keystroke shortcuts and mnemonics to the menu bar
    private void addMenuFunctions() {
        addMenuItem(fileMenu, new LoadAction(),
                KeyStroke.getKeyStroke("control F"));
        addMenuItem(fileMenu, new SaveAction(),
                KeyStroke.getKeyStroke("control S"));

        addMenuItem(quickEditMenu, new SetSavingsPercentGoalAction(),
                KeyStroke.getKeyStroke("control G"), "P");

        addMenuItem(chartMenu, new RefreshGraphAction(),
                KeyStroke.getKeyStroke("control R"), "R");

        addMenuItem(sourceEdit, new SourceAddAction());
        addMenuItem(sourceEdit, new SourceRemoveAction());
        addMenuItem(debtEdit, new DebtAddAction());
        addMenuItem(debtEdit, new DebtRemoveAction());
        addMenuItem(debtEdit, new DebtPayAction());
    }

    //MODIFIES: this
    //EFFECTS: Adds the lowest layer of menus to the menubar
    private void addInternalMenu() {
        quickEditMenu.add(sourceEdit);
        quickEditMenu.add(debtEdit);
        sourceEdit.setMnemonic('S');
        debtEdit.setMnemonic('D');
    }

    //MODIFIES: this
    //EFFECTS: Adds a new menu item to a JMenu with specified action
    private void addMenuItem(JMenu theMenu, AbstractAction action) {
        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setMnemonic(menuItem.getText().charAt(0));
        theMenu.add(menuItem);
    }

    //MODIFIES: this
    //EFFECTS: Adds a new menu item to a JMenu with specified action, and keystroke shortcut
    private void addMenuItem(JMenu theMenu, AbstractAction action, KeyStroke accelerator) {
        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setMnemonic(menuItem.getText().charAt(0));
        if (accelerator != null) {
            menuItem.setAccelerator(accelerator);
        }

        theMenu.add(menuItem);
    }

    //MODIFIES: this
    //EFFECTS: Adds a new menu item to a JMenu with specified action, keystroke shortcut, and mnemonic
    private void addMenuItem(JMenu theMenu, AbstractAction action, KeyStroke accelerator, String mnemonic) {
        JMenuItem menuItem = new JMenuItem(action);
        menuItem.setMnemonic(menuItem.getText().charAt(0));
        if (accelerator != null) {
            menuItem.setAccelerator(accelerator);
        }

        menuItem.setMnemonic(mnemonic.charAt(0));

        theMenu.add(menuItem);
    }

    //MODIFIES: this
    //EFFECTS: Adds a source to the account's list of sources by prompting the user for required inputs.
    private void addSource() {
        String nameString = parseName("What would you like to set as the name of this source?",
                "Enter name: ");
        if (nameString == null) {
            return;
        }
        String valString = parseVal("What is the monthly balance this source modifies in your account?",
                "Enter value: ");
        if (valString == null) {
            return;
        }

        try {
            userAccount.addSource(nameString, new BigDecimal(valString));
        } catch (NumberFormatException exception) {
            showInvalidInputError("Please only enter numbers for this value.");
        }
        refresh();
    }

    //MODIFIES: this
    //EFFECTS: Removes a source from the account's list of sources by prompting the user for required inputs.
    private void removeSource() {
        String nameString = parseVal("What is the name of the source you'd like to remove?",
                "Enter name: ");
        if (nameString == null) {
            return;
        }
        boolean result = userAccount.removeSource(nameString);

        if (!result) {
            showInvalidInputError("That name is not present in your account's sources.");
        }
        refresh();
    }

    //MODIFIES: this
    //EFFECTS: Centers a window on the screen
    private void centreOnScreen() {
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
        setLocation((width - getWidth()) / 4, ((height - getHeight()) / 3));
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user for an input, and withdraws that from the savings account
    private void withdrawSavings() {
        modifySavings("Please enter the amount you'd like to withdraw.", "Enter withdrawal: ", 1);
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user for an input, and deposits that to the savings account
    private void depositSavings() {
        modifySavings("Please enter the amount you'd like to deposit.", "Enter deposit: ", 0);
    }

    //REQUIRES: command == 0 || command == 1
    //MODIFIES: this
    //EFFECTS: Prompts the user with an input box to deposit or withdraw from a savings account, based on command value
    //          (0 to deposit, 1 to withdraw)
    private void modifySavings(String msg, String titleMsg, int command) {
        BigDecimal val;

        String valString = parseVal(msg,
                titleMsg);
        if (valString == null) {
            return;
        }
        try {
            val = new BigDecimal(valString);
            if (command == 0) {
                userAccount.depositSavings(val);
                setBalanceLabelText(userAccount.getBalance());
            } else if (command == 1) {
                userAccount.withdrawSavings(val);
                setBalanceLabelText(userAccount.getBalance());
            }
        } catch (NumberFormatException exception) {
            showInvalidInputError("Please only enter numbers for this value!");
        }
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user for an input, and withdraws that from the account balance
    private void withdrawBalance() {
        modifyBalance("Please enter the amount you'd like to withdraw.", "Enter withdrawal: ", 1);
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user for an input, and deposits that to the account balance
    private void depositBalance() {
        modifyBalance("Please enter the amount you'd like to deposit", "Enter deposit: ", 0);
    }

    //REQUIRES: command == 0 || command == 1
    //MODIFIES: this
    //EFFECTS: Prompts the user with an input box to deposit or withdraw from an account balance, based on command value
    //          (0 to deposit, 1 to withdraw)
    private void modifyBalance(String msg, String titleMsg, int command) {
        BigDecimal val;

        String valString = parseVal(msg, titleMsg);

        if (valString == null) {
            return;
        }
        try {
            val = new BigDecimal(valString);
            if (command == 0) {
                userAccount.depositBalance(val);
            } else if (command == 1) {
                userAccount.withdrawBalance(val);
            }
        } catch (NumberFormatException exception) {
            showInvalidInputError("Please only enter numbers for this value!");
        }
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user to input a value for interest on an account.
    private void setInterest() {
        BigDecimal intVal;
        String intString = parseVal("What is the monthly interest rate of this debt? (0%-100%)",
                "Enter interest: ");
        if (intString == null) {
            return;
        }
        try {
            intVal = new BigDecimal(intString);
            if (intVal.compareTo(ONE_HUNDRED) > 0) {
                showInvalidInputError("Please enter a value between 0 and 100!");
            } else if (intVal.compareTo(ZERO) < 0) {
                showInvalidInputError("Please enter a value between 0 and 100!");
            } else {
                userAccount.getSavings().setInterest(intVal.movePointLeft(2));
            }
        } catch (NumberFormatException exception) {
            showInvalidInputError("Please only enter numbers for the value!");
        }
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user for a debt account name and value, and subtracts the value from debt account's balance
    private void payDebt() {
        String nameString = parseVal("What is the name of the source you'd like to pay for?",
                "Enter name: ");
        if (nameString == null) {
            return;
        }
        String valString = parseVal("How much would you like to pay?",
                "Enter payment: ");
        if (valString == null) {
            return;
        }

        boolean result = userAccount.payDebt(nameString, new BigDecimal(valString));

        if (!result) {
            showInvalidInputError("That name is not present in your account's sources.");
        }
        refresh();
    }

    //EFFECTS: Displays an input dialog box with given message and initialSelection for a value
    private String parseVal(String message, String initialSelectionValue) {
        return JOptionPane.showInputDialog(desktop,
                message,
                initialSelectionValue);
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user to enter a desired savings percent goal and saves it
    private void setSavingsPercentGoal() {
        BigDecimal spg;

        String spgString = parseName("What would you like to set your savings goal to? (0%-100%)",
                "Enter goal: ");
        if (spgString == null) {
            return;
        }

        try {
            spg = BigDecimal.valueOf(Integer.parseInt(spgString));
            if (spg.compareTo(ONE_HUNDRED) > 0) {
                showInvalidInputError("Please enter a value between 0 and 100!");
            } else if (spg.compareTo(BigDecimal.ZERO) < 0) {
                showInvalidInputError("Please enter a value between 0 and 100!");
            } else {
                userAccount.setSavingsPercentGoal(spg.movePointLeft(2));
            }
        } catch (Exception exception) {
            showInvalidInputError("Please enter a value between 0 and 100!");
        }
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user for a name and attempts to remove it from their list of debt accounts
    private void removeDebt() {
        String nameString = parseVal("What is the name of the source you'd like to remove?",
                "Enter name: ");
        if (nameString == null) {
            return;
        }
        boolean result = userAccount.removeDebt(nameString);

        if (!result) {
            showInvalidInputError("That name is not present in your account's sources.");
        }
        refresh();
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user for a name, value, and attempts to add it to the list of debtaccounts.
    private void addDebt() {
        BigDecimal intVal = null;

        String nameString = parseName("What would you like the name of this debt to be?",
                "Enter name: ");
        if (nameString == null) {
            return;
        }
        String valString = parseVal("What is the current balance of this debt?",
                "Enter balance: ");
        checkForAddDebtErrors(intVal, nameString, valString);
        refresh();
    }

    //MODIFIES: this
    //EFFECTS: Prompts the user for a value for a debt account's interest and checks for errors in the name and value.
    private void checkForAddDebtErrors(BigDecimal intVal, String nameString, String valString) {
        BigDecimal valVal;
        try {
            valVal = new BigDecimal(valString);
            String intString = parseVal("What is the monthly interest rate of this debt? (0%-100%)",
                    "Enter interest: ");
            if (intString == null) {
                return;
            }

            checkForDebtInterestErrors(intVal, nameString, valVal, intString);
        } catch (NumberFormatException exception) {
            showInvalidInputError("Please only enter numbers for the value!");
        }
    }

    //MODIFIES: this
    //EFFECTS: Checks for errors in a value for interest on a debt account and setting it if valid
    private void checkForDebtInterestErrors(BigDecimal intVal, String nameString, BigDecimal valVal, String intString) {
        try {
            intVal = BigDecimal.valueOf(Integer.parseInt(intString));
        } catch (Exception exception) {
            showInvalidInputError("Please enter a value between 0 and 100!");
        }

        if (intVal.compareTo(ONE_HUNDRED) > 0) {
            showInvalidInputError("Please enter a value between 0 and 100!");
        } else if (intVal.compareTo(BigDecimal.ZERO) < 0) {
            showInvalidInputError("Please enter a value between 0 and 100!");
        } else {
            userAccount.addDebt(nameString, valVal,
                    intVal.movePointLeft(2));
        }
    }

    //EFFECTS: Displays an input dialog box with given message and initialSelection for a name
    private String parseName(String message, String title) {
        return JOptionPane.showInputDialog(desktop,
                message,
                title,
                JOptionPane.QUESTION_MESSAGE);
    }

    //EFFECTS: Creates an invalid input error box to show the user when an invalid input has been received.
    private void showInvalidInputError(String message) {
        JOptionPane.showMessageDialog(desktop, message,
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    //MODIFIES: this
    //EFFECTS: Re-sets all the datasets for graphs in the window, as well as re-setting the balance.
    private void refresh() {

        setDataset(graph, createSurplusDataSet());
        setDataset(debts, createDebtFutureDataSet());
        setDataset(savings, createSavingsFutureDataset());
        setBalanceLabelText(userAccount.getBalance());
        pack();
    }

    //EFFECTS: Sets a given ChartPanel's CategoryPlot to the given dataset.
    private void setDataset(ChartPanel savings, CategoryDataset dataset) {
        savings.getChart().getCategoryPlot().setDataset(dataset);
    }

    /**
     * Represents an AbstractAction to save an Account object to a Json file
     */
    private class SaveAction extends AbstractAction {

        //EFFECTS: Creates the save action with name
        SaveAction() {
            super("Save");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            save();
        }

        //EFFECTS: Saves a JSON object to a Json file with the details of an account object
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
    }

    /**
     * Represents an AbstractAction to load an account object from a json file
     */
    private class LoadAction extends AbstractAction {

        //EFFECTS: Creates the load action with name
        LoadAction() {
            super("Load");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            load();
            refresh();
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
    }

    /**
     * Represents a MouseAdapter action that brings the application into focus when user clicks the window
     */
    private class DesktopFocusAction extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            BudgeItUI.this.requestFocusInWindow();
        }
    }

    /**
     * Represents an AbstractAction to set a savings percent goal for an Account
     */
    private class SetSavingsPercentGoalAction extends AbstractAction {

        //EFFECTS: Creates a SavingsPercentGoalAction with name
        SetSavingsPercentGoalAction() {
            super("Savings Percent Goal");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setSavingsPercentGoal();
        }
    }

    /**
     * Abstracts classes that construct an action with the name "Add"
     */
    private abstract static class AddAction extends AbstractAction {

        //EFFECTS: Constructs an AddAction with name "Add"
        AddAction() {
            super("Add");
        }
    }

    /**
     * Abstracts classes that construct an action with the name "Remove"
     */
    private abstract static class RemoveAction extends AbstractAction {

        //EFFECTS: Constructs a RemoveAction with name "Remove"
        RemoveAction() {
            super("Remove");
        }
    }

    /**
     * Represents an AddAction to add sources to an Account
     */
    private class SourceAddAction extends AddAction {

        //EFFECTS: Constructs a SourceAddAction
        SourceAddAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addSource();
        }
    }

    /**
     * Represents an AddAction to add debtAcc objects to an Account
     */
    private class DebtAddAction extends AddAction {

        //EFFECTS: Constructs a DebtAddAction
        DebtAddAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addDebt();
        }
    }

    /**
     * Represents a RemoveAction to remove Sources from an Account
     */
    private class SourceRemoveAction extends RemoveAction {

        //EFFECTS: Constructs a SourceRemoveAction
        SourceRemoveAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            removeSource();
        }
    }

    /**
     * Represents a DebtRemoveAction to remove debtAcc objects from an Account
     */
    private class DebtRemoveAction extends RemoveAction {

        //EFFECTS: Constructs a DebtRemoveAction
        DebtRemoveAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            removeDebt();
        }
    }

    /**
     * Represents an AbstractAction to refresh the JFrame objects on screen
     */
    private class RefreshGraphAction extends AbstractAction {
        RefreshGraphAction() {
            super("Refresh");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            refresh();
        }
    }

    /**
     * Represents an object containing buttons for modifying the Source objects in an Account
     */
    private class SourceButtons extends JPanel implements ActionListener {

        //EFFECTS: Creates a SourceButtons object with correct layout and buttons
        public SourceButtons() {
            this.setLayout(new FlowLayout());

            JButton add = new JButton("Add");
            JButton remove = new JButton("Remove");

            add(add);
            add(remove);

            add.addActionListener(this);
            remove.addActionListener(this);

            add(add);
            add(remove);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();

            if (action.equals("Add")) {
                addSource();
            } else if (action.equals("Remove")) {
                removeSource();
            }
        }
    }

    /**
     * Represents an object containing buttons for modifying the debtAcc objects in an Account
     */
    private class DebtButtons extends JPanel implements ActionListener {

        //EFFECTS: Creates the DebtButtons panel and adds all necessary functionality.
        public DebtButtons() {
            this.setLayout(new FlowLayout());

            JButton add = new JButton("Add");
            JButton remove = new JButton("Remove");
            JButton pay = new JButton("Pay Balance");

            add(add);
            add(remove);
            add(pay);

            add.addActionListener(this);
            remove.addActionListener(this);
            pay.addActionListener(this);

            add.setActionCommand("Add");
            remove.setActionCommand("Remove");
            pay.setActionCommand("Pay");

            add(add);
            add(remove);
            add(pay);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();

            switch (action) {
                case "Add":
                    addDebt();
                    break;
                case "Remove":
                    removeDebt();
                    break;
                case "Pay":
                    payDebt();
                    break;
            }
        }
    }

    /**
     * Represents an AbstractAction to pay a value from a debtAcc in an Account
     */
    private class DebtPayAction extends AbstractAction {
        public DebtPayAction() {
            super("Pay");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            payDebt();
        }
    }

    /**
     * Represents an object containing buttons for modifying the SavingsAcc object in an Account
     */
    private class SavingsButtons extends JPanel implements ActionListener {

        //EFFECTS: Constructs the button panel with all functionality
        public SavingsButtons() {
            this.setLayout(new FlowLayout());

            JButton deposit = new JButton("Deposit");
            JButton withdraw = new JButton("Withdraw");
            JButton spg = new JButton("Set Savings Goal");
            JButton interest = new JButton("Set Interest Rate");

            add(deposit);
            add(withdraw);
            add(spg);
            add(interest);

            deposit.addActionListener(this);
            withdraw.addActionListener(this);
            spg.addActionListener(this);
            interest.addActionListener(this);

            deposit.setActionCommand("Deposit");
            withdraw.setActionCommand("Withdraw");
            spg.setActionCommand("SPG");
            interest.setActionCommand("Interest");

            add(deposit);
            add(withdraw);
            add(spg);
            add(interest);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();

            switch (action) {
                case "Deposit":
                    depositSavings();
                    break;
                case "Withdraw":
                    withdrawSavings();
                    break;
                case "SPG":
                    setSavingsPercentGoal();
                    break;
                case "Interest":
                    setInterest();
                    break;
            }
            refresh();
        }
    }

    /**
     * Represents an object containing buttons for modifying the balance of an Account
     */
    private class BalanceButtons extends JPanel implements ActionListener {

        //EFFECTS: Constructs the Balance panel with the current balance, and two modification buttons
        public BalanceButtons() {
            this.setLayout(new GridLayout());

            balanceLabel = new JLabel("Balance: " + 0);
            JButton deposit = new JButton("Deposit");
            JButton withdraw = new JButton("Withdraw");

            add(balanceLabel, this.getLayout());
            add(deposit);
            add(withdraw);

            deposit.addActionListener(this);
            withdraw.addActionListener(this);

            deposit.setActionCommand("Deposit");
            withdraw.setActionCommand("Withdraw");

            add(balanceLabel);
            add(deposit);
            add(withdraw);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String action = e.getActionCommand();

            if (action.equals("Deposit")) {
                depositBalance();
            } else if (action.equals("Withdraw")) {
                withdrawBalance();
            }
            setBalanceLabelText(userAccount.getBalance());
            refresh();
        }
    }
}
