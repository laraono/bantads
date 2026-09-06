package com.bantads.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.bantads.controller.ClientController;
import com.bantads.entity.Client;

@Component
public class ClientModelAssembler implements RepresentationModelAssembler<Client, EntityModel<Client>> {

    @Override
    public EntityModel<Client> toModel(Client client) {
        EntityModel<Client> model = EntityModel.of(client);
        model.add(linkTo(methodOn(ClientController.class).getClient(client.getClientId())).withSelfRel());
        model.add(linkTo(methodOn(ClientController.class).getRequest(client.getClientId())).withRel("request"));
        model.add(linkTo(methodOn(ClientController.class).listClients()).withRel("clients"));
        return model;
    }
    
}
