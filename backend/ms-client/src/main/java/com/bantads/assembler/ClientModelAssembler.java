package com.bantads.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.bantads.controller.ClientController;
import com.bantads.dto.ClientResponseDTO;

@Component
public class ClientModelAssembler implements RepresentationModelAssembler<ClientResponseDTO, EntityModel<ClientResponseDTO>> {

    @Override
    public EntityModel<ClientResponseDTO> toModel(ClientResponseDTO client) {
        return toModel(client, null);
    }

    public EntityModel<ClientResponseDTO> toModel(ClientResponseDTO client, String tipo) {
        EntityModel<ClientResponseDTO> model = EntityModel.of(client);
        model.add(linkTo(methodOn(ClientController.class)).slash("/"+client.getCpf()).withSelfRel());
        model.add(linkTo(methodOn(ClientController.class).getRequest(client.getId())).withRel("solicitacoes"));
        if ("GERENTE".equals(tipo)) {
            model.add(linkTo(methodOn(ClientController.class).listClients(null)).withRel("clientes"));
        }

        return model;
    }
    
}
