package trackgenerate;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.tuple.Pair;
import util.StockholmAlignmentBlock;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TrackGenerator {
    private static Options options;
    private static String srcDir;
    private static String outputDirectory;
    private static Boolean writeHeader;

    public static void main(String[] args) throws IOException {
        // handle command-line argument processing :)
        // add all the arguments we need
        TrackGenerator.options = new Options();
        {
            TrackGenerator.options.addOption(
                    Option.builder("s")
                            .longOpt("srcDir")
                            .hasArg()
                            .desc("Input score files directory")
                            .required()
                            .build());
            TrackGenerator.options.addOption(
                    Option.builder("o")
                            .longOpt("outputDirectory")
                            .hasArg()
                            .desc("Output directory")
                            .required()
                            .build());
            TrackGenerator.options.addOption(
                    Option.builder("h")
                            .longOpt("writeHeader")
                            .desc("Include the header line")
                            .build());
            // TODO add options??? do I even need more options???
        }
        // Parse the commandline arguments
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            // set the src dirname
            TrackGenerator.srcDir = line.getOptionValue("s");
            // set the output filename
            TrackGenerator.outputDirectory = line.getOptionValue("o");
            // set the header line boolean
            TrackGenerator.writeHeader = line.hasOption("h");
        } catch (ParseException exp) {
            // something went wrong
            System.err.println("Parsing failed. Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(
                    "TrackGenerator [options] -s <dirname> -o <filename>",
                    options);
            return;
        }

        // generate the output files
        // the multiple-block motifs file
        Path output = Paths.get(TrackGenerator.outputDirectory, "multi.bed");
        Files.deleteIfExists(output);
        Files.createFile(output);
        BufferedWriter writer = Files.newBufferedWriter(output);
        // the single-block motifs file
        Path outputSingle = Paths.get(TrackGenerator.outputDirectory,
                                      "single.bed");
        Files.deleteIfExists(outputSingle);
        Files.createFile(outputSingle);
        BufferedWriter writerSingle = Files.newBufferedWriter(outputSingle);
        // histograms
        Path rnaStatsFile = Paths.get(TrackGenerator.outputDirectory,
                                      "graph_rnaScoreStats_prefilter" + ".png");
        SimpleHistogram rnaScoreStats = new SimpleHistogram(
                rnaStatsFile,
                "",
                "RNA posterior score",
                "Number of motifs",
                20);
        Path pairStatsFile = Paths.get(TrackGenerator.outputDirectory,
                                       "graph_pairScoreStats_prefilter" +
                                               ".png");
        SimpleHistogram pairScoreStats = new SimpleHistogram(
                pairStatsFile,
                "",
                "Pair posterior score",
                "Number of motifs",
                20);
        Path blockSpanFile = Paths.get(TrackGenerator.outputDirectory,
                                       "graph_blockSpanStats_prefilter" +
                                               ".png");
        SimpleHistogram blockSpanStats = new SimpleHistogram(
                blockSpanFile,
                "",
                "Number of blocks spanned",
                "Number of motifs",
                5);

        // write the header lines
        if (writeHeader) {
            // TODO set a more useful name field?
            writer.write("track"
                                 + " name=" + "motifsFoundMultiBlock"
                                 + " type=" + "bedDetail"
                                 + "\n");
            // TODO set a more useful name field?
            writerSingle.write("track"
                                       + " name=" + "motifsFoundSingleBlock"
                                       + " type=" + "bedDetail"
                                       + "\n");
        }

        // read the given directory...
        Path source = Paths.get(TrackGenerator.srcDir);
        for (File f : source.toFile().listFiles()) {
            // for each file in the given directory,
            // check if it should be added to the BED and add it if necessary
            StockholmAlignmentBlock block =
                    StockholmAlignmentBlock.constructFromScore(f);

            // something was malformatted?
            if (block == null) {
                continue;
            }

            // check if block contains human DNA
            if (!block.containsSpecies("hg38")) {
                continue;
            }

            BigDecimal pairScore = block.pairScore;
            BigDecimal rnaScore = block.rnaScore;

            // write the stats for this sequence to tracker
            pairScoreStats.addValue(pairScore);
            rnaScoreStats.addValue(rnaScore);
            blockSpanStats.addValue(
                    BigDecimal.valueOf(block.motifInNumBlocks("hg38")));

            // score filter
            if (rnaScore.compareTo(BigDecimal.valueOf(40L)) < 0) {
                continue;
            }

            // TODO set a better score filter and value guideline?
            BigDecimal max = BigDecimal.valueOf(125L);

            // construct the line that we will output for this motif
            Pair<BigInteger, BigInteger> interval
                    = block.getInterval("hg38");
            String chr = block.getChromosome("hg38");
            String name = f.getName();
            String thingToWrite = chr + "\t"
                    + interval.getKey() + "\t"
                    + interval.getValue() + "\t"
                    + name + "\t"
                    + rnaScore.divide(max, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(1000)) + "\n";
            // determine which BED track we want to output to
            if (block.motifInNumBlocks("hg38") == 1) {
                // add it to the single-block BED
                writerSingle.write(thingToWrite);
            } else {
                // add it to the multiblock BED
                writer.write(thingToWrite);
            }
        }
        writer.close();
        writerSingle.close();
        System.out.println("Wrote BED files");

        // Output histograms
        pairScoreStats.write();
        rnaScoreStats.write();
        blockSpanStats.write();
        System.out.println("Wrote statistics graphs");
    }
}