import java.sql.*;
import java.util.Scanner;

public class HospitalManagement {
    // JDBC URL, username and password of MySQL server
    private static final String url = "jdbc:mysql://localhost:3306/hospitaldb";
    private static final String user = "root";
    private static final String password = "soumyadeep";

    // Scanner for input
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===== Hospital Management System =====\n");

        // Check connection before starting operations
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connected to database successfully.\n");
            boolean exit = false;
            while (!exit) {
                System.out.println("Choose an option:");
                System.out.println("1. Add Patient");
                System.out.println("2. Display All Patients");
                System.out.println("3. Update Patient");
                System.out.println("4. Delete Patient");
                System.out.println("5. Exit");
                System.out.print("Your choice: ");

                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        addPatient(conn);
                        break;
                    case "2":
                        displayPatients(conn);
                        break;
                    case "3":
                        updatePatient(conn);
                        break;
                    case "4":
                        deletePatient(conn);
                        break;
                    case "5":
                        exit = true;
                        System.out.println("Exiting... Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter 1-5.");
                }
                System.out.println();
            }
        } catch (SQLException e) {
            System.err.println("Error connecting to the database. Please check your credentials and database status.");
            e.printStackTrace();
        }
    }

    private static void addPatient(Connection conn) {
        try {
            System.out.println("\n--- Add New Patient ---");
            System.out.print("Enter name: ");
            String name = scanner.nextLine();

            System.out.print("Enter age: ");
            int age = Integer.parseInt(scanner.nextLine());

            System.out.print("Enter address: ");
            String address = scanner.nextLine();

            System.out.print("Enter phone number: ");
            String phone = scanner.nextLine();

            String sql = "INSERT INTO patients (name, age, address, phone) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, address);
                pstmt.setString(4, phone);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Patient added successfully.");
                } else {
                    System.out.println("Failed to add patient.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding patient: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for age. Operation cancelled.");
        }
    }

    private static void displayPatients(Connection conn) {
        System.out.println("\n--- Patient List ---");
        String sql = "SELECT * FROM patients";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.printf("%-5s %-20s %-5s %-30s %-15s%n", "ID", "Name", "Age", "Address", "Phone");
            System.out.println("---------------------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String address = rs.getString("address");
                String phone = rs.getString("phone");

                System.out.printf("%-5d %-20s %-5d %-30s %-15s%n", id, name, age, address, phone);
            }
            if (!found) {
                System.out.println("No patient records found.");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patients: " + e.getMessage());
        }
    }

    private static void updatePatient(Connection conn) {
        try {
            System.out.println("\n--- Update Patient ---");
            System.out.print("Enter patient ID to update: ");
            int id = Integer.parseInt(scanner.nextLine());

            // Check if patient exists
            String checkSql = "SELECT * FROM patients WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Patient with ID " + id + " not found.");
                        return;
                    }
                }
            }

            System.out.print("Enter new name (leave blank to keep unchanged): ");
            String name = scanner.nextLine();

            System.out.print("Enter new age (leave blank to keep unchanged): ");
            String ageStr = scanner.nextLine();

            System.out.print("Enter new address (leave blank to keep unchanged): ");
            String address = scanner.nextLine();

            System.out.print("Enter new phone number (leave blank to keep unchanged): ");
            String phone = scanner.nextLine();

            StringBuilder sqlBuilder = new StringBuilder("UPDATE patients SET ");
            boolean firstField = true;
            if (!name.isEmpty()) {
                sqlBuilder.append("name = ?");
                firstField = false;
            }
            if (!ageStr.isEmpty()) {
                if (!firstField) sqlBuilder.append(", ");
                sqlBuilder.append("age = ?");
                firstField = false;
            }
            if (!address.isEmpty()) {
                if (!firstField) sqlBuilder.append(", ");
                sqlBuilder.append("address = ?");
                firstField = false;
            }
            if (!phone.isEmpty()) {
                if (!firstField) sqlBuilder.append(", ");
                sqlBuilder.append("phone = ?");
            }
            sqlBuilder.append(" WHERE id = ?");

            String sql = sqlBuilder.toString();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                if (!name.isEmpty()) {
                    pstmt.setString(paramIndex++, name);
                }
                if (!ageStr.isEmpty()) {
                    int age = Integer.parseInt(ageStr);
                    pstmt.setInt(paramIndex++, age);
                }
                if (!address.isEmpty()) {
                    pstmt.setString(paramIndex++, address);
                }
                if (!phone.isEmpty()) {
                    pstmt.setString(paramIndex++, phone);
                }
                pstmt.setInt(paramIndex, id);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Patient updated successfully.");
                } else {
                    System.out.println("Failed to update patient.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for age or ID. Operation cancelled.");
        }
    }

    private static void deletePatient(Connection conn) {
        try {
            System.out.println("\n--- Delete Patient ---");
            System.out.print("Enter patient ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine());

            // Confirm existence
            String checkSql = "SELECT * FROM patients WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, id);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Patient with ID " + id + " not found.");
                        return;
                    }
                }
            }

            System.out.print("Are you sure you want to delete patient with ID " + id + "? (y/n): ");
            String confirmation = scanner.nextLine();
            if (!confirmation.equalsIgnoreCase("y")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            String sql = "DELETE FROM patients WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Patient deleted successfully.");
                } else {
                    System.out.println("Failed to delete patient.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input for ID. Operation cancelled.");
        }
    }
}



