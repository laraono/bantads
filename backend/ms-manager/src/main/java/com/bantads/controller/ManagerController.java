package com.bantads.controller;

import com.bantads.assembler.ManagerModelAssembler;
import com.bantads.dto.CreateManagerDTO;
import com.bantads.dto.ManagerDTO;
import com.bantads.dto.UpdateManagerDTO;
import com.bantads.service.ManagerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/managers")
public class ManagerController {

    @Autowired
    private ManagerModelAssembler assembler;

    @Autowired
    private ManagerService managerService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Map<String, List<EntityModel<ManagerDTO>>> listManagers() {
        List<EntityModel<ManagerDTO>> gerentes = managerService.list().stream().map(assembler::toModel).toList();
        return Map.of("gerentes", gerentes);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<ManagerDTO> createManager(@Valid @RequestBody CreateManagerDTO manager) {
        try {
            ManagerDTO gerente = managerService.createManager(manager);
            return assembler.toModel(gerente);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<ManagerDTO> updateManager(@PathVariable("cpf") String managerCPF,
            @Valid @RequestBody UpdateManagerDTO manager) {
        try {
            ManagerDTO atualizado = managerService.updateManager(managerCPF, manager);
            return assembler.toModel(atualizado);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{cpf}")
    @ResponseStatus(HttpStatus.OK)
    public EntityModel<ManagerDTO> getManagerByCpf(@PathVariable String cpf) {
        try {
            ManagerDTO gerente = managerService.getManager(cpf);
            return assembler.toModel(gerente);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
