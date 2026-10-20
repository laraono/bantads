package com.bantads.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.bantads.controller.ManagerController;
import com.bantads.dto.ManagerDTO;
import com.bantads.entity.Manager;

@Component
public class ManagerModelAssembler implements RepresentationModelAssembler<Manager, EntityModel<Manager>> {

    @Override
    public EntityModel<Manager> toModel(Manager manager) {
        EntityModel<Manager> model = EntityModel.of(manager);
        model.add(linkTo(methodOn(ManagerController.class).getManagerByCpf(manager.getCpf())).withSelfRel());
        return model;
    }

    public EntityModel<ManagerDTO> toModel(ManagerDTO manager) {
        EntityModel<ManagerDTO> model = EntityModel.of(manager);
        model.add(linkTo(methodOn(ManagerController.class).getManagerByCpf(manager.getCpf())).withSelfRel());
        return model;
    }

}
