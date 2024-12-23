package google.demo.controller;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import google.demo.service.GoogleQueryService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // 根據你的前端地址調整
public class GoogleSearchController {

    @Autowired
    private GoogleQueryService googleQueryService;

    @GetMapping("/search")
    public LinkedHashMap<String, Object> search(@RequestParam("q") String query) {
        try {
            return googleQueryService.search(query);
        } catch (IOException e) {
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }
}
