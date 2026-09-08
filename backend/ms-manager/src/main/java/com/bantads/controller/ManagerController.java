package com.bantads.controller;

import com.bantads.dto.CreateManagerDTO;
import com.bantads.dto.UpdateManagerDTO;
import com.bantads.entity.Manager;
import com.bantads.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/managers")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @PostMapping()
    Manager createManager(@RequestBody  CreateManagerDTO manager) {
        return managerService.createManager(manager);
    }

    @GetMapping()
    List<Manager> listManagers() {
        return managerService.listManagers();
    }

    @GetMapping("/{id}")
    Manager getManager(@PathVariable Long id) {
        return managerService.getManager(id);
    }

    @PutMapping("/{id}")
    void updateManager(@PathVariable Long id, @RequestBody UpdateManagerDTO manager) {
        managerService.updateManager(id, manager);
    }

    @DeleteMapping("/{id}")
    void deleteManager(@PathVariable Long id) {
        managerService.deleteManager(id);
    }
}
