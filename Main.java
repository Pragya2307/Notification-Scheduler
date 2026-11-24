import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/*
 * Notification Scheduler
 * Data Structures: PriorityQueue, Queue, Circular Queue
 * File: notifications.txt
 *
 * Compile: javac Main.java
 * Run:     java Main
 */
class Notification {
    private final String id;
    private final String message;
    private LocalDateTime scheduledTime;
    private final long repeatMinutes;
    private boolean cancelled;

    public Notification(String id, String message, LocalDateTime scheduledTime,
                        long repeatMinutes) {
        this.id = id;
        this.message = message;
        this.scheduledTime = scheduledTime;
        this.repeatMinutes = repeatMinutes;
        this.cancelled = false;
    }

    public String getId() { return id; }
    public String getMessage() { return message; }
    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public long getRepeatMinutes() { return repeatMinutes; }
    public boolean isCancelled() { return cancelled; }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void cancel() { this.cancelled = true; }

    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String repeatText = repeatMinutes > 0 ? "Every " + repeatMinutes + " minute(s)" : "One-time";
        return id + " | " + message + " | " + scheduledTime.format(formatter)
                + " | " + repeatText;
    }
}

/*
 * A simple dynamically-resizing circular queue.
 * It stores recurring notification objects and supports rotation/removal.
 */
class CircularQueue {
    private Notification[] items;
    private int front;
    private int rear;
    private int size;

    public CircularQueue() {
        items = new Notification[5];
        front = 0;
        rear = -1;
        size = 0;
    }

    public boolean isEmpty() { return size == 0; }

    private void resize() {
        Notification[] bigger = new Notification[items.length * 2];
        for (int i = 0; i < size; i++) {
            bigger[i] = items[(front + i) % items.length];
        }
        items = bigger;
        front = 0;
        rear = size - 1;
    }

    public void enqueue(Notification notification) {
        if (size == items.length) resize();
        rear = (rear + 1) % items.length;
        items[rear] = notification;
        size++;
    }

    public Notification dequeue() {
        if (isEmpty()) return null;
        Notification result = items[front];
        items[front] = null;
        front = (front + 1) % items.length;
        size--;
        if (size == 0) {
            front = 0;
            rear = -1;
        }
        return result;
    }

    public void rotate() {
        if (size > 1) {
            Notification first = dequeue();
            enqueue(first);
        }
    }

    public Notification findById(String id) {
        for (int i = 0; i < size; i++) {
            Notification n = items[(front + i) % items.length];
            if (n != null && n.getId().equals(id)) return n;
        }
        return null;
    }

    public void removeById(String id) {
        List<Notification> keep = new ArrayList<Notification>();
        while (!isEmpty()) {
            Notification n = dequeue();
            if (!n.getId().equals(id)) keep.add(n);
        }
        for (Notification n : keep) enqueue(n);
    }

    public List<Notification> getAll() {
        List<Notification> result = new ArrayList<Notification>();
        for (int i = 0; i < size; i++) {
            result.add(items[(front + i) % items.length]);
        }
        return result;
    }
}

class NotificationManager {
    private final PriorityQueue<Notification> priorityQueue;
    private final Queue<Notification> readyQueue;
    private final CircularQueue recurringQueue;
    private final Set<String> cancelledIds;
    private final String fileName;
    private int nextId;

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public NotificationManager(String fileName) {
        this.fileName = fileName;
        this.priorityQueue = new PriorityQueue<Notification>(new Comparator<Notification>() {
            public int compare(Notification a, Notification b) {
                int timeCompare = a.getScheduledTime().compareTo(b.getScheduledTime());
                return timeCompare != 0 ? timeCompare : a.getId().compareTo(b.getId());
            }
        });
        this.readyQueue = new LinkedList<Notification>();
        this.recurringQueue = new CircularQueue();
        this.cancelledIds = new HashSet<String>();
        this.nextId = 101;
        loadFromFile();
    }

