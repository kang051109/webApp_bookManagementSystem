package com.example.webapp_project.controller;

import com.example.webapp_project.config.JsonResponse;
import com.example.webapp_project.model.User;
import com.example.webapp_project.repository.UserRepository;
import com.example.webapp_project.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService = AuthService.getInstance();
    private final UserRepository userRepo = UserRepository.getInstance();

    @GetMapping("/users")
    public JsonResponse<Map<String,Object>> listUsers(@RequestParam(defaultValue="1")int page,
            @RequestParam(defaultValue="10")int size, HttpServletRequest req) {
        if(!authService.isAdmin(req)) return JsonResponse.forbidden("Admin only");
        List<User> all = userRepo.findAll();
        int total=all.size(),from=(page-1)*size,to=Math.min(from+size,total);
        Map<String,Object> d=new HashMap<>();
        d.put("users", from<to?all.subList(from,to):Collections.emptyList());
        d.put("page",page); d.put("size",size); d.put("total",total);
        d.put("totalPages", Math.max(1,(total+size-1)/size));
        return JsonResponse.success(d);
    }

    @DeleteMapping("/users/{id}")
    public JsonResponse<Void> deleteUser(@PathVariable Long id, HttpServletRequest req) {
        if(!authService.isAdmin(req)) return JsonResponse.forbidden("Admin only");
        User u=userRepo.findById(id);
        if(u==null) return JsonResponse.notFound("User not found");
        if("admin".equals(u.getRole())) return JsonResponse.badRequest("Cannot delete admin");
        userRepo.delete(id);
        return JsonResponse.success("User deleted",null);
    }
}
