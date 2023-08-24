import java.net.*;
import java.util.*;
import java.io.*;
public class Client {

    private Socket client;
    private BufferedReader in;
    private BufferedWriter out;
    private String name;
    private String email;

    public Client(Socket client, String username, String email) {
        try {
            this.client = client;
            this.out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            this.name = username;
            this.email = email;
        } catch (IOException e) {
            terminate(client, in, out);
        }
    }

    public void enterLottery(String lotteryNumber, String timeSlot) {
        try {
            out.write(timeSlot);
            out.newLine();
            out.flush();
            out.write(name);
            out.newLine();
            out.flush();
            out.write(email);
            out.newLine();
            out.flush();
            out.write(lotteryNumber);
            out.newLine();
            out.flush();
        }catch(IOException e) {
            terminate(client, in, out);
        }
    }

    public void requestHistory(String start, String end) {
        try {
            out.write("History request");
            out.newLine();
            out.flush();
            out.write(start);
            out.newLine();
            out.flush();
            out.write(end);
            out.newLine();
            out.flush();
        }catch(IOException e) {
            terminate(client, in, out);
        }
    }

    public boolean listenForMsg() {
        String messageFromChat;
        try {
            messageFromChat = in.readLine();
            if(messageFromChat.contains("History")){
                while(!(messageFromChat=in.readLine()).contains("Done")){
                    System.out.println(messageFromChat);
                }
            } else
                System.out.println(messageFromChat);
        }catch(IOException e) {
            terminate(client, in, out);
        }
        return true;
    }

    public void terminate(Socket client, BufferedReader in, BufferedWriter out) {
        System.out.println("Shutting down");
        try {
            if(in != null) {
                in.close();
            }
            if(out != null) {
                out.close();
            }
            if(client != null) {
                client.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your name: ");
        String name = scanner.nextLine();
        System.out.println("Enter your email: ");
        String email = scanner.nextLine();
        Socket socket = new Socket("localhost", 7896);
        Client client = new Client(socket, name, email);

        while(true) {
            System.out.println("Do you want to enter the lottery: ");
            String request = scanner.nextLine();
            if (request.contains("yes")) {
                System.out.println("Pick a lottery number from 0 to 255: ");
                String lotteryNumber = scanner.nextLine();
                System.out.println("Pick a time slot (2022-12-12 13:00): ");
                String timeSlot = scanner.nextLine();
                client.enterLottery(lotteryNumber, timeSlot);
            } else {
                System.out.println("Start of requested history (2022-12-12 13:00): ");
                String startHistory = scanner.nextLine();
                System.out.println("End of requested history (2022-12-12 13:00): ");
                String endHistory = scanner.nextLine();
                client.requestHistory(startHistory, endHistory);
            }
            client.listenForMsg();
            System.out.println("Do you have more requests? ");
            if(scanner.nextLine().contains("no")){
                break;
            }
        }
    }

}