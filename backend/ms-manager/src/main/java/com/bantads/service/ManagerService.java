package com.bantads.service;

import com.bantads.dto.CreateManagerDTO;
import com.bantads.dto.UpdateManagerDTO;
import com.bantads.entity.Manager;
import com.bantads.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManagerService {

    @Autowired
    private ManagerRepository managerRepository;

    public Manager createManager(CreateManagerDTO createManagerDTO) {
        Manager manager = Manager.builder()
                .cpf(createManagerDTO.getCpf())
                .email(createManagerDTO.getEmail())
                .name(createManagerDTO.getName())
                .phone(createManagerDTO.getPhone())
                .build();

        return managerRepository.save(manager);
    }

    public List<Manager> listManagers() {
        return managerRepository.findAllByIsActive(true);
    }

    public Manager getManager(Long id) {
        return managerRepository.findDistinctByManagerIdAndIsActive(id, true);
    }

    public void deleteManager(Long id) {
        List<Manager> managers = managerRepository.findByManagerIdNotAndIsActive(id, true);

        if(!managers.isEmpty()) {
            Manager manager = this.getManager(id);

            manager.setActive(false);

            managerRepository.save(manager);
        }
    }

    public void updateManager(Long id, UpdateManagerDTO updateManagerDTO) {
        Manager manager = this.getManager(id);

        manager.setName(updateManagerDTO.getName());
        manager.setPhone(updateManagerDTO.getPhone());

        managerRepository.save(manager);
    }
}
