package de.hska.lkit.user;

import de.hska.lkit.redis.model.User;
import de.hska.lkit.redis.repo.UserRepository;
import de.hska.lkit.sessions.SessionSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Created by bob on 19/10/2016.
 */


@Controller
public class UserViewController {

    @Autowired
    private UserRepository userRepository;

    @SuppressWarnings("Duplicates")
    @RequestMapping(value = "/users/{username}")
    public String showUser(@PathVariable("username") String username, Model model) {

        // Add current user
        User currentUser = userRepository.findUser(SessionSecurity.getName());
        model.addAttribute("currentUser", currentUser);

        // Get requested user
        User user = userRepository.findUser(username);
        if (user != null) {
            model.addAttribute("user", user);
        } else {
            // User not found
            String errorMessage = "User '" + username + "' not found!";
            model.addAttribute("errorMessage", errorMessage);
            return "error";
        }

        if (!SessionSecurity.getName().equals(user.getUsername())) { // Another user
            boolean isFollowing = this.following(currentUser.getUsername(), user.getUsername());
            model.addAttribute("isFollowing", isFollowing);
        } else {
            model.addAttribute("isFollowing", false);
        }

        // Follower
        Set<String> follower = userRepository.findFollowers(user.getUsername());
        Set<String> following = userRepository.findFollowing(user.getUsername());

        // Follower Count
        model.addAttribute("followingCnt", follower.size());
        model.addAttribute("followerCnt", following.size());

        model.addAttribute("isSelf", SessionSecurity.getName().equals(user.getUsername()));
        return "user";
    }

    @RequestMapping(value = "/follow/{username}")
    public String followUser(@PathVariable("username") String username, HttpServletRequest request, Model model) {

        // Get users
        User currentUser = userRepository.findUser(SessionSecurity.getName());
        User userToFollow = userRepository.findUser(username);

        userRepository.startFollowUser(currentUser.getUsername(), userToFollow.getUsername());

        if (request.getHeader("referer").contains("follower")) {
            int index = request.getHeader("referer").indexOf("8080");
            String referrer = request.getHeader("referer").substring(index + 5);

            // Do redirect to same page
            return "redirect:/" + referrer;
        }

        return "redirect:/users/" + username;
    }

    @RequestMapping(value = "/unfollow/{username}")
    public String unFollowUser(@PathVariable("username") String username, HttpServletRequest request, Model model) {

        // Get users
        User currentUser = userRepository.findUser(SessionSecurity.getName());
        User userToUnFollow = userRepository.findUser(username);

        userRepository.stopFollowUser(currentUser.getUsername(), userToUnFollow.getUsername());

        if (request.getHeader("referer").contains("follower")) {
            int index = request.getHeader("referer").indexOf("8080");
            String referrer = request.getHeader("referer").substring(index + 5);

            // Do redirect to same page
            return "redirect:/" + referrer;
        }

        return "redirect:/users/" + username;
    }

    @SuppressWarnings("Duplicates")
    @RequestMapping(value = "/users/{username}/follower")
    public String showFollower(@PathVariable("username") String username, Model model) {

        // Get currentUser
        User currentUser = userRepository.findUser(SessionSecurity.getName());
        model.addAttribute("currentUser", currentUser);

        // Get requested user
        User user = userRepository.findUser(username);
        if (user != null) {
            model.addAttribute("user", user);
        } else {
            // User not found
            String errorMessage = "User '" + username + "' not found!";
            model.addAttribute("errorMessage", errorMessage);
            return "error";
        }

        if (!SessionSecurity.getName().equals(user.getUsername())) { // Another user
            boolean isFollowing = this.following(currentUser.getUsername(), user.getUsername());
            model.addAttribute("isFollowing", isFollowing);
        } else {
            model.addAttribute("isFollowing", false);
        }

        // Follower
        Set<String> follower = userRepository.findFollowers(user.getUsername());
        Set<String> following = userRepository.findFollowing(user.getUsername());

        // Follower Count
        model.addAttribute("followingCnt", follower.size());
        model.addAttribute("followerCnt", following.size());

        // Following list
        List<Follower> listFollowing = new ArrayList<>();
        for (String name : follower)
            listFollowing.add(new Follower(name, this.following(user.getUsername(), name)));
        model.addAttribute("listFollowing", listFollowing);

        // Follower list
        List<Follower> listFollower = new ArrayList<>();
        for (String name : following)
            listFollower.add(new Follower(name, this.following(user.getUsername(), name)));
        model.addAttribute("listFollower", listFollower);

        model.addAttribute("isSelf", SessionSecurity.getName().equals(user.getUsername()));
        return "follower";
    }

    /**
     * Checks if username is following the otherUser
     * @param username
     * @param otherUser
     * @return
     */
    private boolean following(String username, String otherUser) {
        return userRepository.findFollowers(username).contains(otherUser);
    }
}
