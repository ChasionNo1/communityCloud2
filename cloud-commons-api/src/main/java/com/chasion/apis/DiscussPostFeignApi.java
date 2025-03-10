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

@FeignClient(name = "cloud-post")
public interface DiscussPostFeignApi {

    @GetMapping("/discussPost/count")
    public int discussPostCount();

    @PostMapping("/discussPost/list")
    public ResultData<List<DiscussPostDTO>> discussPosts(@RequestBody Page page, @RequestParam("orderMode") int orderMode);


}
