package com.bantads.assemblers;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.bantads.controller.AccountController;
import com.bantads.controller.EventController;
import com.bantads.dto.event.GetAccountDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class AccountDataModelAssembler
        implements RepresentationModelAssembler<GetAccountDTO, EntityModel<GetAccountDTO>> {


    @Override
    public EntityModel<GetAccountDTO> toModel(GetAccountDTO entity) {
        String number = entity.getNumero();

        EntityModel<GetAccountDTO> model = EntityModel.of(entity);
        model.add(linkTo(AccountController.class).slash(number).withSelfRel());
        model.add(linkTo(AccountController.class).slash(number).slash("extract").withRel("extrato"));
        model.add(linkTo(AccountController.class).slash(number).slash("balance").withRel("saldo"));
        model.add(linkTo(EventController.class).slash(number).slash("deposit").withRel("deposito"));
        model.add(linkTo(EventController.class).slash(number).slash("withdraw").withRel("saque"));
        model.add(linkTo(EventController.class).slash(number).slash("transfer").withRel("transferencia"));

        return model;
    }
}
