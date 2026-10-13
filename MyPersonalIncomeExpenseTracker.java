import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class c {
    private static final Scanner sc = new Scanner(System.in);
    private static final List<Entry> entries = new ArrayList<>();
    private static int nextId = 1;
    private static double monthlyBudget = 0.0;
    private static double savingsGoal = 0.0;

    static class Entry {
        private final int id;
        private final LocalDate date;
        private final String type;       // INCOME or EXPENSE
        private final String category;
        private final String note;
        private final double amount;

        Entry(int id, LocalDate date, String type, String category, String note, double amount) {
            this.id = id;
            this.date = date;
            this.type = type;
            this.category = category;
            this.note = note;
            this.amount = amount;
        }

        int getId() { return id; }
        LocalDate getDate() { return date; }
        String getType() { return type; }
        String getCategory() { return category; }
        String getNote() { return note; }
        double getAmount() { return amount; }

        void display() {
            System.out.printf(Locale.US, "#%d | %s | %-7s | %-15s | Rs. %10.2f | %s%n",
                    id, date, type, category, amount, note);
        }
    }

    public static void main(String[] args) {
        int choice = 0;
        while (choice != 9) {
            System.out.println("\\n========== MY PERSONAL MONEY TRACKER ==========");
            System.out.println("1. Add income");
            System.out.println("2. Add expense");
            System.out.println("3. View all records");
            System.out.println("4. Where did my money go? (category report)");
            System.out.println("5. Savings and monthly summary");
            System.out.println("6. Set monthly expense budget");
            System.out.println("7. Set savings goal");
            System.out.println("8. Delete a record");
            System.out.println("9. Exit");
            try {
                choice = readInt("Choose: ");
                switch (choice) {
                    case 1 -> addEntry("INCOME");
                    case 2 -> addEntry("EXPENSE");
                    case 3 -> viewAll();
                    case 4 -> categoryReport();
                    case 5 -> savingsSummary();
                    case 6 -> setMonthlyBudget();
                    case 7 -> setSavingsGoal();
                    case 8 -> deleteEntry();
                    case 9 -> System.out.println("Your records are finished for this session. Bye!");
                    default -> System.out.println("Please choose a number from 1 to 9.");
                }
            } catch (InputMismatchException ex) {
                System.out.println("Please enter a valid number.");
                sc.nextLine();
            } catch (IllegalArgumentException ex) {
                System.out.println("Input issue: " + ex.getMessage());
            }
        }
        sc.close();
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        int value = sc.nextInt();
        sc.nextLine();
        return value;
    }

    private static double readAmount(String prompt) {
        System.out.print(prompt);
        double value = sc.nextDouble();
        sc.nextLine();
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
        return value;
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        String value = sc.nextLine().trim();
        if (value.isEmpty()) throw new IllegalArgumentException("This field cannot be empty.");
        return value;
    }

    private static LocalDate readDate() {
        System.out.print("Date (YYYY-MM-DD, e.g. 2026-10-12): ");
        String text = sc.nextLine().trim();
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Use a valid date in YYYY-MM-DD format.");
        }
    }

    private static String chooseCategory(String type) {
        String[] categories = type.equals("INCOME")
                ? new String[]{"Pocket Money", "Salary/Part-time", "Scholarship", "Gift", "Other"}
                : new String[]{"Food", "Transport", "Education", "Shopping", "Entertainment",
                               "Bills/Recharge", "Health", "Rent/Hostel", "Travel", "Other"};
        System.out.println("Categories: " + String.join(" | ", categories));
        String chosen = readText("Category: ");
        for (String category : categories) {
            if (category.equalsIgnoreCase(chosen)) return category;
        }
        throw new IllegalArgumentException("Choose a category exactly from the list above.");
    }

    private static void addEntry(String type) {
        System.out.println("\\n--- ADD " + type + " ---");
        LocalDate date = readDate();
        String category = chooseCategory(type);
        String note = readText("Short note (e.g. lunch, bus pass, pocket money): ");
        double amount = readAmount("Amount in rupees: ");
        entries.add(new Entry(nextId++, date, type, category, note, amount));
        System.out.println(type + " saved successfully.");
    }

    private static void viewAll() {
        if (entries.isEmpty()) {
            System.out.println("No records yet. Add your income and expenses first.");
            return;
        }
        System.out.println("\\nID | DATE       | TYPE    | CATEGORY        | AMOUNT      | NOTE");
        System.out.println("--------------------------------------------------------------------------");
        entries.stream().sorted(Comparator.comparing(Entry::getDate))
                .forEach(Entry::display);
    }

    private static void categoryReport() {
        LocalDate now = LocalDate.now();
        Map<String, Double> spending = entries.stream()
                .filter(e -> e.getType().equals("EXPENSE")
                        && e.getDate().getYear() == now.getYear()
                        && e.getDate().getMonthValue() == now.getMonthValue())
                .collect(Collectors.groupingBy(Entry::getCategory, TreeMap::new,
                        Collectors.summingDouble(Entry::getAmount)));

        double total = spending.values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("\\n--- WHERE YOUR MONEY WENT: THIS MONTH ---");
        if (spending.isEmpty()) {
            System.out.println("No expenses recorded for this month.");
            return;
        }
        for (Map.Entry<String, Double> item : spending.entrySet()) {
            double percent = total == 0 ? 0 : item.getValue() * 100 / total;
            System.out.printf(Locale.US, "%-18s Rs. %9.2f  (%5.1f%%)%n",
                    item.getKey(), item.getValue(), percent);
        }
        System.out.printf(Locale.US, "TOTAL SPENT: Rs. %.2f%n", total);
        System.out.println("Tip: Check the categories with the highest percentage first.");
    }

    private static void savingsSummary() {
        LocalDate now = LocalDate.now();
        double income = entries.stream()
                .filter(e -> e.getType().equals("INCOME")
                        && e.getDate().getYear() == now.getYear()
                        && e.getDate().getMonthValue() == now.getMonthValue())
                .mapToDouble(Entry::getAmount).sum();
        double expense = entries.stream()
                .filter(e -> e.getType().equals("EXPENSE")
                        && e.getDate().getYear() == now.getYear()
                        && e.getDate().getMonthValue() == now.getMonthValue())
                .mapToDouble(Entry::getAmount).sum();
        double savings = income - expense;
        double savingsRate = income == 0 ? 0 : savings * 100 / income;

        System.out.println("\\n--- THIS MONTH'S SAVINGS SUMMARY ---");
        System.out.printf(Locale.US, "Income recorded : Rs. %.2f%n", income);
        System.out.printf(Locale.US, "Expenses        : Rs. %.2f%n", expense);
        System.out.printf(Locale.US, "Money left      : Rs. %.2f%n", savings);
        System.out.printf(Locale.US, "Savings rate    : %.1f%%%n", savingsRate);

        if (monthlyBudget > 0) {
            System.out.printf(Locale.US, "Expense budget  : Rs. %.2f%n", monthlyBudget);
            System.out.printf(Locale.US, "Budget remaining: Rs. %.2f%n", monthlyBudget - expense);
            if (expense > monthlyBudget) {
                System.out.println("ALERT: You exceeded your monthly expense budget.");
            } else {
                System.out.println("Budget status   : Within budget.");
            }
        }
        if (savingsGoal > 0) {
            System.out.printf(Locale.US, "Savings goal    : Rs. %.2f%n", savingsGoal);
            System.out.printf(Locale.US, "Goal progress   : %.1f%%%n",
                    Math.max(0, Math.min(100, savings / savingsGoal * 100)));
        }

        System.out.println("\\nSUGGESTION:");
        if (income == 0) {
            System.out.println("Add this month's income records to get useful savings suggestions.");
        } else if (savings < 0) {
            System.out.println("You spent more than your recorded income. Review non-essential expenses.");
        } else if (savingsRate < 10) {
            System.out.println("Less than 10% of recorded income remains. Try reducing one non-essential category.");
        } else if (savingsRate < 20) {
            System.out.println("Good start. Try moving a little more money to savings when you receive income.");
        } else {
            System.out.println("Nice work! You have kept at least 20% of recorded income. Keep it consistent.");
        }
        System.out.println("Savings here means recorded income minus recorded expenses; it is not a bank balance.");
    }

    private static void setMonthlyBudget() {
        monthlyBudget = readAmount("Set maximum expense budget for this month (Rs.): ");
        System.out.printf(Locale.US, "Monthly expense budget set to Rs. %.2f%n", monthlyBudget);
    }

    private static void setSavingsGoal() {
        savingsGoal = readAmount("Set your savings goal for this month (Rs.): ");
        System.out.printf(Locale.US, "Monthly savings goal set to Rs. %.2f%n", savingsGoal);
    }

    private static void deleteEntry() {
        int id = readInt("Record ID to delete: ");
        boolean removed = entries.removeIf(e -> e.getId() == id);
        System.out.println(removed ? "Record deleted." : "Record ID not found.");
    }
}