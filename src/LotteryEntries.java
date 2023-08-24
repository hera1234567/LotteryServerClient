import java.util.ArrayList;
import java.util.List;

public class LotteryEntries {
    public List<Clients> entries;
    public String timeSlot;
    public int money;

    public LotteryEntries(String timeSlot, int money){
        entries = new ArrayList<>();
        this.timeSlot = timeSlot;
        this.money = money;
    }

}
