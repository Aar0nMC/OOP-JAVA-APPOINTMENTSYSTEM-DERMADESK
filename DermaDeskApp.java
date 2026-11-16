import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DermaDeskApp extends JFrame {
    private final Color NAVY = new Color(35, 48, 68);
    private final Color TEAL = new Color(65, 145, 140);
    private final List<Service> services = new ArrayList<>();
    private final List<Appointment> appointments = new ArrayList<>();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    private JTextField customerNameField, dateField, timeField, customerSearchField;
    private JComboBox<String> serviceCombo;
    private DefaultTableModel customerModel, staffModel, serviceModel;
    private JTable customerTable, staffTable, serviceTable;

    public DermaDeskApp() {
        setTitle("DermaDesk | Dermatology Clinic Appointment System");
        setSize(1000, 650);
        setMinimumSize(new Dimension(850, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        loadSampleServices();
        mainPanel.add(createWelcomePanel(), "welcome");
        mainPanel.add(createCustomerPanel(), "customer");
        mainPanel.add(createStaffPanel(), "staff");
        add(mainPanel);
        cardLayout.show(mainPanel, "welcome");
    }

    private void loadSampleServices() {
        services.add(new Service("Basic Facial", "Basic skin cleansing and care", 500, 45, true));
        services.add(new Service("Acne Treatment", "Basic acne care session", 800, 60, true));
        services.add(new Service("Chemical Peel", "Skin exfoliation treatment", 1500, 60, true));
        services.add(new Service("Skin Consultation", "Initial skin consultation", 300, 30, true));
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(242, 247, 247));
        JPanel content = new JPanel();
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(35, 55, 35, 55));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("DermaDesk");
        title.setFont(new Font("SansSerif", Font.BOLD, 38));
        title.setForeground(NAVY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Dermatology Clinic Appointment System");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(Color.DARK_GRAY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton customerButton = makeButton("Continue as Customer");
        JButton staffButton = makeButton("Continue as Clinic Staff");
        customerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        staffButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        customerButton.addActionListener(e -> {
            refreshServiceChoices();
            cardLayout.show(mainPanel, "customer");
        });
        staffButton.addActionListener(e -> {
            refreshStaffTable();
            refreshServiceTable();
            cardLayout.show(mainPanel, "staff");
        });
        content.add(title);
        content.add(Box.createVerticalStrut(8));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(35));
        content.add(customerButton);
        content.add(Box.createVerticalStrut(12));
        content.add(staffButton);
        content.add(Box.createVerticalStrut(15));
        JLabel note = new JLabel("Demo version | Sample services and appointments");
        note.setFont(new Font("SansSerif", Font.ITALIC, 11));
        note.setForeground(Color.GRAY);
        note.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(note);
        panel.add(content);
        return panel;
    }

    private JPanel createCustomerPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        panel.setBackground(new Color(247, 249, 249));
        panel.add(header("Customer Portal", "Browse services and request an appointment"), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createTitledBorder("Book an Appointment"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 10, 8, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        customerNameField = new JTextField(18);
        serviceCombo = new JComboBox<>();
        dateField = new JTextField("2026-12-01", 12);
        timeField = new JTextField("09:00", 12);
        addFormRow(form, g, 0, "Customer name:", customerNameField);
        addFormRow(form, g, 1, "Service:", serviceCombo);
        addFormRow(form, g, 2, "Date (YYYY-MM-DD):", dateField);
        addFormRow(form, g, 3, "Time (HH:mm):", timeField);
        JButton bookButton = makeButton("Book Appointment");
        g.gridx = 1; g.gridy = 4; g.gridwidth = 1;
        form.add(bookButton, g);
        bookButton.addActionListener(e -> bookAppointment());

        customerModel = new DefaultTableModel(new String[]{"ID", "Service", "Date", "Time", "Duration", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        customerTable = new JTable(customerModel);
        styleTable(customerTable);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createTitledBorder("Your Appointment History"));
        tablePanel.add(new JScrollPane(customerTable), BorderLayout.CENTER);
        center.add(form, BorderLayout.NORTH);
        center.add(tablePanel, BorderLayout.CENTER);
        panel.add(center, BorderLayout.CENTER);

        JButton refresh = makeButton("Refresh My Bookings");
        JButton back = makeButton("Back to Home");
        refresh.addActionListener(e -> refreshCustomerTable());
        back.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(refresh); bottom.add(back);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        panel.setBackground(new Color(247, 249, 249));
        panel.add(header("Clinic Staff Dashboard", "Review bookings and maintain the service list"), BorderLayout.NORTH);

        staffModel = new DefaultTableModel(new String[]{"ID", "Customer", "Service", "Date", "Time", "Duration", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        staffTable = new JTable(staffModel);
        styleTable(staffTable);
        JPanel bookingPanel = new JPanel(new BorderLayout(5, 5));
        bookingPanel.setBackground(Color.WHITE);
        bookingPanel.setBorder(BorderFactory.createTitledBorder("Appointment Requests"));
        bookingPanel.add(new JScrollPane(staffTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setBackground(Color.WHITE);
        JButton confirm = makeButton("Confirm");
        JButton reschedule = makeButton("Reschedule");
        JButton reject = makeButton("Reject");
        JButton cancel = makeButton("Cancel Booking");
        JButton refresh = makeButton("Refresh");
        actions.add(confirm); actions.add(reschedule); actions.add(reject); actions.add(cancel); actions.add(refresh);
        confirm.addActionListener(e -> updateSelectedStatus("Confirmed"));
        reject.addActionListener(e -> updateSelectedStatus("Rejected"));
        cancel.addActionListener(e -> updateSelectedStatus("Cancelled"));
        reschedule.addActionListener(e -> rescheduleSelected());
        refresh.addActionListener(e -> refreshStaffTable());
        bookingPanel.add(actions, BorderLayout.SOUTH);

        serviceModel = new DefaultTableModel(new String[]{"Service", "Description", "Price (PHP)", "Duration (min)", "Available"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        serviceTable = new JTable(serviceModel);
        styleTable(serviceTable);
        JPanel servicePanel = new JPanel(new BorderLayout(5, 5));
        servicePanel.setBackground(Color.WHITE);
        servicePanel.setBorder(BorderFactory.createTitledBorder("Service Catalog"));
        servicePanel.add(new JScrollPane(serviceTable), BorderLayout.CENTER);
        JButton addService = makeButton("Add Service");
        JButton toggleService = makeButton("Change Availability");
        JPanel serviceActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        serviceActions.setBackground(Color.WHITE);
        serviceActions.add(addService); serviceActions.add(toggleService);
        servicePanel.add(serviceActions, BorderLayout.SOUTH);
        addService.addActionListener(e -> addService());
        toggleService.addActionListener(e -> toggleSelectedService());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, bookingPanel, servicePanel);
        split.setResizeWeight(0.68);
        panel.add(split, BorderLayout.CENTER);
        JButton back = makeButton("Back to Home");
        back.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false); bottom.add(back);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel header(String titleText, String subtitleText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(NAVY);
        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(Color.GRAY);
        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 4));
        labels.setOpaque(false); labels.add(title); labels.add(subtitle);
        p.add(labels, BorderLayout.CENTER);
        return p;
    }

    private void addFormRow(JPanel panel, GridBagConstraints g, int row, String label, Component field) {
        g.gridy = row; g.gridx = 0; g.weightx = 0;
        panel.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1;
        panel.add(field, g);
    }

    private JButton makeButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(TEAL);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        return b;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
    }

    private void refreshServiceChoices() {
        serviceCombo.removeAllItems();
        for (Service s : services) if (s.isAvailable()) serviceCombo.addItem(s.getName());
    }

    private void bookAppointment() {
        try {
            String customer = customerNameField.getText().trim();
            if (customer.isEmpty()) throw new IllegalArgumentException("Please enter your name.");
            String serviceName = (String) serviceCombo.getSelectedItem();
            if (serviceName == null) throw new IllegalArgumentException("There are no available services.");
            LocalDate date = LocalDate.parse(dateField.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalTime time = LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            if (date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Please choose today or a future date.");
            Service service = findService(serviceName);
            LocalDateTime start = LocalDateTime.of(date, time);
            LocalDateTime end = start.plusMinutes(service.getDuration());
            if (hasConflict(start, end, null)) throw new IllegalArgumentException("That schedule is already occupied. Please choose another time.");
            Appointment a = new Appointment(appointments.size() + 1, customer, service, date, time, "Pending");
            appointments.add(a);
            JOptionPane.showMessageDialog(this, "Appointment request submitted!\nBooking ID: " + a.getId() + "\nStatus: Pending");
            refreshCustomerTable();
            refreshStaffTable();
        } catch (DateTimeParseException ex) {
            showError("Please enter the date as YYYY-MM-DD and time as HH:mm (24-hour format).");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private boolean hasConflict(LocalDateTime newStart, LocalDateTime newEnd, Appointment ignore) {
        for (Appointment a : appointments) {
            if (a == ignore || !isActive(a.getStatus())) continue;
            LocalDateTime oldStart = LocalDateTime.of(a.getDate(), a.getTime());
            LocalDateTime oldEnd = oldStart.plusMinutes(a.getService().getDuration());
            if (newStart.isBefore(oldEnd) && oldStart.isBefore(newEnd)) return true;
        }
        return false;
    }

    private boolean isActive(String status) {
        return status.equals("Pending") || status.equals("Confirmed") || status.equals("Rescheduled");
    }

    private void refreshCustomerTable() {
        if (customerModel == null) return;
        customerModel.setRowCount(0);
        String name = customerNameField.getText().trim();
        for (Appointment a : appointments) {
            if (name.isEmpty() || a.getCustomer().equalsIgnoreCase(name)) {
                customerModel.addRow(new Object[]{a.getId(), a.getService().getName(), a.getDate(), a.getTime(), a.getService().getDuration() + " min", a.getStatus()});
            }
        }
    }

    private void refreshStaffTable() {
        if (staffModel == null) return;
        staffModel.setRowCount(0);
        for (Appointment a : appointments) {
            staffModel.addRow(new Object[]{a.getId(), a.getCustomer(), a.getService().getName(), a.getDate(), a.getTime(), a.getService().getDuration() + " min", a.getStatus()});
        }
    }

    private void refreshServiceTable() {
        if (serviceModel == null) return;
        serviceModel.setRowCount(0);
        for (Service s : services) serviceModel.addRow(new Object[]{s.getName(), s.getDescription(), String.format("₱%.2f", s.getPrice()), s.getDuration(), s.isAvailable() ? "Yes" : "No"});
    }

    private Appointment getSelectedAppointment() {
        int row = staffTable.getSelectedRow();
        if (row < 0) { showError("Please select an appointment first."); return null; }
        int id = Integer.parseInt(staffModel.getValueAt(row, 0).toString());
        for (Appointment a : appointments) if (a.getId() == id) return a;
        return null;
    }

    private void updateSelectedStatus(String status) {
        Appointment a = getSelectedAppointment();
        if (a == null) return;
        a.setStatus(status);
        refreshStaffTable();
        refreshCustomerTable();
    }

    private void rescheduleSelected() {
        Appointment a = getSelectedAppointment();
        if (a == null) return;
        JTextField date = new JTextField(a.getDate().toString());
        JTextField time = new JTextField(a.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
        p.add(new JLabel("New date (YYYY-MM-DD):")); p.add(date);
        p.add(new JLabel("New time (HH:mm):")); p.add(time);
        if (JOptionPane.showConfirmDialog(this, p, "Reschedule Appointment", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            LocalDate newDate = LocalDate.parse(date.getText().trim());
            LocalTime newTime = LocalTime.parse(time.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            if (newDate.isBefore(LocalDate.now())) throw new IllegalArgumentException("Please choose today or a future date.");
            LocalDateTime start = LocalDateTime.of(newDate, newTime);
            if (hasConflict(start, start.plusMinutes(a.getService().getDuration()), a)) throw new IllegalArgumentException("That schedule conflicts with another active appointment.");
            a.setDate(newDate); a.setTime(newTime); a.setStatus("Rescheduled");
            refreshStaffTable(); refreshCustomerTable();
            JOptionPane.showMessageDialog(this, "Appointment rescheduled successfully.");
        } catch (DateTimeParseException ex) {
            showError("Invalid date or time format.");
        } catch (IllegalArgumentException ex) { showError(ex.getMessage()); }
    }

    private void addService() {
        JTextField name = new JTextField();
        JTextField description = new JTextField();
        JTextField price = new JTextField();
        JTextField duration = new JTextField();
        JPanel p = new JPanel(new GridLayout(4, 2, 6, 6));
        p.add(new JLabel("Service name:")); p.add(name);
        p.add(new JLabel("Description:")); p.add(description);
        p.add(new JLabel("Price (PHP):")); p.add(price);
        p.add(new JLabel("Duration (minutes):")); p.add(duration);
        if (JOptionPane.showConfirmDialog(this, p, "Add Service", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            String n = name.getText().trim();
            if (n.isEmpty() || description.getText().trim().isEmpty()) throw new IllegalArgumentException("Complete all service details.");
            double cost = Double.parseDouble(price.getText().trim());
            int mins = Integer.parseInt(duration.getText().trim());
            if (cost < 0 || mins <= 0) throw new IllegalArgumentException("Price must be 0 or higher and duration must be positive.");
            if (findServiceOrNull(n) != null) throw new IllegalArgumentException("That service already exists.");
            services.add(new Service(n, description.getText().trim(), cost, mins, true));
            refreshServiceTable(); refreshServiceChoices();
        } catch (NumberFormatException ex) { showError("Enter a valid number for price and duration."); }
        catch (IllegalArgumentException ex) { showError(ex.getMessage()); }
    }

    private void toggleSelectedService() {
        int row = serviceTable.getSelectedRow();
        if (row < 0) { showError("Please select a service first."); return; }
        String name = serviceModel.getValueAt(row, 0).toString();
        Service s = findService(name);
        s.setAvailable(!s.isAvailable());
        refreshServiceTable(); refreshServiceChoices();
    }

    private Service findService(String name) {
        Service s = findServiceOrNull(name);
        if (s == null) throw new IllegalArgumentException("Selected service was not found.");
        return s;
    }

    private Service findServiceOrNull(String name) {
        for (Service s : services) if (s.getName().equalsIgnoreCase(name)) return s;
        return null;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "DermaDesk", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DermaDeskApp().setVisible(true));
    }

    private static class Service {
        private final String name, description;
        private final double price;
        private final int duration;
        private boolean available;
        Service(String name, String description, double price, int duration, boolean available) {
            this.name = name; this.description = description; this.price = price; this.duration = duration; this.available = available;
        }
        String getName() { return name; }
        String getDescription() { return description; }
        double getPrice() { return price; }
        int getDuration() { return duration; }
        boolean isAvailable() { return available; }
        void setAvailable(boolean available) { this.available = available; }
    }

    private static class Appointment {
        private final int id;
        private final String customer;
        private final Service service;
        private LocalDate date;
        private LocalTime time;
        private String status;
        Appointment(int id, String customer, Service service, LocalDate date, LocalTime time, String status) {
            this.id = id; this.customer = customer; this.service = service; this.date = date; this.time = time; this.status = status;
        }
        int getId() { return id; }
        String getCustomer() { return customer; }
        Service getService() { return service; }
        LocalDate getDate() { return date; }
        LocalTime getTime() { return time; }
        String getStatus() { return status; }
        void setDate(LocalDate date) { this.date = date; }
        void setTime(LocalTime time) { this.time = time; }
        void setStatus(String status) { this.status = status; }
    }
}
