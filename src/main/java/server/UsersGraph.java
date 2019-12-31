package server;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class UsersGraph {
    private TreeSet<User> usersTree;
    private ConcurrentHashMap<String, ArrayList<User.UserInfo>> usersGraph;

    public UsersGraph() {
        usersTree = new TreeSet<User>();
        usersGraph = new ConcurrentHashMap<String, ArrayList<User.UserInfo>>();
    }

    public boolean existsUser(String nickname) {
        return usersTree.contains(nickname);
    }

    public void insertUser(User user) {
        usersTree.add(user);
        usersGraph.put(user.getInfo().getNickname(), new ArrayList<User.UserInfo>());
    }

    public User getUser(String nickname) {
        return usersTree.get(nickname);
    }

    public void insertFriend(String userNickname, String friendNickname) {
        usersGraph.get(userNickname).add(usersTree.);
    }
}
