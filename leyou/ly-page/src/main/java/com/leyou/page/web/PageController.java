package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
public class PageController {
    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id")Long id,Model model){
        Map<String,Object> attributes=pageService.loadModel(id);
        model.addAllAttributes(attributes);
        return "item";
    }

    @GetMapping("item/hello")
    public String tohello(Model model){
        model.addAttribute("msg","成功了");
        return "hello";
    }

}
