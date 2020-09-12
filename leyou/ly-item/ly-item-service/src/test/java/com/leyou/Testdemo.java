package com.leyou;


import com.leyou.common.dto.CartDTO;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Testdemo {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private GoodsService goodsService;
    @Test
    public void queryloadCategoryAndBrandName(){

        categoryService.queryByIds(Arrays.asList(74L,75L,76L))
        .stream().map(Category::getName);
    }
    @Test
    public void test1(){
        List<CartDTO> list = Arrays.asList(new CartDTO(2600242l, 2), new CartDTO(2600248l, 2));
        goodsService.decreaseStock(list);

    }


}
