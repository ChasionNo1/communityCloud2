package com.chasion.dao;

import com.chasion.entity.DiscussPost;
import com.chasion.entity.DiscussPostDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPostDTO, Integer> {

}
