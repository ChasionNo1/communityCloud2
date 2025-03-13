package com.chasion.apis;

import com.chasion.entity.DiscussPostDTO;
import com.chasion.entity.Page;
import com.chasion.resp.ResultData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "cloud-post", path = "discussPost")
public interface DiscussPostFeignApi {

    @GetMapping("/get/count")
    public int discussPostCount();

    @PostMapping("/get/list")
    public ResultData<List<DiscussPostDTO>> discussPosts(@RequestBody Page page, @RequestParam("orderMode") int orderMode);

    @PostMapping("/add/discussPost")
    public ResultData<String> addDiscussPost(@RequestParam("userId") int userId, @RequestParam("title") String title, @RequestParam("content") String content);


}
