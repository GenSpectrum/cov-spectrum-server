package ch.ethz.covspectrum.controller.resource;

import ch.ethz.covspectrum.entity.api.RxivArticle;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;


@RestController
@RequestMapping("/resource/article")
public class ArticleResourceController {

    private final DatabaseService databaseService;


    public ArticleResourceController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }


    @GetMapping("")
    public List<RxivArticle> getPangolinLineageArticles(
            @RequestParam String pangolinLineage
    ) throws SQLException {
        return databaseService.getPangolinLineageArticles(pangolinLineage);
    }

}