    public void scheduleNotification(String message, LocalDateTime time, long repeatMinutes) {
        if (message == null || message.trim().isEmpty()) {
            System.out.println("Message cannot be empty.");
            return;
        }
        if (time == null || !time.isAfter(LocalDateTime.now())) {
            System.out.println("Invalid time. Please enter a future date and time.");
            return;
        }
        if (repeatMinutes < 0) {
            System.out.println("Repeat interval cannot be negative.");
            return;
        }
        if (isDuplicate(message.trim(), time)) {
            System.out.println("Duplicate notification. It was not scheduled.");
            return;
        }

        String id = "N" + nextId++;
        Notification notification =
                new Notification(id, message.trim(), time, repeatMinutes);
        priorityQueue.offer(notification);

        if (repeatMinutes > 0) {
            recurringQueue.enqueue(notification);
        }

        System.out.println("Notification scheduled: " + id + " | "
                + time.format(FORMAT));
    }

    private boolean isDuplicate(String message, LocalDateTime time) {
        for (Notification n : priorityQueue) {
            if (!n.isCancelled()
                    && n.getMessage().equalsIgnoreCase(message)
                    && n.getScheduledTime().equals(time)) return true;
        }
        for (Notification n : readyQueue) {
            if (!n.isCancelled()
                    && n.getMessage().equalsIgnoreCase(message)
                    && n.getScheduledTime().equals(time)) return true;
        }
        return false;
    }

    public void viewPendingNotifications() {
        if (priorityQueue.isEmpty()) {
            System.out.println("No pending notifications.");
            return;
        }
        List<Notification> list = new ArrayList<Notification>(priorityQueue);
        Collections.sort(list, new Comparator<Notification>() {
            public int compare(Notification a, Notification b) {
                return a.getScheduledTime().compareTo(b.getScheduledTime());
            }
        });
        System.out.println("\n--- PENDING NOTIFICATIONS ---");
        for (Notification n : list) {
            if (!n.isCancelled()) System.out.println(n);
        }
    }

    public void cancelNotification(String id) {
        boolean found = false;
        Iterator<Notification> iterator = priorityQueue.iterator();
        while (iterator.hasNext()) {
            Notification n = iterator.next();
            if (n.getId().equalsIgnoreCase(id)) {
                n.cancel();
                cancelledIds.add(n.getId());
                iterator.remove();
                recurringQueue.removeById(n.getId());
                found = true;
                break;
            }
        }

        if (!found) {
            Iterator<Notification> readyIterator = readyQueue.iterator();
            while (readyIterator.hasNext()) {
                Notification n = readyIterator.next();
                if (n.getId().equalsIgnoreCase(id)) {
                    n.cancel();
                    cancelledIds.add(n.getId());
                    readyIterator.remove();
                    recurringQueue.removeById(n.getId());
                    found = true;
                    break;
                }
            }
        }

        if (found) System.out.println("Notification " + id + " cancelled.");
        else System.out.println("Notification ID not found.");
    }

    public void checkDueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        int moved = 0;

        while (!priorityQueue.isEmpty()
                && !priorityQueue.peek().getScheduledTime().isAfter(now)) {
            Notification n = priorityQueue.poll();
            if (!n.isCancelled() && !cancelledIds.contains(n.getId())) {
                readyQueue.offer(n);
                moved++;
            }
        }

        if (moved == 0) {
            System.out.println("No notifications ready to send.");
            return;
        }

