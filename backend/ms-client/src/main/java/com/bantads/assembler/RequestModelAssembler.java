package com.bantads.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import com.bantads.controller.RequestController;
import com.bantads.entity.Request;

public class RequestModelAssembler implements RepresentationModelAssembler<Request, EntityModel<Request>>  {
    
    @Override
    public EntityModel<Request> toModel(Request request) {
        EntityModel<Request> model = EntityModel.of(request);

        model.add(linkTo(methodOn(RequestController.class).getRequest(request.getRequestId())).withSelfRel());
        
        if ("PENDENTE".equals(request.getStatus())) {
            model.add(linkTo(RequestController.class).slash(request.getRequestId()).slash("aprovacao").withRel("aprovacao"));
            model.add(linkTo(RequestController.class).slash(request.getRequestId()).slash("rejeicao").withRel("rejeicao"));
        }

        return model;
    }
}
