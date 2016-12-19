package iDynoOptimizer;
import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Results.Feature.Names.NumericFeatureNameIteration;
import iDynoOptimizer.Search.Search;
import iDynoOptimizer.Search.SimulationRun;

import java.io.IOException;


/**
 * Created by Chris on 11/19/2014.
 */
public class Driver {


    public static void main(String[] args) throws IOException {


        /*

        EXAMPLE:

        This code will do a parameter sweep for each protocol file in Y:\Chris Docs\code\projects\iDynoMicsOptimizer\inputoutput\input\protocolFolder

        Your output will be located at Y:\Chris Docs\code\projects\iDynoMicsOptimizer\inputoutput\output\AA
            It will create the folder AA
            If AA exists, it will output to AA-1. If that exists it will output to AA-2, etc...

            You can ignore the empty string parameter.

        */
        Global.getGlobal("E:\\ChrisDocs\\cloud\\Box Sync\\Chris-Research\\protocol-files\\sandbox", "E:\\ChrisDocs\\code\\cdyOpt-O\\out",
                "", "SimulatedLargeScale-RealCDPTest.xml", "cdp-test"
        );

        Search.runJustTarget(1);

        //Search.runJustTarget(1);
       // Search.doJustSweep(2);
//        Global g = Global.getGlobal();
//        SimulationRun.extractLast("E:\\ChrisDocs\\code\\cdyOpt-IO\\out\\bg-2\\target\\results", NumericFeatureNameIteration.maxHeight, NumericFeatureNameIteration.minHeight, NumericFeatureNameIteration.maxLength, NumericFeatureNameIteration.minLength, NumericFeatureNameIteration.agentCount, NumericFeatureNameIteration.totalMass).output();




      //  int[] itr = new int[2];
      //  itr[0] = 4;
      //  itr[1] = 12;

      // OutputConvergence.write("Y:\\Chris Docs\\code\\projects\\iDynoMicsOptimizer\\inputoutput\\output\\HillClimber-1", itr);

    // Search.doHillAllItersHeightToTarget();


//        List<String> tmp = new ArrayList<>();
//
//        Global.getGlobal().setExperimentFolder("Y:\\Chris Docs\\code\\projects\\iDynoMicsOptimizer\\inputoutput\\output\\HillClimber-1");
//
//        tmp.add("Y:\\Chris Docs\\code\\projects\\iDynoMicsOptimizer\\inputoutput\\output\\HillClimber-1\\test\\step-1\\cellMatrix128\\results(1)");
//        tmp.add("Y:\\Chris Docs\\code\\projects\\iDynoMicsOptimizer\\inputoutput\\output\\HillClimber-1\\test\\step-1\\cellMatrix128\\results(2)");
     //   Search.compilePovrays(tmp);

    }


}