        System.out.println(moved + " notification(s) moved to the ready queue.");
        processReadyQueue();
    }

    public void processReadyQueue() {
        while (!readyQueue.isEmpty()) {
            Notification n = readyQueue.poll();
            if (n.isCancelled() || cancelledIds.contains(n.getId())) continue;

            System.out.println("SENT / DISPLAYED: " + n.getMessage()
                    + " [" + n.getId() + "]");

            if (n.getRepeatMinutes() > 0) {
                Notification recurring = recurringQueue.findById(n.getId());
                if (recurring != null && !recurring.isCancelled()) {
                    LocalDateTime nextTime = recurring.getScheduledTime()
                            .plusMinutes(recurring.getRepeatMinutes());

                    // Skip missed intervals instead of sending a burst of old reminders.
                    LocalDateTime now = LocalDateTime.now();
                    while (!nextTime.isAfter(now)) {
                        nextTime = nextTime.plusMinutes(recurring.getRepeatMinutes());
                    }

                    recurring.setScheduledTime(nextTime);
                    priorityQueue.offer(recurring);
                    recurringQueue.rotate();
                    System.out.println("Next repeat scheduled for: "
                            + nextTime.format(FORMAT));
                }
            }
        }
    }

    public void saveToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (Notification n : priorityQueue) {
                if (!n.isCancelled() && !cancelledIds.contains(n.getId())) {
                    String encodedMessage = Base64.getEncoder().encodeToString(
                            n.getMessage().getBytes("UTF-8"));
                    writer.write(n.getId() + "|" + encodedMessage + "|"
                            + n.getScheduledTime().toString() + "|"
                            + n.getRepeatMinutes());
                    writer.newLine();
                }
            }
            System.out.println("Pending notifications saved to " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving notifications: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("No saved notification file found. Starting fresh.");
            return;
        }

        int loaded = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split("\\|", 4);
                    if (parts.length != 4) continue;

                    String id = parts[0];
                    String message = new String(Base64.getDecoder().decode(parts[1]), "UTF-8");
                    LocalDateTime time = LocalDateTime.parse(parts[2]);
                    long repeat = Long.parseLong(parts[3]);

                    if (!time.isAfter(LocalDateTime.now())) continue;

                    Notification n = new Notification(id, message, time, repeat);
                    priorityQueue.offer(n);
                    if (repeat > 0) recurringQueue.enqueue(n);

                    if (id.startsWith("N")) {
                        try {
                            int number = Integer.parseInt(id.substring(1));
                            if (number >= nextId) nextId = number + 1;
                        } catch (NumberFormatException ignored) { }
                    }
                    loaded++;
                } catch (Exception badRecord) {
                    System.out.println("Skipped an invalid record in the save file.");
                }
            }
            System.out.println(loaded + " pending notification(s) loaded.");
        } catch (IOException e) {
            System.out.println("Error loading notifications: " + e.getMessage());
        }
    }
}

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static LocalDateTime readDateTime() {
        System.out.print("Enter date and time (yyyy-MM-dd HH:mm): ");
        String input = scanner.nextLine().trim();
        try {
            LocalDateTime time = LocalDateTime.parse(input, FORMAT);
            if (!time.isAfter(LocalDateTime.now())) {
                System.out.println("The scheduled date/time must be in the future.");
                return null;
            }
            return time;
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date/time format. Example: 2026-12-01 10:30");
            return null;
        }
    }

    public static void main(String[] args) {
        NotificationManager manager = new NotificationManager("notifications.txt");
        boolean running = true;

        while (running) {
            System.out.println("\n========== NOTIFICATION SCHEDULER ==========");
            System.out.println("1. Schedule notification");
            System.out.println("2. View pending notifications");
            System.out.println("3. Cancel notification");
            System.out.println("4. Process due notifications");
            System.out.println("5. Save notifications");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    System.out.print("Enter notification message: ");
                    String message = scanner.nextLine();
                    LocalDateTime time = readDateTime();
                    if (time == null) break;

                    System.out.print("Repeat interval in minutes (0 = one-time): ");
                    try {
                        long repeat = Long.parseLong(scanner.nextLine().trim());
                        if (repeat < 0) {
                            System.out.println("Repeat interval cannot be negative.");
                            break;
                        }
                        manager.scheduleNotification(message, time, repeat);
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid whole number.");
                    }
                    break;

                case "2":
                    manager.viewPendingNotifications();
                    break;

                case "3":
                    System.out.print("Enter notification ID to cancel (example N101): ");
                    manager.cancelNotification(scanner.nextLine().trim());
                    break;

                case "4":
                    manager.checkDueNotifications();
                    break;

                case "5":
                    manager.saveToFile();
                    break;

                case "6":
                    manager.saveToFile();
                    running = false;
                    System.out.println("Thank you for using Notification Scheduler.");
                    break;

                default:
                    System.out.println("Invalid choice. Please select 1 to 6.");
            }
        }
        scanner.close();
    }
}
