package banking;
import java.sql.Connection;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static Status status;
    private static String creditCardNumber = "";
    private static String creditCardPin = "";
    private static int balance = 0;
    private static int id = 0;
    private static String url;
    private static Scanner scanner;

    public static void setStatus (Status newStatus) {
        status = newStatus;
    }

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        String fileName = args[1];
        String tableName = "card";
        url = "jdbc:sqlite:" + fileName;
        //System.out.println("URL for database is: " + url);
        //create database
        createNewDatabase();
        //create table
        //drop table
        //dropTable(tableName);
        createNewTable(tableName);
        readAllTableData(tableName);
        //System.out.println("Global id is: " + id);
        status = Status.MAIN;
        while (true) {
            System.out.println("Current status is " + status);
            checkStatus(status);
            if (status == Status.EXIT) {
                System.out.println("Bye!");
                break;
            }
        }
        //processingResultSet("SELECT * FROM card");
    }

    private static void updateId () {
        String sql = "SELECT * FROM card";
        int maxId = 0;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    // Retrieve column values
                    int currId = resultSet.getInt("id");
                    if(currId > maxId) {
                        maxId = currId;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        id = maxId + 1;
    }

    public static void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void createNewTable(String tableName) {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(\n"
                + "	id integer PRIMARY KEY,\n"
                + "	number text NOT NULL,\n"
                + "	pin text,\n"
                + " balance INTEGER DEFAULT 0\n"
                + ");";

        connectAndExecuteStatement(sql);
    }

    private static void dropTable(String tableName) {
        //sql statement for droping table
        String sqlDropTable = "DROP TABLE IF EXISTS card";

        connectAndExecuteStatement(sqlDropTable);
    }


    //most important helper method one who connects to database and execute given statement

    private static void connectAndExecuteStatement(String sql) {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // statement execution
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static void processingResultSet(String sql) {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    // Retrieve column values
                    int id = resultSet.getInt("id");
                    String cardNumber = resultSet.getString("number");
                    String cardPin = resultSet.getString("pin");
                    int cardBalance = resultSet.getInt("balance");
                    System.out.printf("Card ID %d%n", id);
                    System.out.printf("\tCard Number: %s%n", cardNumber);
                    System.out.printf("\tCard PIN: %s%n", cardPin);
                    System.out.printf("\tCard Balance: %d%n", cardBalance);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private static boolean checkForCardNumberInDatabase(String sql, String transferCardNumber) {
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    // Retrieve column values
                    String cardNumber = resultSet.getString("number");
                    if(cardNumber.equals(transferCardNumber)) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private static void insertAccountData(String cardNumber, String cardPin, int balance) {
        //System.out.println("Old ID is " + id);
        updateId();
        //System.out.println("Updated ID is " + id);
        //System.out.println("PIN while inserting data is " + cardPin);
        String sql = "INSERT INTO card (id, number, pin, balance) " +
                "VALUES (" + id + "," +  cardNumber + ","  + cardPin + "," + balance +")";
        //System.out.println("Full sql statement is: " + sql);
        connectAndExecuteStatement(sql);
        //processingResultSet("SELECT * FROM card");
    }

    private static void readAllTableData(String tableName) {
        String readAllData = "SELECT * FROM " + tableName;
        processingResultSet(readAllData);
    }

    private static void checkStatus(Status status) {
        if (status == Status.MAIN) { //status MAIN
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            String input = scanner.nextLine();
            checkMainInput(input);
        } else if (status == Status.LOGGING) { //STATUS LOGGING
            System.out.println("Enter your card number:");
            String attemptCreditCardNumber = scanner.nextLine();
            System.out.println("Enter your PIN:");
            String attemptCardPin = scanner.nextLine();
            if (matchCardAndPin(attemptCreditCardNumber, attemptCardPin)){
                System.out.println("You have successfully logged in!" + "\n");
                setStatus(Status.LOGGEDIN);
                System.out.println(status);
            } else {
                System.out.println("Wrong card number or PIN!" + "\n");
                setStatus(Status.MAIN);
                System.out.println(status);
            }
        } else if (status == Status.LOGGEDIN) { //STATUS LOGGEDIN
            System.out.println("1. Balance");
            System.out.println("2. Add income");
            System.out.println("3. Do transfer");
            System.out.println("4. Close account");
            System.out.println("5. Log out");
            System.out.println("0. Exit");
            String loggedinInput = scanner.nextLine();
            checkLoggedInInput(loggedinInput);
        } else if (status == Status.EXIT) { //STATUS EXIT
            //System.out.println("Bye!");
        }
    }

    private static void checkLoggedInInput(String loggedinInput) {
        if (loggedinInput.equals("1")) {
            //System.out.println(balance);
            readBalanceFromDatabase();
            setStatus(Status.LOGGEDIN);
        } else if (loggedinInput.equals("2")) {
            addIncome();
        } else if (loggedinInput.equals("3")) {
            System.out.println("Transfer");
            doTransfer();
            setStatus(Status.LOGGEDIN);
        } else if (loggedinInput.equals("4")) {
            closeAccount();
            System.out.println("The account has been closed!");
            setStatus(Status.MAIN);
        } else if (loggedinInput.equals("5")) {
            System.out.println("You have successfully logged out!");
            creditCardNumber = "";
            creditCardPin = "";
            setStatus(Status.MAIN);
        } else if (loggedinInput.equals("0")) {
            setStatus(Status.EXIT);
        }
    }

    private static void addIncome() {
        System.out.println("Enter income:");
        int income = Integer.parseInt(scanner.nextLine());
        addIncomeToDatabase(income);
        System.out.println("Income was added!");
        setStatus(Status.LOGGEDIN);
    }

    private static void doTransfer() {
        String readAllData = "SELECT * FROM card";
        //System.out.println("Transfer");
        System.out.println("Enter card number:");
        String transferToCardNumber = scanner.nextLine();
        if(checkLuhn(transferToCardNumber)) {
            //System.out.println("Luhn successfully checked!!!");
            if(checkForCardNumberInDatabase(readAllData, transferToCardNumber)) {
                if (!isSameCardNumber(transferToCardNumber)) {
                    System.out.println("Enter how much money you want to transfer:");
                    int transferAmount = scanner.nextInt();
                    if(checkBalance(transferAmount)) {
                        String tempCardNumber = creditCardNumber;
                        addIncomeToDatabase(-transferAmount);
                        creditCardNumber = transferToCardNumber;
                        addIncomeToDatabase(transferAmount);
                        creditCardNumber = tempCardNumber;
                    } else {
                        System.out.println("Not enough money!");
                    }
                } else {
                    System.out.println("You can't transfer money to the same account!");
                }
            } else {
                System.out.println("Such a card does not exist.");
            }
        } else {
            System.out.println("Probably you made mistake in the card number. Please try again!");
        }
        //setStatus(Status.LOGGEDIN);
    }

    private static boolean isSameCardNumber(String transferToCardNumber) {
        return creditCardNumber.equals(transferToCardNumber);
    }

    private static boolean checkLuhn(String cardNumber) {
        if (cardNumber.length() == 16) {
            int [] numbers = new int[16];
            int sum = 0;
            for (int i = 0; i < 16; i++) {
                String ch = String.valueOf(cardNumber.charAt(i));
                int currNum = Integer.parseInt(ch);
                if (i % 2 == 0) {
                    currNum *= 2;
                }
                //System.out.println("Curr digit multiplied by is: " + currNum);
                if(currNum > 9) {
                    currNum -= 9;
                }
                sum += currNum;
            }
            //System.out.println("Sum of digits is: " + sum);
            if (sum % 10 == 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkBalance(int transferAmount) {
        return balance >= transferAmount;
    }

    private static boolean isCardValid(String ccNum, String ccPin) {
        //old solution without database
        //return (ccNum.equals(creditCardNumber) && ccPin.equals(creditCardPin));
        return false;
    }

    private static boolean matchCardAndPin(String ccNum, String ccPin) {
        String sql = "SELECT * FROM card";
        int ccPinInt = Integer.parseInt(ccPin);
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    // Retrieve column values
                    int id = resultSet.getInt("id");
                    String currCardNumber = resultSet.getString("number");
                    String currCardPin = resultSet.getString("pin");
                    int currCardBalance = resultSet.getInt("balance");
                    int currCardPinInt = Integer.parseInt(currCardPin);
                    //System.out.println("Current card number is " + currCardNumber);
                    //System.out.println("Current PIN is " + currCardPin);
                    if (ccNum.equals(currCardNumber) && ccPinInt == currCardPinInt) {
                        creditCardNumber = ccNum;
                        creditCardPin = ccPin;
                        balance = currCardBalance;
                        System.out.println("It is true!");
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private static void addIncomeToDatabase(int amount) {
        String sql = "UPDATE card SET balance = balance + "
                + amount + " WHERE number = " + creditCardNumber;
        System.out.println(sql);
        connectAndExecuteStatement(sql);
        readBalanceFromDatabase();
    }

    private static void readBalanceFromDatabase() {
        String sql = "SELECT number, balance FROM card " +
                "WHERE number = " + creditCardNumber;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            try (ResultSet resultSet = stmt.executeQuery(sql)) {
                while (resultSet.next()) {
                    // Retrieve column values
                    balance = resultSet.getInt("balance");
                    System.out.println("Balance is: " + balance);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void checkMainInput(String input) {
        if (input.equals("1")) {
            System.out.println(createNewAccount());
            status = Status.MAIN;
        } else if (input.equals("2")) {
            status = Status.LOGGING;
        } else if (input.equals("0")) {
            status = Status.EXIT;
        }
    }

    private static String createNewAccount() {
        String account;
        String newCardNumber = createNewCardNumber();
        String newCardPin = createNewCardPin();
        System.out.println("Adding account do database PIN is " + newCardPin);
        balance = 0;
        insertAccountData(newCardNumber, newCardPin, balance);
        //increment id by one;
        StringBuilder sb = new StringBuilder();
        sb.append("Your card number has been created" + "\n");
        sb.append("Your card number:" + "\n");
        sb.append(newCardNumber + "\n");
        sb.append("Your card PIN:" + "\n");
        sb.append(newCardPin);
        account = sb.toString();
        //System.out.println("id number is " + id);
        //id++;
        return account;
    }

    private static String createNewCardPin() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        System.out.println("From create new card PIN method PIN is " + sb.toString());
        return sb.toString();
    }

    private static String createNewCardNumber() {
        Random random = new Random();
        int sumExeceptLast = 4;
        StringBuilder sb = new StringBuilder("4");
        //dodavanje prvih 5 cifara posle cetvorke
        //for(int i = 0; i < 5; i++) {
        //    sb.append(random.nextInt(10));
        //}
        sb.append("00000");
        //dodavanje preostalih cifara do 16 cifara
        for (int i = 0; i < 9; i ++ ){
            int currDigit = random.nextInt(10);
            sumExeceptLast += currDigit;
            sb.append(currDigit);
        }
        sb.append(findLastDigitByLuhnAlgorithm(sb.toString()));
        creditCardNumber = sb.toString();
        return sb.toString();
    }

    private static String findLastDigitByLuhnAlgorithm(String first15Digits) {
        int[] digits = new int[15];
        int sum = 0;
        int lastDigit = 0;
        for (int i = 0; i < 15; i++) {
            int currDigit = Integer.parseInt(String.valueOf(first15Digits.charAt(i)));
            if (i % 2 == 0) {
                currDigit *= 2;
            }
            digits[i] = currDigit;

        }
        for (int j = 0; j < digits.length; j++) {
            if (digits[j] > 9) {
                digits[j] -= 9;
            }
            sum += digits[j];
        }
        for (int k = 0; k < 10; k++) {
            if ((sum + k) % 10 == 0) {
                lastDigit = k;
            }
        }
        return String.valueOf(lastDigit);
    }

    private static void closeAccount() {
        String sqlDeleteAccount = "DELETE FROM card WHERE number = " + creditCardNumber;
        connectAndExecuteStatement(sqlDeleteAccount);
    }
}



enum Status {
    MAIN,
    LOGGING,
    LOGGEDIN,
    EXIT
}
