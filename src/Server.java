import java.net.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class Server implements Serializable{
    public static final int PORT = 7896;
    private static ServerSocket serverSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private static ArrayList<LotteryEntries> lotteryEntries = new ArrayList<>();
    private static ArrayList<LotteryEntries> lotteryWinners = new ArrayList<>();

    public Server(ServerSocket serverSocket) {
        Server.serverSocket = serverSocket;
    }
    public void run() {
        try {
            System.out.println(LocalDateTime.now());
            TimerTask task = new TimerTask() {
                public void run() {
                    lotterySystem();
                }
            };
            Timer timer = new Timer("Timer");
            timer.schedule(task, 0l, 1000*60*60);

            while(!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected");
                this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message;
                while(true) {
                    try {
                        message = in.readLine();
                        if(message.contains("History request")){
                            requestHistory();
                        } else {
                            enterLottery(message);
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
            }
        } catch (IOException e) {
            try {
                if(serverSocket!=null) {
                    serverSocket.close();
                }
            } catch(IOException ie) {
                System.out.println(e.getMessage());
                ie.printStackTrace();
            }
        }
    }
    public void enterLottery(String timeSlot){
        boolean exists = false;
        try {
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter timeSlotFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String name = in.readLine();
            String email = in.readLine();
            int number = Integer.parseInt(in.readLine());

            if(number<0||number>255){
                this.out.write("Unsuccessful lottery number is out of bounds");
                this.out.newLine();
                this.out.flush();
                return;
            }
            if(LocalDateTime.parse(timeSlot, timeSlotFormat).isBefore(myDateObj)){
                this.out.write("Unsuccessful time slot is out of bounds");
                this.out.newLine();
                this.out.flush();
                return;
            }
            for (LotteryEntries lots : lotteryEntries) {
                if(lots.timeSlot.contains(timeSlot)){
                    for (Clients c : lots.entries) {
                        if(c.name.contains(name) && c.lotteryNumber==number){
                            this.out.write("Unsuccessful lottery number already used");
                            this.out.newLine();
                            this.out.flush();
                            return;
                        }
                    }
                    lots.money += 100;
                    lots.entries.add(new Clients(name, email, number));
                    this.out.write("You have entered the lottery, the current money pool is: " + lots.money + "kr");
                    this.out.newLine();
                    this.out.flush();
                    exists = true;
                }
            }
            if(!exists){
                LotteryEntries entry = new LotteryEntries(timeSlot, 100);
                entry.entries.add(new Clients(name, email, number));
                lotteryEntries.add(entry);
                this.out.write("You have entered the lottery, the current money pool is: " + entry.money + "kr");
                this.out.newLine();
                this.out.flush();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void requestHistory(){
        try{
            DateTimeFormatter timeSlotFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime start = LocalDateTime.parse(in.readLine(), timeSlotFormat);
            LocalDateTime end = LocalDateTime.parse(in.readLine(), timeSlotFormat);
            boolean sent = false;
            this.out.write("History");
            this.out.newLine();
            this.out.flush();
            if(lotteryWinners.isEmpty()){
                this.out.write("No winners were found in the time slot");
                this.out.newLine();
                this.out.flush();
                sent = true;
            }
            if(start.isAfter(end)){
                this.out.write("Unsuccessful start time is after end");
                this.out.newLine();
                this.out.flush();
                return;
            }
            for(LotteryEntries lots : lotteryWinners){//got through all winners
                System.out.println("Looking through winners " + lots.timeSlot);
                //if time slot is => start and =< end
                if((start.isBefore(LocalDateTime.parse(lots.timeSlot, timeSlotFormat)) && end.isAfter(LocalDateTime.parse(lots.timeSlot, timeSlotFormat)))
                        || start.isEqual(LocalDateTime.parse(lots.timeSlot, timeSlotFormat)) || end.isEqual(LocalDateTime.parse(lots.timeSlot, timeSlotFormat))){
                    for (Clients c : lots.entries){
                        if(!sent){
                            this.out.write("In time slot " + lots.timeSlot + " the winners with number " + c.lotteryNumber + " won " + lots.money + "kr, the winners were:");
                            this.out.newLine();
                            this.out.flush();
                            sent = true;
                        }
                        this.out.write("Name: " + c.name + " Email: " + c.email);
                        this.out.newLine();
                        this.out.flush();
                    }
                }
            }
            if(!sent){
                this.out.write("No winners found in this time slot");
                this.out.newLine();
                this.out.flush();
            }
            this.out.write("Done");
            this.out.newLine();
            this.out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    public static void main(String args[]) throws IOException {
        serverSocket = new ServerSocket(PORT);
        Server server = new Server(serverSocket);
        server.run();
    }

      public void lotterySystem(){
                //keep track of the time
                LocalDateTime myDateObj = LocalDateTime.now();
                DateTimeFormatter timeSlotFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                String timeSlot = myDateObj.format(timeSlotFormat);
                boolean win = false;
                boolean exists = false;
                int amountOfWinners = 1;
                int moneyPool = 0;
               // if (LocalDateTime.now().getMinute() == lotteryTime) {
                    //each hour call on lotteryNumber
                    //if (myDateObj.getMinute() == 35) {
                    int lotteryNumber = lotteryNumber();
                    System.out.println("The winning number is: " + lotteryNumber);
                    //go through entries and check for time slot
                    for (LotteryEntries lots : lotteryEntries) {
                        //if there are any entries in the time slot
                        if (lots.timeSlot.contains(timeSlot)) {
                            moneyPool = lots.money;
                            //go through clients in time slot
                            for (Clients c : lots.entries) {
                                //if a clients lottery number equals the drawn number
                                if (c.lotteryNumber == lotteryNumber) {
                                    win = true;
                                    //check if time slot exists in winner list
                                    for (LotteryEntries slot : lotteryWinners) {
                                        //if time slot exists add client
                                        if (slot.timeSlot.contains(timeSlot)) {
                                            amountOfWinners++;
                                            exists = true;
                                            slot.entries.add(c);
                                        }
                                    }
                                    if (!exists) { //if there is only one winner
                                        LotteryEntries winner = new LotteryEntries(timeSlot, lots.money);
                                        winner.entries.add(c);
                                        lotteryWinners.add(winner);
                                    }
                                }
                            }
                        }
                    }
                    if (win) {
                        sendEmail(timeSlot, amountOfWinners);
                    }
                    if (!win) {
                        System.out.println("No winners this time slot");
                        addToNextMoneyPool(timeSlot, moneyPool);
                    }
               // }
    }

    public void addToNextMoneyPool(String timeSlot, int money){
        LocalDateTime temp = LocalDateTime.MAX;
        DateTimeFormatter timeSlotFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime start = LocalDateTime.parse(timeSlot, timeSlotFormat);

        for (LotteryEntries lots : lotteryEntries){
            if(LocalDateTime.parse(lots.timeSlot, timeSlotFormat).isAfter(start)&&LocalDateTime.parse(lots.timeSlot, timeSlotFormat).isBefore(temp)) {
                temp = LocalDateTime.parse(lots.timeSlot, timeSlotFormat);
            }
        }
        String newTime = temp.format(timeSlotFormat);
        if(temp!=null){
            for (LotteryEntries lots : lotteryEntries){
                if(lots.timeSlot.contains(newTime)){
                    lots.money += money;
                    System.out.println("New money pool: " + lots.money + " for time slot " + lots.timeSlot);
                }
            }
        }

    }

    public void sendEmail(String timeSlot, int nrOfWinners){
        for(LotteryEntries slot : lotteryWinners){
            if(slot.timeSlot.contains(timeSlot)){
                slot.money = slot.money/nrOfWinners;
                for(Clients c : slot.entries){
                    System.out.println("Email address " + c.email);
                    System.out.println("Client " + c.name + " have won " + slot.money + "kr of the lottery!");
                }
            }
        }
    }
    public int lotteryNumber(){ //pick number
        return ThreadLocalRandom.current().nextInt(0, 255 + 1);
    }


}