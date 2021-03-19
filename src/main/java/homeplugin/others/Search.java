package homeplugin.others;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class Search {

    Player player;
    UI.Page page;
    ArrayList<String> keywords;

    public Search(Player player, UI.Page page) {
        this.player = player;
        this.page = page;
    }

    public void set(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

    public void add(String keyword) {
        if (!keywords.contains(keyword)) keywords.add(keyword);
    }

    public void remove(String keyword) {
        keywords.remove(keyword);
    }

    public void clear() {
        keywords = new ArrayList<>();
    }


    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public String getKeyword(Integer index) {
        return keywords.get(keywords.size() - index);
    }
}
