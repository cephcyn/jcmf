package cmf;

/**
 *
 * cmfinder04.pl
 */
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.*;

public class cmfinder {

    // default parameters
    String CMFINDER_PACKAGE_VERSION = "0.4.1.15";
    int CAND = 40;
    int MAXSPAN1 = 100;
    int MINSPAN1 = 30;
    int MAXSPAN2 = 100;
    int MINSPAN2 = 40;
    int CLUSTER = 3;
    double FRACTION = 0.8;
    int SINGLE = 5;
    int DOUBLE = 5;
    int verbose = 0;
    int help = 0;
    int COMBINE = 0;
    String cand_weight_option = "";
    String cmfinderBaseExe = "cmfinder04";
    // ($likeold,$skipClustalw,$useOldCmfinder,$simpleMotifsAlreadyDone,$justGetCmfinderCommand,
    // $copyCmfinderRunsFromLog,$amaa,$version,$filterNonFrag,$fragmentary,$commaSepEmFlags,
    // $commaSepSummarizeFlags,$commaSepCandfFlags,$saveTimer,$allCpus,$cpu,$candsParallel,$outFileSuffix,
    // $columnOnlyBasePairProbs);
    int emulate_apparent_bug_in_resolve_overlap = 1;

    //getOptions parameters from json file
    /*
    boolean version;
    boolean h;
    boolean v;
    String w;
    int c;
    int minspan1;
    int maxspan1;
    int minspan2;
    int maxspan2;
    double f;
    int s1;
    int s2;
    int combine;
    int o;
    double n;
    boolean skipClustalw;
    boolean likeold;
    boolean useOldCmfinder;
    boolean simpleMotifsAlreadyDone;
    boolean justGetCmfinderCommand;
    String copyCmfinderRunsFromLog;
    boolean amaa;
    String motifList;
    int minCandScoreInFinal;
    String emSeq;
    boolean filterNonFrag;
    boolean fragmentary;
    //flags
    boolean degen_rand;
    String degen_keep;
     */
    //read json file
    public JSONObject read_json_file(String file_name) throws JSONException, IOException {
        Path p = Paths.get(file_name);
        byte[] jsonByte = Files.readAllBytes(p);
        JSONObject jsonObject = new JSONObject(new String(jsonByte));
        return jsonObject;
    }

    /* decide too many parameters, use JSONObject directly
    public void parse_param(JSONObject jo) {
        version = jo.optBoolean("version");
        h = jo.optBoolean("h");
        v = jo.optBoolean("v");
        w = jo.optString("w");
        c = jo.optInt("c");
        minspan1 = jo.optInt("minspan1");
        maxspan1 = jo.optInt("maxspan1");
        minspan2 = jo.optInt("minspan2");
        maxspan2 = jo.optInt("maxspan2");
        f = jo.optDouble("f");
        s1 = jo.optInt("s1");
        s2 = jo.optInt("s2");
        combine = jo.optInt("combine");
        o = jo.optInt("o");
        n = jo.optDouble("n");
        skipClustalw = jo.optBoolean("skipClustalw");
        likeold = jo.optBoolean("likeold");
        useOldCmfinder = jo.optBoolean("useOldCmfinder");
        simpleMotifsAlreadyDone = jo.optBoolean("simpleMotifsAlreadyDone");
        justGetCmfinderCommand = jo.optBoolean("justGetCmfinderCommand");
        copyCmfinderRunsFromLog = jo.optString("copyCmfinderRunsFromLog");
        amaa = jo.optBoolean("amaa");
        motifList = jo.optString("motifList");
        minCandScoreInFinal = jo.optInt("minCandScoreInFinal");
        emSeq = jo.optString("emSeq");
        filterNonFrag = jo.optBoolean("filterNonFrag");
        fragmentary = jo.optBoolean("fragmentary");
        degen_rand = jo.getJSONObject("commaSepEmFlags").optBoolean("degen-rand");
        degen_keep = jo.getJSONObject("commaSepEmFlags").optString("degen_keep");
        fragmentary = jo.optBoolean("fragmentary");
    }
     */
    public void print_version() {
        System.out.println("CMFINDER_PACKAGE_VERSION=" + CMFINDER_PACKAGE_VERSION);
    }

