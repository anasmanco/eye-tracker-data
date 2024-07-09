/* 
package tobii;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

class ListeningToSocketThread implements Runnable {
    private final Socket socket;
    private final Thread otherThread;

    public ListeningToSocketThread(Socket socket, Thread otherThread) {
        this.socket = socket;
        this.otherThread = otherThread;
    }

    public void run() {
        String message = getMessageFromSocket(this.socket);

        if (Objects.equals(message, "STOP")) {
            System.out.println(message);
            otherThread.interrupt();
            Thread.currentThread().interrupt();
        }
    }

    public static String getMessageFromSocket(Socket socket) {
        String message = null;
        try {
            InputStream input = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int length = input.read(buffer);
            message = new String(buffer, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }
}

class WriteGazeDataToFile implements Runnable {
    private final double screenWidth, screenHeight;
    private final String file_name;

    private final int freq = 64;

    public WriteGazeDataToFile(double screenWidth, double screenHeight, String file_name) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.file_name = file_name;
    }

    public void run() {
        long last_nanos = System.nanoTime();
        while (!Thread.currentThread().isInterrupted()) {
            float[] position = Tobii.gazePosition();
            long aux = System.nanoTime();
            long timestamp = System.currentTimeMillis();

            float xRatio = position[0];
            float yRatio = position[1];

            int xPosition = (int) (xRatio * screenWidth);
            int yPosition = (int) (yRatio * screenHeight);

            String message = "(" + xPosition + ", " + yPosition + ")";
            System.out.println(message);

            if (aux - last_nanos >= 1000000000 / freq) {
                writeToFile(file_name, xPosition + "\n" + yPosition + "\n" + timestamp + "\n");
                last_nanos = aux;
            }
        }
    }

    private static void writeToFile(String filename, String content) {
        try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write(content);
            fw.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}

public class TobiiDemo {
    static final String folder_path = "/home/user/Documentos/test";
    static String file_name_base = folder_path + "/GAZE-DATA-";
    static int PORT = 1234;


    public static void main(String[] args) throws Exception {
        File folder = new File(folder_path);
        if (!folder.exists()) {
            if (!folder.mkdirs())
                System.exit(-1);
        }
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = defaultToolkit.getScreenSize();
            double screenWidth = screenSize.getWidth();
            double screenHeight = screenSize.getHeight();
            System.out.println("screenWidth = " + screenWidth + ", screenHeight = " + screenHeight);

            while (true) {
                Socket socket = serverSocket.accept();
                String inputFromPython = ListeningToSocketThread.getMessageFromSocket(socket);
                if (Objects.equals(inputFromPython, "WRITE")) {
                    String file_name = file_name_base + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS")) + ".txt";
                    createNewFile(file_name);

                    WriteGazeDataToFile WriteGazeData = new WriteGazeDataToFile(screenWidth, screenHeight, file_name);
                    Thread WriteGazeDataThread = new Thread(WriteGazeData);
                    ListeningToSocketThread ListeningToSocket = new ListeningToSocketThread(socket, WriteGazeDataThread);
                    Thread ListeningToSocketThread = new Thread(ListeningToSocket);

                    ListeningToSocketThread.start();
                    WriteGazeDataThread.start();
                }
            }
        }
    }

    public static void createNewFile(String filename) {
        try {
            File file = new File(filename);
            if (file.createNewFile()) {
                System.out.println("File created successfully.");
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file: " + e.getMessage());
        }
    }
}
*/

package tobii;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

class ListeningToSocketThread implements Runnable {
    private final Socket socket;
    private final Thread otherThread;

    public ListeningToSocketThread(Socket socket, Thread otherThread) {
        this.socket = socket;
        this.otherThread = otherThread;
    }

    public void run() {
        String message = getMessageFromSocket(this.socket);
        System.out.println("ListeningToSocketThread received message: " + message);

        if (Objects.equals(message, "STOP")) {
            System.out.println("Received STOP message. Stopping threads.");
            otherThread.interrupt();
            Thread.currentThread().interrupt();
        }
    }

    public static String getMessageFromSocket(Socket socket) {
        String message = null;
        try {
            InputStream input = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int length = input.read(buffer);
            if (length > 0) {
                message = new String(buffer, 0, length);
            } else {
                System.out.println("No data read from socket.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }
}

class WriteGazeDataToFile implements Runnable {
    private final double screenWidth, screenHeight;
    private final String file_name;
    private final int freq = 64;

    public WriteGazeDataToFile(double screenWidth, double screenHeight, String file_name) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.file_name = file_name;
    }

    public void run() {
        long last_nanos = System.nanoTime();
        while (!Thread.currentThread().isInterrupted()) {
            float[] position = Tobii.gazePosition();
            if (position == null || position.length != 2) {
                System.out.println("Invalid gaze position data. Using dummy data.");
                position = new float[]{0.5f, 0.5f}; // Dummy data for testing
            }

            long aux = System.nanoTime();
            long timestamp = System.currentTimeMillis();

            float xRatio = position[0];
            float yRatio = position[1];

            int xPosition = (int) (xRatio * screenWidth);
            int yPosition = (int) (yRatio * screenHeight);

            String message = "(" + xPosition + ", " + yPosition + ")";
            System.out.println("Gaze data: " + message);

            if (aux - last_nanos >= 1000000000 / freq) {
                writeToFile(file_name, xPosition + "\n" + yPosition + "\n" + timestamp + "\n");
                last_nanos = aux;
            }
        }
    }

    private static void writeToFile(String filename, String content) {
        try {
            FileWriter fw = new FileWriter(filename, true);
            fw.write(content);
            fw.close();
            System.out.println("Wrote to file: " + filename);
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}

public class TobiiDemo {
    static final String folder_path = "/home/user/Documentos/test";
    static String file_name_base = folder_path + "/GAZE-DATA-";
    static int PORT = 1234;

    public static void main(String[] args) throws Exception {
        File folder = new File(folder_path);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                System.out.println("Failed to create directory: " + folder_path);
                System.exit(-1);
            }
        }
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = defaultToolkit.getScreenSize();
            double screenWidth = screenSize.getWidth();
            double screenHeight = screenSize.getHeight();
            System.out.println("screenWidth = " + screenWidth + ", screenHeight = " + screenHeight);

            while (true) {
                System.out.println("Waiting for connection on port " + PORT + "...");
                Socket socket = serverSocket.accept();
                System.out.println("Connection accepted from: " + socket.getInetAddress());
                String inputFromPython = ListeningToSocketThread.getMessageFromSocket(socket);
                System.out.println("Received message: " + inputFromPython);
                if (Objects.equals(inputFromPython, "WRITE")) {
                    String file_name = file_name_base + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss-SSS")) + ".txt";
                    createNewFile(file_name);

                    WriteGazeDataToFile writeGazeData = new WriteGazeDataToFile(screenWidth, screenHeight, file_name);
                    Thread writeGazeDataThread = new Thread(writeGazeData);
                    ListeningToSocketThread listeningToSocket = new ListeningToSocketThread(socket, writeGazeDataThread);
                    Thread listeningToSocketThread = new Thread(listeningToSocket);

                    listeningToSocketThread.start();
                    writeGazeDataThread.start();
                } else {
                    System.out.println("Unexpected message received: " + inputFromPython);
                }
            }
        }
    }

    public static void createNewFile(String filename) {
        try {
            File file = new File(filename);
            if (file.createNewFile()) {
                System.out.println("File created successfully: " + filename);
            } else {
                System.out.println("File already exists: " + filename);
            }
        } catch (IOException e) {
            System.out.println("An error occurred while creating the file: " + e.getMessage());
        }
    }
}
