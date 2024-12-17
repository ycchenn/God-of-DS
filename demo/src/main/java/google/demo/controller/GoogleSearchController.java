package google.demo.controller;

import google.demo.service.GoogleQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000") // 根據你的前端地址調整
public class GoogleSearchController {

    @Autowired
    private GoogleQueryService googleQueryService;

    @GetMapping("/search")
    public HashMap<String, String> search(@RequestParam("q") String query) {
        try {
            return googleQueryService.search(query);
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
}