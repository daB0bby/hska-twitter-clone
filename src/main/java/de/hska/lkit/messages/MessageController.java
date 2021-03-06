package de.hska.lkit.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.hska.lkit.elasticsearch.model.EsPost;
import de.hska.lkit.elasticsearch.repo.ESPostRepository;
import de.hska.lkit.redis.model.Post;
import de.hska.lkit.redis.repo.PostRepository;
import de.hska.lkit.redis.repo.UserRepository;


/**
 * Created by bob on 08.12.2016.
 */


@Controller
public class MessageController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ESPostRepository esPostRepo;

    @MessageMapping("/post")
    public void post(PostMessage postMessage) throws Exception {
        // Create and save post
        Post post = new Post(postMessage.getMessage(), postMessage.getUsername());
        postRepository.savePost(post);

        //Save in ES for search operations
        EsPost esPost = new EsPost(post);
        esPostRepo.save(esPost);
    }

    @RequestMapping(value = "/isfollower", method = RequestMethod.GET, produces = "application/json", params = { "username", "follower" })
    @ResponseBody
    public FollowerResponse isFollower(@RequestParam("username") String of, @RequestParam("follower") String name) {
        // Check if the user is follower of another one
        boolean isFollower = userRepository.isFollower(name, of);

        return new FollowerResponse(isFollower);
    }
}
