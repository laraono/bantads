package com.bantads.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.bantads.controller.RequestController;
import com.bantads.dto.RequestResponseDTO;
import com.bantads.entity.RequestStatus;

@Component
public class RequestModelAssembler implements RepresentationModelAssembler<RequestResponseDTO, EntityModel<RequestResponseDTO>>  {
    
    @Override
    public EntityModel<RequestResponseDTO> toModel(RequestResponseDTO request) {

        EntityModel<RequestResponseDTO> model = EntityModel.of(request);

        model.add(linkTo(methodOn(RequestController.class).getRequest(request.getCpf())).withSelfRel());
        
        if (request.getStatus() == RequestStatus.PENDENTE) {
            model.add(linkTo(RequestController.class).slash(request.getCpf()).slash("aprovacao").withRel("aprovacao"));
            model.add(linkTo(RequestController.class).slash(request.getCpf()).slash("rejeicao").withRel("rejeicao"));
        }

        return model;
    }
}
