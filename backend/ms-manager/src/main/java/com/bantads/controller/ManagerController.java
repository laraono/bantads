package com.bantads.controller;

import com.bantads.dto.CreateManagerDTO;
import com.bantads.dto.ManagerDTO;
import com.bantads.dto.UpdateManagerDTO;
import com.bantads.entity.Manager;
import com.bantads.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/managers")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @PostMapping()
    public ResponseEntity<Manager> createManager(@RequestBody  CreateManagerDTO manager) {
        try {
            Manager gerente = managerService.createManager(manager);
            return ResponseEntity.status(HttpStatus.CREATED).body(gerente);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping()
    public ResponseEntity<Map<String, Object>> listManagers() {
        try{
            Map<String, Object> gerentes =  managerService.listManagers();
            return ResponseEntity.status(HttpStatus.CREATED).body(gerentes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{cpf}")
    public ResponseEntity<Object> updateManager(@PathVariable String managerCPF, @RequestBody UpdateManagerDTO manager) {
        try {
            managerService.updateManager(managerCPF, manager);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @DeleteMapping("/{cpf}")
    public ResponseEntity<Object> deleteManager(@RequestHeader("x-user-cpf") String userCPF, @PathVariable String managerCPF) {
        try {
            this.managerService.deleteManager(managerCPF, userCPF);;
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<Manager> getManagerByCpf(@PathVariable String cpf) {
        try{
            Manager gerente = managerService.getManager(cpf);
            return ResponseEntity.status(HttpStatus.CREATED).body(gerente);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
 
}
