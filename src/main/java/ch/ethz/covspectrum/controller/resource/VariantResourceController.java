package ch.ethz.covspectrum.controller.resource;

import ch.ethz.covspectrum.entity.core.Variant;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;


@RestController
@RequestMapping("/resource/variant")
public class VariantResourceController {

    private final DatabaseService databaseService;


    public VariantResourceController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("")
    public List<Variant> getAllKnownVariants() throws SQLException {
        return databaseService.getKnownVariants();
    }

}