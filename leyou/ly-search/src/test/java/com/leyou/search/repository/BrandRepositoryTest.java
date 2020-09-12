package com.leyou.search.repository;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BrandRepositoryTest {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsClient goodsClient;
    @Test
    public void testCreateIndex(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }
    @Test
    public void loadData(){
        int page=1;
        int rows=100;
        int size;
        do {
            //查询spu信息
            PageResult<Spu> result = goodsClient.querySpuByPage(null, true, page, rows);
            List<Spu> spuList = result.getItems();
            if (CollectionUtils.isEmpty(spuList)){
                break;
            }
            //构成goods
            List<Goods> collect = spuList.stream().map(searchService::buildGoods).collect(Collectors.toList());
            //存进索引库
            goodsRepository.saveAll(collect);
            page++;
            size = spuList.size();
        }while (size==100);


    }

}
