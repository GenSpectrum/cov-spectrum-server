package ch.ethz.covspectrum.controller.resource;

import ch.ethz.covspectrum.entity.api.ReferenceGenomeResponse;
import ch.ethz.covspectrum.entity.core.Gene;
import ch.ethz.covspectrum.service.DatabaseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/resource/reference-genome")
public class ReferenceGenomeResourceController {

    private final ReferenceGenomeResponse response;

    public ReferenceGenomeResourceController(DatabaseService databaseService) {
        String nucleotideSequence;
        List<Gene> genes = new ArrayList<>();
        String nucSeqSql = """
            select seq
            from consensus_sequence
            where sample_name = 'REFERENCE_GENOME';
        """;
        String geneAASeqSql = """
            select gene, reference_aa_sequence, start_position, end_position
            from gene
            order by start_position;
        """;
        try (Connection conn = databaseService.getDatabaseConnection()) {
            try (Statement statement = conn.createStatement()) {
                try (ResultSet rs = statement.executeQuery(nucSeqSql)) {
                    rs.next();
                    nucleotideSequence = rs.getString("seq");

                }
                try (ResultSet rs = statement.executeQuery(geneAASeqSql)) {
                    while (rs.next()) {
                        genes.add(new Gene()
                                .setName(rs.getString("gene"))
                                .setAaSeq(rs.getString("reference_aa_sequence"))
                                .setStartPosition(rs.getInt("start_position"))
                                .setEndPosition(rs.getInt("end_position"))
                        );
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        response = new ReferenceGenomeResponse(nucleotideSequence, genes);
    }

    @GetMapping("")
    public ReferenceGenomeResponse getReferenceGenome() {
        return response;
    }

}
