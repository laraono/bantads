package com.bantads.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.bantads.controller.ClientController;
import com.bantads.entity.Client;

import lombok.Getter;
import lombok.Setter;

@Component
public class ClientModelAssembler implements RepresentationModelAssembler<Client, EntityModel<Client>> {

    @Setter 
    public String tipo;

    @Override
    public EntityModel<Client> toModel(Client client) {
        EntityModel<Client> model = EntityModel.of(client);
        model.add(linkTo(methodOn(ClientController.class)).slash("/"+client.getCpf()).withSelfRel());
        model.add(linkTo(methodOn(ClientController.class).getRequest(client.getClientId())).withRel("solicitacoes"));
        if(tipo.equals("GERENTE")) {
            model.add(linkTo(methodOn(ClientController.class).listClients(null)).withRel("clientes"));
        }

        return model;
    }
    
}