    public void print_help() {
        String help_content = "CMFINDER [options] SEQ\n"
                + "Options:\n"
                + "    -c <number>      \n"
                + "     The maximum number of candidates in each sequence. Default 40. No bigger than 100.\n"
                + "    -m <number>      \n"
                + "     The minimum length of candidates. Default 30\n"
                + "    -M <number>      \n"
                + "     The maximum length of candidates. Default 100\n"
                + "    -f <number>      \n"
                + "     The fraction of the sequences expected to contain the motif. Default 0.80\n"
                + "    -s1 <number>     \n"
                + "     The max number of output single stem-loop motifs\n"
                + "    -s2 <number>    \n"
                + "     The max number of output double stem-loop motifs    \n"
                + "    -minspan1 <number>\n"
                + "     minimum span of a candidate sub-sequence in the heuristics to come up with an initial alignment for single-hairpin (h1) motifs\n"
                + "    -maxspan1 <number>\n"
                + "     like -minspan1, but maximum\n"
                + "    -minspan2 <number>\n"
                + "     like -minspan1, but for double-hairpin (h2) motifs\n"
                + "    -maxspan2 <number>\n"
                + "     like -minspan2, but maximal\n"
                + "    -combine         \n"
                + "     Combine the output motifs. Default False\n"
                + "    -motifList <file> \n"
                + "     Produce a list of motifs generated, one motif per line.\n"
                + "    -o <number>\n"
                + "     Minimum overlap for combining motifs\n"
                + "    -n <number>      \n"
                + "     Minimum number of sequences (weighted) for combining motifs\n"
                + "    -emSeq <file>\n"
                + "     Use the sequences in this fasta file for the expectation maximization step (i.e., the C executable cmfinder), but not for the earlier steps related to finding candidate motifs.  The reason for this distinction is that it is somewhat easier to add weighting to the cmfinder program, than the various canda, candf, cands and align programs.\n"
                + "    -likeold         \n"
                + "     Behave as much as possible like the old CMfinder, e.g., passing --enone, --p56 and --degen-rand to cmfinder_inf11.  It's not possible to produce identical results to CMfinder 0.3, but these flags make it more similar.\n"
                + "    -fragmentary\n"
                + "     Pass --fragmentary for cmfinder\n"
                + "    -amaa            \n"
                + "     Pass --amaa to cmfinder (align max align accuracy)\n"
                + "    -useOldCmfinder  \n"
                + "     Run the old cmfinder executable, mainly to test whether we get different results because of this perl script, or the cmfinder_inf11 executable.\n"
                + "    -skipClustalw    \n"
                + "     Do not run clustalw, like older installations lacking this program.\n"
                + "    -justGetCmfinderCommand    \n"
                + "     Print the command to run for the cmfinder executable, with appropriate partial flags.  This can be used to realign an existing .sto file, for example.\n"
                + "    -copyCmfinderRunsFromLog <log-file> \n"
                + "     For debugging.  Reads a log file that contains cmfinder commands, and re-runs them with new CMfinder.\n"
                + "    -commaSepEmFlags x<flags>\n"
                + "     List of flags and arguments to pass to the EM-step cmfinder exe.  There's an 'x' at the beginning of the flags, so that perl doesn't interpret the flags as flags for it.  It's comma-separated where on the command line it would be space separated.  I think commas are safe, and mean that I don't have to worry about quoting stuff.  e.g., -commaSepEmFlags x--fragmentary,--filter-non-frag,--filter-non-frag-pad,10 would pass this to the cmfinder program: \"--fragmentary --filter-non-frag --filter-non-frag-pad 10\", i.e., just replace commas with spaces.\n"
                + "    -commaSepSummarizeFlags x<flags>\n"
                + "     Flags to pass to the --summarize command.  Same syntax as for --commaSepEmFlags\n"
                + "    -commaSepCandfFlags x<flags>\n"
                + "     Flags to pass to the candf command.  Same syntax as for --commaSepEmFlags\n"
                + "    -minCandScoreInFinal <number>    \n"
                + "     Pass --min-cand-score-in-final <number> to cmfinder.  WARNING: there's a difference between using this flag (where even intermediate motifs will avoid these hits) and taking the low-scoring instances out of the final alignments (which might be combinations of motifs in which the sequence would have been lower-scoring).\n"
                + "    -filterNonFrag\n"
                + "     Pass --filter-non-frag to cmfinder\n"
                + "    -columnOnlyBasePairProbs\n"
                + "     Pass --column-only-base-pair-probs to cmfinder\n"
                + "    -saveTimer <file>\n"
                + "     create tab-delimited <file> containing timing stats on various sub-processes of this script.  the first tab-delimited field is the description of the sub-process, the second field is the total CPU time (user+sys) and the third field is the wall-clock time.  Sub-processes can occur in multiple lines if they are run multiple timers, so the caller should add them.  Due to my laziness, the time of the clustalw program (if used) is not counted.\n"
                + "    -cpu <num>\n"
                + "     use <num> CPUs for functionality that can use multi-CPUs (currently only the internal cmsearch commands in cmfinder04)\n"
                + "    -allCpus\n"
                + "     equivalent to -cpu X , where X is the number of available processors.\n"
                + "    -candsParallel\n"
                + "     run the two cands jobs in parallel, even if -cpu 1\n"
                + "    -outFileSuffix <string>\n"
                + "     add <string> to the output file names.  this is useful if you want to run the script in multiple ways in the same directory.\n"
                + "    -h               \n"
                + "     Show this list\n"
                + "    -version\n"
                + "     Show package version\n";
        System.err.println(help_content);
    }

    public static void main(String[] args) {
        try {
            cmfinder cm = new cmfinder();
            JSONObject jo = cm.read_json_file("src/cmf/cmfinder_param.json");
            //cm.parse_param(jo);

        } catch (JSONException ex) {
            Logger.getLogger(cmfinder.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(cmfinder.class.getName()).log(Level.SEVERE, null, ex);
        }

        int arr_size = 20;
        int[] list1 = new int[arr_size];
        int[] list2 = new int[arr_size];
        for (int i = 0; i < arr_size; i++) {
            int n = (int) (Math.random() * 99 + 1);
            list1[i] = n;
            n = (int) (Math.random() * 99 + 1);
            list2[i] = n;
        }

    }
}
