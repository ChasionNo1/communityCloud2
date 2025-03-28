package com.chasion.controller;

import com.chasion.entity.DiscussPost;
import com.chasion.entity.DiscussPostDTO;
import com.chasion.resp.ResultData;
import com.chasion.resp.ReturnCodeEnum;
import com.chasion.service.DiscussPostService;
import com.chasion.service.ElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/discussPost")
@RefreshScope
public class ESController {

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    ElasticsearchService elasticsearchService;

    @GetMapping("/search")
    public ResultData<List<DiscussPostDTO>> searchDiscussPost(@RequestParam("keyword") String keyword,
                                                              @RequestParam("current") int current,
                                                              @RequestParam("limit") int limit) throws IOException {
        List<DiscussPostDTO> discussPosts = elasticsearchService.searchDiscussPost(keyword, current, limit);
        ResultData<List<DiscussPostDTO>> resultData = new ResultData<>();
        if (!discussPosts.isEmpty()) {
            resultData.setCode(ReturnCodeEnum.RC200.getCode());
            resultData.setMessage(ReturnCodeEnum.RC200.getMessage());
            resultData.setData(discussPosts);
        }else {
            resultData.setCode(ReturnCodeEnum.RC201.getCode());
            resultData.setMessage(ReturnCodeEnum.RC201.getMessage());
        }
        return resultData;
    }
}
