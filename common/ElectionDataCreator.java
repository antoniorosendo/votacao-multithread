package common;

import common.ElectionData;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ElectionDataCreator {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("     Election Data Creator");
        System.out.println();
    
        System.out.println("Enter the election question:");
        String question = scanner.nextLine().trim();
        
        if (question.isEmpty()) {
            System.err.println("Error: Question cannot be empty!");
            scanner.close();
            return;
        }
        
       
        System.out.println();
        System.out.println("How many voting options?");
        
        int numOptions;
        try {
            numOptions = Integer.parseInt(scanner.nextLine().trim());
            if (numOptions < 2) {
                System.err.println("Error: Must have at least 2 options!");
                scanner.close();
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid number!");
            scanner.close();
            return;
        }
        
        System.out.println();
        System.out.println("Enter the " + numOptions + " voting options:");
        String[] options = new String[numOptions];
        
        for (int i = 0; i < numOptions; i++) {
            System.out.print((i + 1) + ". ");
            options[i] = scanner.nextLine().trim();
            
            if (options[i].isEmpty()) {
                System.err.println("Error: Option cannot be empty!");
                scanner.close();
                return;
            }
        }
        
        // Get filename
        System.out.println();
        System.out.println("Enter filename (without .ser extension):");
   
        String filename = scanner.nextLine().trim();
        
        if (filename.isEmpty()) {
            filename = "election_" + System.currentTimeMillis();
        }
        
        if (!filename.endsWith(".ser")) {
            filename += ".ser";
        }
        
     
        List<String> optionsList = Arrays.asList(options);
        ElectionData electionData = new ElectionData(question, optionsList);
        
        // Save to file
        try {
            saveElectionData(electionData, filename);
            
            System.out.println();
            System.out.println("✓ Election data saved successfully!");
            System.out.println("File: " + filename);
            System.out.println("Question: " + question);
            System.out.println("Options: " + numOptions);
            System.out.println();
            System.out.println("You can now load this file in the server.");
            
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
        
        scanner.close();
    }
    
    private static void saveElectionData(ElectionData electionData, String filename) 
            throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(filename))) {
            oos.writeObject(electionData);
        }
    }
   
}