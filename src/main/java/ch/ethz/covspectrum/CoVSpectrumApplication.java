package ch.ethz.covspectrum;

import ch.ethz.covspectrum.fiv.FindInterestingVariants;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;


@SpringBootApplication
public class CoVSpectrumApplication {

    private static final Logger logger = LoggerFactory.getLogger(CoVSpectrumApplication.class);

    public static void main(String[] args) throws SQLException, JsonProcessingException {
        System.getProperties().setProperty("org.jooq.no-logo", "true");

        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.out.println("There is no manual, sorry.");
                System.exit(0);
                return;
            }
            if ("--find-interesting-variants".equals(args[0])) {
                if (args.length != 5 && args.length != 6) {
                    System.err.println("Please use the program with the following arguments: " +
                            "--find-interesting-variants <country> <minNumberSamplesRelative> <maxVariantLength> " +
                            "<maxNumberOfVariants>");
                    System.exit(1);
                    return;
                }
                String country = args[1];
                float minNumberSamplesRelative = Float.parseFloat(args[2]);
                int maxVariantLength = Integer.parseInt(args[3]);
                int maxNumberOfVariants = Integer.parseInt(args[4]);
                logger.info("Start finding interesting variants for " + country + ".");
                long start = System.currentTimeMillis();
                FindInterestingVariants findInterestingVariants = new FindInterestingVariants();
                findInterestingVariants.doWork(
                        country,
                        minNumberSamplesRelative,
                        maxVariantLength,
                        maxNumberOfVariants
                );
                long finish = System.currentTimeMillis();
                long timeElapsed = (finish - start) / 1000;
                logger.info("Finished after " + timeElapsed + " seconds.");
                System.exit(0);
                return;
            }
        }

        // The main server
        SpringApplication.run(CoVSpectrumApplication.class, args);
    }

}
