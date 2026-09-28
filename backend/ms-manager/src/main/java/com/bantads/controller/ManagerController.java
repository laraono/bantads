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
    public ResponseEntity<List<ManagerDTO>> listManagers() {
        try{
            List<ManagerDTO> gerentes =  managerService.listManagers();
            return ResponseEntity.status(HttpStatus.CREATED).body(gerentes);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateManager(@PathVariable Long id, @RequestBody UpdateManagerDTO manager) {
        try {
            managerService.updateManager(id, manager);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteManager(@RequestHeader("x-user-cpf") String userCPF, @PathVariable Long id) {
        try {
            this.managerService.deleteManager(id, userCPF);;
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<Manager> getManagerByCpf(@PathVariable String cpf) {
        try{
            Manager gerente = managerService.getManagerByCpf(cpf);
            return ResponseEntity.status(HttpStatus.CREATED).body(gerente);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
}
