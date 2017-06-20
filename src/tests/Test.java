package tests;

import system.core.database.BSDb;
import system.shell.BSShell;

import java.sql.Connection;
import java.util.Scanner;

public class Test {
    public static void main (String[] args) {
        try {
            System.out.println("-------------------------------------");
            System.out.println("Banking System shell.");
            System.out.println("-------------------------------------");
            // Connection для теста
            Connection connection = BSDb.getConnection(
                    "knack",
                    "927360",
                    "localhost",
                    1433,
                    "banking_system"
            );
            Scanner sc = new Scanner(System.in);
            String temp;
            while ((temp = sc.nextLine()).length() > 0) {
                System.out.println(BSShell.getDataByCommand(temp, connection));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
