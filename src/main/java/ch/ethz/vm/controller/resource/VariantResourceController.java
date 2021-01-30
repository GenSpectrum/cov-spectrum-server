package ch.ethz.vm.controller.resource;

import ch.ethz.vm.service.DatabaseService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/resource/variant")
public class VariantResourceController {

    private final DatabaseService databaseService;


    public VariantResourceController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

}
