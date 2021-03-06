package gui;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import customers.Customer;
import customers.CustomerDatabase;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by mbala on 24.05.17.
 */

public class FileHandler {

    private final String separator = ";";
    private final double defaultPackageWeight = 0.0;
    private final double defaultPackageCapacity = 0.0;
    private final String defaultMinDeliveryHour = "08:00";
    private final String defaultMaxDeliveryHour = "18:00";
    private final String defaultServiceTime = "00:15";

    public FileHandler() {

    }

    public File chooseFile(MyWindow parentWindow) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        //fileChooser.setFileFilter(new FileNameExtensionFilter(".csv", "csv"));
        //fileChooser.setFileFilter(new FileNameExtensionFilter(".txt", "txt"));
        int result = fileChooser.showOpenDialog(parentWindow);
        File selectedFile = null;
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("File is not selected.");
        }
        return selectedFile;
    }

    public void readFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            CustomerDatabase.getCustomerList().clear();
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                String[] fields = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, separator);

                String name = fields[0];
                double lat;
                double lon;

                if (NumberUtils.isParsable(fields[1]) && NumberUtils.isParsable(fields[2])) {
                    lat = Double.parseDouble(fields[1]);
                    lon = Double.parseDouble(fields[2]);
                    if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                        System.out.println("Coordinates are out of range in line " + lineNumber);
                        continue;
                    }
                } else {
                    System.out.println("Cannot parse coordinates in line " + lineNumber);
                    continue;
                }

                double weight = defaultPackageWeight;
                if (NumberUtils.isParsable(fields[3])) {
                    weight = Double.parseDouble(fields[3]);
                }

                double capacity = defaultPackageCapacity;
                if (NumberUtils.isParsable(fields[4])) {
                    capacity = Double.parseDouble(fields[4]);
                }

                String minDeliveryHour = fields[5];
                String maxDeliveryHour = fields[6];
                try {
                    DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    Date begin = dateFormat.parse(minDeliveryHour);
                    Date end = dateFormat.parse(maxDeliveryHour);

                    if (begin.before(dateFormat.parse(defaultMinDeliveryHour))) {
                        minDeliveryHour = defaultMinDeliveryHour;
                    }
                    if (end.after(dateFormat.parse(defaultMaxDeliveryHour))) {
                        maxDeliveryHour = defaultMaxDeliveryHour;
                    }

                } catch (ParseException e) {
                    minDeliveryHour = defaultMinDeliveryHour;
                    maxDeliveryHour = defaultMaxDeliveryHour;
                    System.out.println("Cannot parse delivery hours in line " + lineNumber + "! Delivery hours set for 08:00-18:00.");
                }

                Customer customer = new Customer(name, lat, lon, weight, capacity, minDeliveryHour, maxDeliveryHour);
                CustomerDatabase.getCustomerList().add(customer);
                System.out.println("ID: " + customer.getId()
                        + ", Nazwa: " + customer.getName()
                        + ", Szer: " + customer.getLatitude()
                        + ", Dl: " + customer.getLongitude()
                        + ", Masa: " + customer.getPackageWeight()
                        + ", Objetosc: " + customer.getPackageCapacity()
                        + ", Okno czasowe: " + customer.getMinDeliveryHour().toString() + "-" + customer.getMaxDeliveryHour().toString());
            }
        } catch (IOException e) {
            System.out.println("Unexpected error while reading the file.");
        }
    }
}
